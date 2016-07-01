package com.tescom.chenlong.myannotation1;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static Context context;
    public static void showToast(String str) {
        if(null == context) return;
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
}
