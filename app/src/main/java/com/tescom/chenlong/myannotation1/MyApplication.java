package com.tescom.chenlong.myannotation1;

import android.app.Application;

import com.chenlong.anno.DBConfiguration;

/**
 * Created by Long on 2016/6/29.
 */
@DBConfiguration(name="test.db",startVersion = 1, currentVersion = 3, oldVersionSequence = {1})
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.context = getApplicationContext();
        DaoImplFactory.init(new MyOpenHelper(getApplicationContext()));
    }
}
