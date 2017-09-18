package com.duoku.demo.permission;

import android.app.Application;

import com.duoku.permission.PermissionUtil;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        PermissionUtil.init(this);
    }
}
