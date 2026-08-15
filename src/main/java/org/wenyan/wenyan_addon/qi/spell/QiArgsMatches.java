package org.wenyan.wenyan_addon.qi.spell;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link QiArgsMatch} 重复标注容器。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface QiArgsMatches {
    QiArgsMatch[] value();
}
