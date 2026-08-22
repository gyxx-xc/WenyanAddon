package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.request.IBaseHandleableRequest;
import indi.wenyan.judou.api.exec.structure.IHandleContext;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.runtime.IWenyanRunner;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.Consumer;

/**
 * 玩家施法请求：法术剑运行器中调用背包设备（玩家版函数包）时构造的请求。
 * 执行时忽略外部传入的上下文，自行以施法玩家构造 {@link PlayerCastContext} 注入函数
 * （法术剑队列中同时存在方块请求 {@link indi.wenyan.content.block.runner.BlockRequest}，
 * 方块请求需要 {@link indi.wenyan.content.block.runner.BlockRequest.BlockContext}，二者互不干扰）。
 */
public class PlayerCastRequest implements IBaseHandleableRequest, IArgsRequest {
    private final ServerPlayer player;
    private final IWenyanRunner thread;
    private final IWenyanValue self;
    private final List<IWenyanValue> args;
    private final RawHandlerPackage.IRawRequest request;
    private final Consumer<IWenyanValue> onReturn;

    public PlayerCastRequest(ServerPlayer player, IWenyanRunner thread, IWenyanValue self,
                             List<IWenyanValue> args, RawHandlerPackage.IRawRequest request,
                             Consumer<IWenyanValue> onReturn) {
        this.player = player;
        this.thread = thread;
        this.self = self;
        this.args = args;
        this.request = request;
        this.onReturn = onReturn;
    }

    @Override
    public boolean handle(IHandleContext context) throws WenyanException {
        return request.handle(new PlayerCastContext(player), this, onReturn);
    }

    @Override
    public IWenyanRunner thread() {
        return thread;
    }

    @Override
    public IWenyanValue self() {
        return self;
    }

    @Override
    public List<IWenyanValue> args() {
        return args;
    }
}