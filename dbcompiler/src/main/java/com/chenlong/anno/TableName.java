package com.chenlong.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 制定了将实体与数据库表中的对应关系
 * Created by Long on 2016/6/29.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
    /**
     * 数据库中的表�?
     * @return
     */
    String value();
}