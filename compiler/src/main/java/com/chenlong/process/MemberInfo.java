package com.chenlong.process;

import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * Created by chenlong on 2016/6/16.
 */
public class MemberInfo {
    public int id;

    public MemberInfo(int id, Name name, TypeMirror type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Name name;
    public TypeMirror type;

    @Override
    public String toString() {
        return "MemberInfo{" +
                "id=" + id +
                ", name=" + name +
                ", type=" + type +
                '}';
    }
}
