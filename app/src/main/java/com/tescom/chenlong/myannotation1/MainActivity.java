package com.tescom.chenlong.myannotation1;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chenlong.anno.ClickView;
import com.chenlong.anno.InjectLayout;
import com.chenlong.anno.InjectView;
import com.chenlong.base.DaoImpl;

import java.util.Date;
import java.util.List;

@InjectLayout(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    @InjectView(R.id.bt_add)
    public Button bt_add;
    @InjectView(R.id.bt_delete)
    public Button bt_delete;
    @InjectView(R.id.bt_update)
    public Button bt_update;
    @InjectView(R.id.bt_list)
    public Button bt_list;
    @InjectView(R.id.bt_list_by_condition)
    public Button bt_list_by_condition;
    @InjectView(R.id.tv_content)
    public TextView tv_content;

    private DaoImpl<User> impl;


    @Override
    protected void initSomething() {
        impl = DaoImplFactory.getDaoImpl(User.class);
    }

    @Override
    @ClickView({R.id.bt_add, R.id.bt_delete, R.id.bt_update, R.id.bt_list, R.id.bt_list_by_condition})
    public void onClick(View v) {
        if(v == bt_add) {
            User user1 = new User();
            user1.short1 = 123;
            user1.shorter1 = 321;


            User user2 = new User();
            user2.short1 = 456;
            user2.shorter1 = 654;

            long insert;
            insert = impl.insert(user1);
            tv_content.setText("" + insert);
            insert = impl.insert(user2);
            tv_content.setText("" + insert);
        } else if (v == bt_delete) {
            int delete = impl.deleteByCondition(" short1 = ? and shorter1 = ? ", new String[]{"123", "321"});
            ToastUtils.showToast("delete " + delete);
            List<User> findAll = impl.findAll();
            tv_content.setText(findAll.toString());
        } else if (v == bt_update) {
            User user2 = new User();
            user2.id = 2;
            user2.short1 = 789;
            user2.shorter1 = 987;

            impl.update(user2);
            List<User> findAll = impl.findAll();
            tv_content.setText(findAll.toString());
        } else if (v == bt_list) {
            List<User> findAll = impl.findAll();
            tv_content.setText(findAll.toString());
        } else if (v == bt_list_by_condition) {
            List<User> findByCondition = impl.findByCondition(" short1 = ? and shorter1 = ? ", new String[]{"456", "654"}, null);
            tv_content.setText(findByCondition.toString());
        }
    }
}