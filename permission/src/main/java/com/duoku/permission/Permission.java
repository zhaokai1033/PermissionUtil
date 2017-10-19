package com.duoku.permission;

/**
 * ========================================
 * Created by zhaokai on 2017/10/19.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class Permission {
    public String title;
    public String message;
    public String name;
    public boolean isPass;

    public Permission() {
    }

    public Permission(String title, String message, String name, boolean isPass) {
        this.title = title;
        this.message = message;
        this.name = name;
        this.isPass = isPass;
    }
}
