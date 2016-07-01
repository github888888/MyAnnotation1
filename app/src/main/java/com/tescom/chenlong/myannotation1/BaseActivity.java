package com.tescom.chenlong.myannotation1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


/**
 * Created by chenlong on 2016/6/8.
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{
    /**
     * 部分数据功能初始化
     */
    protected abstract void initSomething();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注入布局/视图
        InjectFactory.inject(this);
        // 回调两个方法
        initSomething();
    }

    @Override
    public void onClick(View v) {}
}