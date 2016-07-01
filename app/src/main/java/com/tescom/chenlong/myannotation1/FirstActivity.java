package com.tescom.chenlong.myannotation1;

import android.view.View;
import android.widget.TextView;

import com.chenlong.anno.InjectLayout;
import com.chenlong.anno.InjectView;

@InjectLayout(R.layout.activity_first)
public class FirstActivity extends BaseActivity {
    @InjectView(R.id.tv_show2)
    public TextView tv_show2;

    @Override
    protected void initSomething() {

    }

    @Override
    public void onClick(View v) {

    }
}