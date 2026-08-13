package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.consume.YinYangTendency;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 符咒方法标签：声明主属性标签（需要什么属性才能发挥最佳效果）与辅属性标签（兼容属性）。
 * 主属性标签为空 = 无属性倾向，执行时按输入灵气自动路由到 {@link QiBranch} 分支。
 * 属性以注册 id 引用（如 "water" / "ice"），运行时经 {@link org.wenyan.wenyan_addon.qi.element.ElementRegistry} 解析。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface QiFunction {
    /**
     * 文言函数名（施「名」调用）。
     */
    String name();

    /**
     * 函数描述，注册时写入文言函数元数据。
     */
    String description() default "";

    /**
     * 主属性标签：属性注册 id，空 = 无倾向（自适应路由）。
     */
    String[] primary() default {};

    /**
     * 辅属性标签：属性注册 id，非必须，存在时影响匹配等级。
     */
    String[] compatible() default {};

    /**
     * 基础消耗量，由文言函数自行确定数额。
     */
    double baseCost() default 0.0;

    /**
     * 阴阳倾向，参与标签匹配（匹配/不匹配/无倾向）。
     */
    YinYangTendency tendency() default YinYangTendency.NONE;
}
