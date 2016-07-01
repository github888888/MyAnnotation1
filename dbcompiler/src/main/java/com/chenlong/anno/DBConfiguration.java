package com.chenlong.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库基本配置信息
 * Created by Long on 2016/6/29.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface DBConfiguration {
    /**
     * 数据库表名
     */
    String name();

    /**
     * 数据库起始版本
     */
    int startVersion() default 1;

    /**
     * 数据当前版本
     */
    int currentVersion();

    /**
     * 旧版本序列(数据库的所有版本号的序列 eg {1,2,3,5})
     */
    int[] oldVersionSequence();
}
