package com.chenlong.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义日期的格式化类型
 * Created by Long on 2016/6/29.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Format {
    String value() default "yyyy-MM-dd";
}
