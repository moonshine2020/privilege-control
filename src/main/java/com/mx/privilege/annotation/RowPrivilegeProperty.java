package com.mx.privilege.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mengxu
 * @date 2022/1/19 7:18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface RowPrivilegeProperty {

    // 常量数组
    String[] value() default  {};

    String[] range() default {};

    // 远程配置
    String[] link() default {};
}
