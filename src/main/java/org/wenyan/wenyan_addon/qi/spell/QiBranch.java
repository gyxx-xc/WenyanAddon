package org.wenyan.wenyan_addon.qi.spell;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 无倾向符咒方法的子分支：按输入灵气属性组合自动路由。
 * 分支方法命名约定：主方法 Java 名 + "_" + 任意后缀，且第一个参数上下文类型与主方法一致。
 * 属性以注册 id 引用（如 "water" / "ice"）。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface QiBranch {
    /**
     * 匹配的属性组合（输入灵气需包含全部），属性注册 id。
     */
    String[] forPrimary() default {};

    /**
     * 预留匹配条件：exact / contains / generated / restricted。
     */
    String when() default "";
}
