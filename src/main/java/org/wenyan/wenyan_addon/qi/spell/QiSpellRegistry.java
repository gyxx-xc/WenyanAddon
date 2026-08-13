package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.qi.consume.YinYangTendency;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 符咒方法注册器：扫描类中带 {@link QiFunction} 注解的静态方法，
 * 注册为文言设备函数（施「名」调用）。
 * 有主属性标签 → 单方法执行（消耗随匹配动态变化）；
 * 无主属性标签 → 按输入灵气路由到带 {@link QiBranch} 注解的分支方法。
 * 注解中的属性 id 经 {@link ElementRegistry} 运行时解析。
 */
public final class QiSpellRegistry {
    private QiSpellRegistry() {
    }

    public static BiFunction<BlockPos, BlockState, RawHandlerPackage> blockPackage(Class<?> holder) {
        return (bp, _) -> build(holder);
    }

    private static RawHandlerPackage build(Class<?> holder) {
        HandlerPackageBuilder builder = HandlerPackageBuilder.create();
        for (Method method : holder.getDeclaredMethods()) {
            QiFunction function = method.getAnnotation(QiFunction.class);
            if (function == null) {
                continue;
            }
            List<ElementAttribute> primary = resolve(function.primary());
            List<ElementAttribute> compatible = resolve(function.compatible());
            QiSpellMethod mainMethod = toMethod(method);
            if (!function.description().isEmpty()) {
                builder.description(function.description());
            }
            String symbol = ChineseUtils.bracketOf(function.name());
            if (primary.isEmpty()) {
                builder.handler(symbol, QiSpellExecution.execute(primary, compatible,
                        function.baseCost(), function.tendency(), route(holder, method)));
            } else {
                builder.handler(symbol, QiSpellExecution.execute(primary, compatible,
                        function.baseCost(), function.tendency(), mainMethod));
            }
        }
        return builder.build();
    }

    private static List<ElementAttribute> resolve(String[] ids) {
        List<ElementAttribute> result = new ArrayList<>();
        for (String id : ids) {
            ElementAttribute attribute = ElementRegistry.byId(id);
            if (attribute == null) {
                throw new IllegalArgumentException("未知元素属性 id: " + id);
            }
            result.add(attribute);
        }
        return result;
    }

    private static QiSpellMethod route(Class<?> holder, Method mainMethod) {
        List<QiSpellRouter.Branch> branches = new ArrayList<>();
        for (Method branchMethod : holder.getDeclaredMethods()) {
            QiBranch branch = branchMethod.getAnnotation(QiBranch.class);
            if (branch == null) {
                continue;
            }
            if (!branchMethod.getName().startsWith(mainMethod.getName() + "_")) {
                continue;
            }
            branches.add(new QiSpellRouter.Branch(Set.copyOf(resolve(branch.forPrimary())), toMethod(branchMethod)));
        }
        return QiSpellRouter.route(branches, toMethod(mainMethod));
    }

    private static QiSpellMethod toMethod(Method method) {
        return (ctx, request, match) -> {
            try {
                if (!method.canAccess(null)) {
                    method.setAccessible(true);
                }
                IWenyanValue result = (IWenyanValue) method.invoke(null, ctx, request, match);
                return result;
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof WenyanException wenyanException) {
                    throw wenyanException;
                }
                throw new WenyanException("符咒方法执行失败: " + e.getCause());
            } catch (IllegalAccessException | ClassCastException e) {
                throw new WenyanException("符咒方法调用错误: " + method.getName() + " (" + e.getMessage() + ")");
            }
        };
    }
}
