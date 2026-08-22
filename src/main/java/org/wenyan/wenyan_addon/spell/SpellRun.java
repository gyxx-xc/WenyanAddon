package org.wenyan.wenyan_addon.spell;

import indi.wenyan.content.block.LazyProgram;
import indi.wenyan.content.block.runner.BlockPackageGetter;
import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.ImportRequest;
import indi.wenyan.interpreter_impl.SimpleRequest;
import indi.wenyan.interpreter_impl.WenyanSymbol;
import indi.wenyan.judou.api.exec.structure.IExecQueue;
import indi.wenyan.judou.api.exec.structure.IHandleContext;
import indi.wenyan.judou.api.exec.structure.IWenyanPlatform;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.language.Symbol;
import indi.wenyan.judou.api.runtime.IWenyanScheduler;
import indi.wenyan.judou.api.runtime.RunnerCreator;
import indi.wenyan.judou.api.utils.Either;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.IWenyanObject;
import indi.wenyan.judou.api.values.WenyanPackage;
import indi.wenyan.judou.api.values.exception.WenyanCompileException;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import indi.wenyan.judou.runtime.function_impl.WenyanSchedularImpl;
import indi.wenyan.setup.language.ExceptionText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
import org.wenyan.wenyan_addon.qi.spell.PlayerCastContext;
import org.wenyan.wenyan_addon.qi.spell.PlayerCastRequest;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

/**
 * 单次法术运行：以玩家为中心的文言程序运行器。
 * 实现 IWenyanPlatform，每 tick 由 SpellRunManager 驱动 step()/handle()。
 * 熔断：单次运行最多 200 tick（10 秒），超时强制中止。
 */
public class SpellRun implements IWenyanPlatform {
    /** 熔断时长：200 tick = 10 秒 */
    public static final int MAX_TICKS = 200;

    private final ServerPlayer player;
    private final ServerLevel level;
    private final SpellEnvironment env;
    private final String spellCode;
    private final int step;

    private final LazyProgram<IWenyanScheduler<WenyanSchedularImpl.PCB>> lazyProgram;
    private final IExecQueue execQueue = IExecQueue.create(this);
    private final BlockPackageGetter blockPackageGetter = new BlockPackageGetter(_ -> {
    });
    private final Deque<String> errors = new ConcurrentLinkedDeque<>();

    private int ticksElapsed = 0;
    private boolean fuseTripped = false;
    private boolean finished = false;

    public SpellRun(ServerPlayer player, SpellEnvironment env, String spellCode, int step) {
        this.player = player;
        this.level = (ServerLevel) player.level();
        this.env = env;
        this.spellCode = spellCode;
        this.step = Math.max(1, step);
        this.lazyProgram = new LazyProgram<>(() -> IWenyanScheduler.defaultImpl(this, this.step));
    }

    /**
     * 启动运行：创建线程并注册到调度器。必须在主线程调用。
     */
    public void launch() {
        try {
            RunnerCreator.createThread(lazyProgram, env.bytecode(), initEnvironment());
        } catch (WenyanException | WenyanCompileException e) {
            handleError(e.getMessage());
        }
    }

    private WenyanPackage initEnvironment() {
        var base = IWenyanPlatform.initEnvironment();
        base.put(Symbol.IMPORT_ID, ImportRequest.handlerOf(this::getPackage));
        base.put(WenyanSymbol.PRINT, SimpleRequest.handlerOf((self, args) -> {
            String text = args.getFirst().as(WenyanString.TYPE).value();
            player.sendSystemMessage(Component.literal(text));
            return WenyanNull.NULL;
        }));
        return base;
    }

    /**
     * 包解析：优先背包拓展包扫描结果，其次扫描周围 3 格设备方块，兜底报错。
     */
    private Either<IWenyanObject, String> getPackage(IHandleContext context, String name) throws WenyanException {
        String code = env.scrollPackages().get(name);
        if (code != null) {
            return Either.right(code);
        }
        var device = env.devicePackages().get(name);
        if (device != null) {
            return Either.left(processItemDevice(device));
        }
        var block = blockPackageGetter.getPackage(level, player.blockPosition(), name);
        if (block != null) {
            return block;
        }
        String available = String.join(", ", env.scrollPackages().keySet());
        throw new WenyanException(ExceptionText.ImportNotFound.string(name)
                + (available.isEmpty() ? "（背包拓展包内无符咒）" : "（已扫描到：" + available + "）"));
    }

    /**
     * 背包物品设备包装为玩家施法包：优先使用注册的玩家版函数包（PlayerCastContext 签名，
     * 以玩家为施法主体）；未注册时回落到设备自身包（通用 IHandleContext 签名天然兼容）。
     * 请求类型为 {@link PlayerCastRequest}，执行时注入 {@link PlayerCastContext}，
     * 与方块设备请求（BlockRequest + BlockContext）互不干扰。
     */
    private WenyanPackage processItemDevice(SpellEnvironmentScanner.DeviceEntry entry) {
        Function<ItemStack, RawHandlerPackage> playerPackage = PlayerDevicePackages.of(entry.stack().getItem());
        RawHandlerPackage raw = playerPackage != null ? playerPackage.apply(entry.stack()) : entry.device().getExecPackage();
        var map = new java.util.HashMap<>(raw.variables());
        raw.functions().forEach((functionName, function) ->
                map.put(functionName, (indi.wenyan.judou.api.exec.IRequestCallHandler) (thread, self, argsList, onReturn) ->
                        new PlayerCastRequest(player, thread, self, argsList, function.get(), onReturn)));
        return new WenyanPackage(map);
    }

    /**
     * 每 tick 驱动运行：先派发错误，再推进程序。主线程调用。
     */
    public void tick() {
        while (!errors.isEmpty()) {
            player.sendSystemMessage(Component.literal(errors.removeFirst()).withStyle(ChatFormatting.RED));
        }
        if (finished) {
            return;
        }
        if (++ticksElapsed > MAX_TICKS) {
            fuseTripped = true;
            finish("术式已熔断");
            return;
        }
        if (!isRunning()) {
            finish(null);
            return;
        }
        lazyProgram.ifCreated().ifPresent(program -> {
            program.step();
            BlockRequest.BlockContext context = new BlockRequest.BlockContext(
                    level, player.blockPosition(), level.getBlockState(player.blockPosition()));
            ((BlockContextCasterAccessor) (Object) context).setCaster(player);
            handle(context);
        });
    }

    private void finish(String message) {
        finished = true;
        stop();
        if (message != null && !player.isRemoved()) {
            player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GOLD));
        }
    }

    public boolean isRunning() {
        return lazyProgram.ifCreated().filter(IWenyanScheduler::isRunning).isPresent();
    }

    public boolean isFinished() {
        return finished;
    }

    public String spellCode() {
        return spellCode;
    }

    public ServerPlayer player() {
        return player;
    }

    public void stop() {
        lazyProgram.ifCreated().ifPresent(IWenyanScheduler::stop);
    }

    @Override
    public void handleError(String error) {
        if (fuseTripped) {
            return;
        }
        errors.add(error);
    }

    @Override
    public String getPlatformName() {
        return player.getGameProfile().name();
    }

    @Override
    public IExecQueue getExecQueue() {
        return execQueue;
    }
}