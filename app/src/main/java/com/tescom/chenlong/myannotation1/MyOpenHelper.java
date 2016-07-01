package com.tescom.chenlong.myannotation1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.chenlong.base.CommonUtils;

/**
 * Created by Long on 2016/6/29.
 */
public class MyOpenHelper extends AbstractOpenHelper {
    public static final String TABLE_NEWS_NAME = "news"; // 表名
    public static final String TABLE_NEWS_TITLE = "title";
    public static final String TABLE_NEW_SUMMARY = "summary";
    public static final String TABLE_NEW_DATE = "date";
    public static final String TABLE_NEW_SCORE1 = "score1";
    public static final String TABLE_NEW_SCORE2 = "score2";

    public static final String TABLE_USER_NAME = "user"; // 表名
    public static final String TABLE_USER_BYTE1 = "byte1";
    public static final String TABLE_USER_BYTER1 = "byter1";
    public static final String TABLE_USER_SHORT1 = "short1";
    public static final String TABLE_USER_SHORTER1 = "shorter1";
    public static final String TABLE_USER_INT1 = "int1";
    public static final String TABLE_USER_INTEGER1 = "inter1";
    public static final String TABLE_USER_LONG1 = "long1";
    public static final String TABLE_USER_LONGER1 = "longer1";
    public static final String TABLE_USER_STRING1 = "string1";
    public static final String TABLE_USER_CH1 = "ch1";
    public static final String TABLE_USER_CHARACTER1 = "character1";
    public static final String TABLE_USER_FLAG1 = "flag1";
    public static final String TABLE_USER_FLAGER1 = "flager1";
    public static final String TABLE_USER_BYTES = "bytes";
    public static final String TABLE_USER_FLOAT1 = "float1";
    public static final String TABLE_USER_FLOATER1 = "floater1";
    public static final String TABLE_USER_DOUBLE1 = "double1";
    public static final String TABLE_USER_DOUBLER1 = "doubler1";
    public static final String TABLE_USER_DATE = "date";

    public MyOpenHelper(Context context) {
        super(context);
    }

    @Override
    public void init(SQLiteDatabase db) {
        db.execSQL(CommonUtils.getCreateSqlStatement(News.class));
    }

    @Override
    public void old_version_1_upgrade(SQLiteDatabase db) {
        db.execSQL(CommonUtils.getCreateSqlStatement(User.class));
    }

}
