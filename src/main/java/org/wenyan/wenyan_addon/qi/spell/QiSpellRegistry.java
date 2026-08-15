package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.structure.IHandleContext;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 符咒方法注册器：扫描类中带 {@link QiFunction} 或 {@link QiArgsMatch} 注解的静态方法，
 * 按文言名分组注册。
 * <p>
 * 同名方法存在 {@link QiArgsMatch} 时完全接管该函数：
 * 按 args 实际类型逐个匹配组合，命中跳转执行；全部不匹配抛异常（列出可用组合）。
 * 无 {@link QiArgsMatch} 时走灵气系统：有主属性标签 → 单方法执行（消耗随匹配动态变化）；
 * 无主属性标签 → 按输入灵气路由到带 {@link QiBranch} 注解的分支方法。
 */
public final class QiSpellRegistry {
    private QiSpellRegistry() {
    }

    public static BiFunction<BlockPos, BlockState, RawHandlerPackage> blockPackage(Class<?> holder) {
        return (bp, _) -> build(holder);
    }

    private static RawHandlerPackage build(Class<?> holder) {
        // 按文言名分组：QiArgsMatch 方法列表 + QiFunction 方法（每组至多一个）
        Map<String, List<ArgsEntry>> argsGroups = new LinkedHashMap<>();
        Map<String, QiFunctionMethod> functionMethods = new LinkedHashMap<>();

        for (Method method : holder.getDeclaredMethods()) {
            QiFunction function = method.getAnnotation(QiFunction.class);
            QiArgsMatch[] argsMatches = method.getAnnotationsByType(QiArgsMatch.class);
            if (argsMatches.length > 0) {
                // QiArgsMatch（可与 QiFunction 同注）：参数组合前置条件
                QiFunctionMethod qiFunction = function != null ? new QiFunctionMethod(function, method) : null;
                for (QiArgsMatch argsMatch : argsMatches) {
                    argsGroups.computeIfAbsent(argsMatch.name(), _ -> new ArrayList<>())
                            .add(new ArgsEntry(argsMatch, method, qiFunction));
                }
                continue;
            }
            if (function != null) {
                functionMethods.put(function.name(), new QiFunctionMethod(function, method));
            }
        }

        HandlerPackageBuilder builder = HandlerPackageBuilder.create();
        java.util.Set<String> allNames = new java.util.LinkedHashSet<>(argsGroups.keySet());
        allNames.addAll(functionMethods.keySet());
        for (String name : allNames) {
            List<ArgsEntry> argsEntries = argsGroups.getOrDefault(name, List.of());
            QiFunctionMethod functionMethod = functionMethods.get(name);
            String symbol = ChineseUtils.bracketOf(name);
            if (!argsEntries.isEmpty() && functionMethod != null) {
                // QiArgsMatch 优先；全不匹配回落到灵气系统
                builder.handler(symbol, routeArgsWithFallback(name, argsEntries, routeFunction(holder, functionMethod)));
            } else if (!argsEntries.isEmpty()) {
                builder.handler(symbol, routeArgs(name, argsEntries));
            } else {
                builder.handler(symbol, routeFunction(holder, functionMethod));
            }
        }
        return builder.build();
    }

    /**
     * QiArgsMatch 路由 + 回落：逐组合匹配，命中执行；全不匹配回落到灵气系统 handler。
     */
    private static HandlerPackageBuilder.HandlerReturnFunction routeArgsWithFallback(
            String name, List<ArgsEntry> entries, HandlerPackageBuilder.HandlerReturnFunction fallback) {
        return (ctx, request) -> {
            List<IWenyanValue> args = request.args();
            for (ArgsEntry entry : entries) {
                if (QiArgsRouter.matches(entry.annotation().value(), args)) {
                    return invokeEntry(entry, ctx, request);
                }
            }
            return fallback.handle(ctx, request);
        };
    }

    /**
     * QiArgsMatch 路由：逐组合匹配，命中执行；全部不匹配抛异常（列出可用组合）。
     */
    private static HandlerPackageBuilder.HandlerReturnFunction routeArgs(String name, List<ArgsEntry> entries) {
        return (ctx, request) -> {
            List<IWenyanValue> args = request.args();
            for (ArgsEntry entry : entries) {
                if (QiArgsRouter.matches(entry.annotation().value(), args)) {
                    return invokeEntry(entry, ctx, request);
                }
            }
            throw new WenyanException("「" + name + "」参数类型不匹配，可用组合: "
                    + entries.stream()
                            .map(entry -> QiArgsRouter.describe(entry.annotation().value()))
                            .toList());
        };
    }

    /**
     * 命中 QiArgsMatch 后执行：同注 QiFunction → 走灵气消耗流程；否则直接执行方法体。
     */
    private static IWenyanValue invokeEntry(ArgsEntry entry, IHandleContext ctx, IArgsRequest request) throws WenyanException {
        if (entry.qiFunction() != null) {
            QiFunction function = entry.qiFunction().annotation();
            List<ElementAttribute> primary = resolve(function.primary());
            List<ElementAttribute> compatible = resolve(function.compatible());
            return QiSpellExecution.execute(primary, compatible,
                    function.baseCost(), function.tendency(), toMethod(entry.method()))
                    .handle(ctx, request);
        }
        return toArgsMethod(entry.method()).handle(ctx, request);
    }

    /**
     * QiFunction（灵气系统）路由：有主属性标签单方法；无主属性标签按灵气路由分支。
     */
    private static HandlerPackageBuilder.HandlerReturnFunction routeFunction(Class<?> holder, QiFunctionMethod entry) {
        QiFunction function = entry.annotation();
        List<ElementAttribute> primary = resolve(function.primary());
        List<ElementAttribute> compatible = resolve(function.compatible());
        QiSpellMethod mainMethod = toMethod(entry.method());
        if (primary.isEmpty()) {
            return QiSpellExecution.execute(primary, compatible,
                    function.baseCost(), function.tendency(), route(holder, entry.method()));
        }
        return QiSpellExecution.execute(primary, compatible,
                function.baseCost(), function.tendency(), mainMethod);
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

    /**
     * QiArgsMatch 方法反射包装：签名 (BlockContext ctx, BlockRequest request)。
     */
    private static HandlerPackageBuilder.HandlerReturnFunction toArgsMethod(Method method) {
        return (ctx, request) -> {
            try {
                if (!method.canAccess(null)) {
                    method.setAccessible(true);
                }
                if (!(ctx instanceof BlockRequest.BlockContext context)
                        || !(request instanceof BlockRequest blockRequest)) {
                    return indi.wenyan.judou.api.values.WenyanNull.NULL;
                }
                return (IWenyanValue) method.invoke(null, context, blockRequest);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof WenyanException wenyanException) {
                    throw wenyanException;
                }
                throw new WenyanException("参数路由方法执行失败: " + e.getCause());
            } catch (IllegalAccessException | ClassCastException e) {
                throw new WenyanException("参数路由方法调用错误: " + method.getName() + " (" + e.getMessage() + ")");
            }
        };
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

    private record ArgsEntry(QiArgsMatch annotation, Method method, QiFunctionMethod qiFunction) {
    }

    private record QiFunctionMethod(QiFunction annotation, Method method) {
    }
}
