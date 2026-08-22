package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.judou.api.values.IWenyanValue;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 参数检测注解：声明同名文言函数的一组参数类型（IWenyanValue 实现类，按 {@code type().tClass} 判定）。
 * 可重复标注实现多组合（如 (str,int) 与 (int,bool) 各一个注解）。
 * 传入参数时按 args 实际类型匹配组合，命中则跳转该方法执行；
 * 存在本注解的方法优先于灵气系统路由；全部组合不匹配时抛异常（列出可用组合）。
 * 方法签名：IWenyanValue 方法名(ContextType ctx, IArgsRequest request[, QiSpellContext context])
 * ContextType 为设备上下文类型（方块：BlockRequest.BlockContext；投掷物品：ThrowEntityContext；
 * 玩家施法：PlayerCastContext）。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
@Repeatable(QiArgsMatches.class)
public @interface QiArgsMatch {
    /**
     * 文言函数名（施「名」调用，与 {@link QiFunction#name()} 同名）。
     */
    String name();

    /**
     * 一组参数类型（IWenyanValue 实现类），按声明顺序与 args 逐项比对。
     */
    Class<? extends IWenyanValue>[] value() default {};
}
