package org.wenyan.wenyan_addon;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.NonNull;

import static org.wenyan.wenyan_addon.WenyanAddon.MODID;

/**
 * 方块能力注册中心。
 * <p>
 * 本类继承自 {@code enum} 是为了利用 JVM 的单例类加载机制，
 * 确保 Neoforge 的事件总线能正确订阅其静态方法。
 * 此类不包含任何枚举常量（空枚举体 {@code ;}），仅作为订阅事件的载体。
 * </p>
 * <p>
 * 每当 Neoforge 触发 {@link RegisterCapabilitiesEvent} 事件时，
 * {@link #registerCapabilities(RegisterCapabilitiesEvent)} 会被调用，
 * 将文言编程语言的「包」（{@link RawHandlerPackage}，即一组文言函数/处理器）
 * 绑定到特定的 Minecraft 方块上，
 * 使得这些方块成为可被文言脚本操纵的「文言方块设备」。
 * </p>
 *
 * @see RegisterCapabilitiesEvent Neoforge 的能力注册事件
 * @see IWenyanBlockDevice 文言方块设备接口
 * @see RawHandlerPackage 文言函数包
 */
@EventBusSubscriber(modid = MODID)
public enum Capabilities {
    ;

    /**
     * 向 Neoforge 注册本模组的能力。
     * <p>
     * 此方法由 Neoforge 事件总线在模组初始化阶段自动调用。
     * 它将文言函数包注册到本模组和原版的方块上。
     * </p>
     *
     * <h3>注册的文言函数包(示范)</h3>
     * <table border="1">
     *   <tr><th>包名</th><td>「crush game」</td></tr>
     *   <tr><th>函数名</th><td>「crush」</td></tr>
     *   <tr><th>函数行为</th><td>调用时抛出 {@link NullPointerException}</td></tr>
     * </table>
     *
     * <h3>调用时机</h3>
     * 此方法由 Neoforge 事件总线自动调用，不需要也不应该手动调用。
     *
     * @param event Neoforge 的能力注册事件对象，由事件总线注入。参数非空（标记有 {@link NonNull}）
     *
     * @see WyRegistration#WENYAN_BLOCK_DEVICE_CAPABILITY 文言方块设备能力键
     * @see #simpleDevice(String, RawHandlerPackage) 用于创建设备提供者
     * @see HandlerPackageBuilder 用于构建 {@link RawHandlerPackage}
     */
    @SubscribeEvent
    public static void registerCapabilities(@NonNull RegisterCapabilitiesEvent event) {
        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                simpleDevice("crush game", HandlerPackageBuilder.create() // package name
                        .handler(ChineseUtils.bracketOf("crush"), // function name
                                _ -> { // function content
                                    throw new NullPointerException();
                                })
                        .build()),
                WenyanAddon.EXAMPLE_BLOCK.get(),
                Blocks.BEDROCK
        );
    }

    /**
     * 创建一个简化的文言方块设备能力提供者。
     * <p>
     * 此方法是工厂方法，根据给定的包名和处理器包，
     * 创建并返回一个实现了 {@link IBlockCapabilityProvider} 接口的匿名对象。
     * 该提供者随后可用于
     * {@link RegisterCapabilitiesEvent#registerBlock} 方法中，
     * 将文言函数包绑定到方块上。
     * </p>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * // 创建一个名为 "my device" 的设备，包含一个 "say" 函数
     * IBlockCapabilityProvider<IWenyanBlockDevice, Void> provider = simpleDevice(
     *     "my device",
     *     HandlerPackageBuilder.create()
     *         .handler(ChineseUtils.bracketOf("say"), ctx -> {
     *             System.out.println("Hello from wenyan!");
     *         })
     *         .build()
     * );
     *
     * // 注册到某个方块上
     * event.registerBlock(
     *     WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
     *     provider,
     *     MY_BLOCK.get()
     * );
     * }</pre>
     *
     * @param name 设备名/包名（未加书名号）。调用 {@code getPackageName()} 时会自动通过
     *             {@link ChineseUtils#bracketOf(String)} 添加文言风格的书名号「」包裹
     * @param handlerPackage 该设备所包含的文言函数处理器集合（{@link RawHandlerPackage}），
     *                       通常通过 {@link HandlerPackageBuilder} 构建。
     *                       每个 handler 对应一个可在文言脚本中调用的函数
     * @return 一个 {@link IBlockCapabilityProvider} 实现，可直接用于
     *         {@link RegisterCapabilitiesEvent#registerBlock} 注册方块能力
     *
     * @see IBlockCapabilityProvider Neoforge 的方块能力提供者接口
     * @see IWenyanBlockDevice 文言方块设备接口
     * @see HandlerPackageBuilder 处理器包构建器
     * @see ChineseUtils#bracketOf(String) 给文言名称添加书名号
     */
    public static IBlockCapabilityProvider<IWenyanBlockDevice, Void> simpleDevice(String name, RawHandlerPackage handlerPackage) {
        return (_, p, s, _, _) -> new IWenyanBlockDevice() {
            @Override
            public BlockState blockState() {
                return s;
            }

            @Override
            public BlockPos blockPos() {
                return p;
            }

            @Override
            public boolean isRemoved() {
                return false;
            }

            @Override
            public RawHandlerPackage getExecPackage() {
                return handlerPackage;
            }

            @Override
            public String getPackageName() {
                return ChineseUtils.bracketOf(name);
            }
        };
    }
}
