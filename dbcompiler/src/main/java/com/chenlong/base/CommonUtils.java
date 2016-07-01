package com.chenlong.base;

import com.chenlong.anno.Column;
import com.chenlong.anno.TableName;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonUtils {

    public static Date string2date(String value, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
            return format.parse(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String date2string(Date date, String pattern) {
        if (null == date)
            return null;
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }

    public static byte int2Byte(int i) {
        return (byte) i;
    }

    public static int byte2Int(byte b) {
        return b & 0xFF;
    }

    public static char int2char(int i) {
        return (char) i;
    }

    public static int char2int(char c) {
        return c;
    }

    public static int boolean2int(boolean flag) {
        return flag == false ? 0 : 1;
    }

    public static boolean int2boolean(int i) {
        return i != 0;
    }
    
    public static <T extends BaseBean> String getCreateSqlStatement(Class<T> t) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");
        TableName name = t.getAnnotation(TableName.class);
        if(name == null) {
            throw new RuntimeException("请再类的名称前面添加@TableName注解，确定对应的表名称！");
        }
        builder.append(name.value() + " ( id integer primary key autoincrement");
        Field[] fields = t.getDeclaredFields();
        for(Field item : fields) {
            Column column = item.getAnnotation(Column.class);
            if(column == null) continue;
            String sqlType = getSqliteType(item);
            if(sqlType == null || column.value() == null) continue;
            builder.append(", " + column.value() + " " + sqlType);
        }
        builder.append(" )");
        return builder.toString();
    }
    
    private static String getSqliteType(Field field) {
        Class<?> type = field.getType();
        if(type.isPrimitive()) {
            if(type == float.class || type == double.class) return "real";
            return "integer";
        } else if (type == String.class || type == Date.class) {
            return "text";
        } else if (type.isArray()) {
            Class<?> clazz = type.getComponentType();
            if(clazz == byte.class) return "blob";
            throw new RuntimeException("暂时不支持非byte[]类型的数组存储!");
        } else {
            try {
                // 最后处理包装类
                Field f = type.getDeclaredField("TYPE");
                if (f != null && ((Class<?>) f.get(null)).isPrimitive()) {
                    Class<?> clazz = (Class<?>) f.get(null);
                    if(clazz == float.class || clazz == double.class) return "real";
                    return "integer";
                }
            } catch (Exception e) {
                throw new RuntimeException("暂时不支持非String和Date对象的嵌套形式!");
            }
        }
        return null;
    }
}
