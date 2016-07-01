package com.chenlong.process;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Name;

/**
 * Created by chenlong on 2016/6/16.
 */
public class ClazzInfo {
    public Name clazzName; // 类名称
    public Name clazzQualiName; // 类的全引用名称
    public Name packageName; // 包名
    public int layoutid; // 布局资源id
    public int[] clickIds; // 点击的成员

    @Override
    public String toString() {
        return "ClazzInfo{" +
                "clazzName=" + clazzName +
                ", clazzQualiName=" + clazzQualiName +
                ", packageName=" + packageName +
                ", memberMap=" + memberMap +
                '}';
    }

    public ClazzInfo(Name clazzName, Name clazzQualiName, Name packageName, int layoutid, int[] clickIds) {
        this.clazzName = clazzName;
        this.clazzQualiName = clazzQualiName;
        this.packageName = packageName;
        this.layoutid = layoutid;
        this.clickIds = clickIds;
    }

    public Map<Integer, MemberInfo> memberMap = new HashMap<>();
}