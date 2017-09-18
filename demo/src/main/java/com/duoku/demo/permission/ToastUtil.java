package com.duoku.demo.permission;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * ========================================
 * Created by zhaokai on 2017/8/24.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class ToastUtil {

    private final static Handler handler = new Handler(Looper.getMainLooper());
    private static Toast toast;

    /**
     * 调用提示
     *
     * @param msg 消息
     */
    public static void show(final Context context, final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                } else {
                    toast.setText(msg);
                    toast.setDuration(Toast.LENGTH_SHORT);
                }
                toast.show();
            }
        });
    }
}
