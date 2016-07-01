package com.chenlong.base;

import javax.lang.model.type.TypeMirror;

/**
 * Created by chenlong on 2016/6/16.
 */
public class MemberInfo {
    public String columnName; // 对应的数据库列名称
    public String name;       // 对应成员名称
    public TypeMirror type;   // 对应成员类型
    public String getMethod;  // 对应的get方法
    public String setMethod;  // 对应的set方法
    public String pattern;    // 日期格式化

    public MemberInfo(String columnName, String name, TypeMirror type) {
        this.columnName = columnName;
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "MemberInfo{" +
                "columnName='" + columnName + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", getMethod='" + getMethod + '\'' +
                ", setMethod='" + setMethod + '\'' +
                '}';
    }
}
