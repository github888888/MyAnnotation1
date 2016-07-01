package com.chenlong.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Name;

/**
 * Bean
 * Created by Long on 2016/6/30.
 */
public class ClazzInfo {
    public static Name packageName; // 包名
    public List<String> importList = new ArrayList<>();
    public Name clazzName; // 类名称
    public Name clazzQualiName; // 类的全引用名称
    public String tableName; // 对应的表名称
    public Map<String, MemberInfo> map = new HashMap<>();

    {
        importList.add("import java.io.Serializable;");
        importList.add("import java.util.ArrayList;");
        importList.add("import java.util.List;");
        importList.add("import com.chenlong.base.CommonUtils;");
        importList.add("import com.chenlong.base.DaoImpl;");
        importList.add("import " + packageName + ".DaoImplFactory;");
        importList.add("import android.content.ContentValues;");
        importList.add("import android.database.Cursor;");
        importList.add("import android.database.sqlite.SQLiteDatabase;");
        importList.add("import android.text.TextUtils;");
    }

    @Override
    public String toString() {
        return "ClazzInfo{" +
                "importList=" + importList +
                ", clazzName=" + clazzName +
                ", clazzQualiName=" + clazzQualiName +
                ", tableName='" + tableName + '\'' +
                ", map=" + map +
                '}';
    }

    public ClazzInfo(Name clazzName, Name clazzQualiName, String tableName) {
        this.clazzName = clazzName;
        this.clazzQualiName = clazzQualiName;
        this.tableName = tableName;
        // 添加需要处理的类
        importList.add("import " + clazzQualiName + ";");
    }
}
