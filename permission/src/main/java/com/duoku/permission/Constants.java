package com.duoku.permission;

import android.Manifest;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class Constants {
    public static final int CODE_SPECIAL = 11000;
    public static final String REQUEST_CODE = "requestCode";
    public static final String REQUEST_CODE_RETRY = "requestCodeRetry";
    public static final String PERMISSIONS = "permission";
    public static final String MESSAGE = "message";
    public static final String TITLE = "title";
    public static final String ACTION = "action";
    public static final String ACTION_SPECIAL = "action_special";
    public static final String ACTION_REQUEST = "action_request";
    public static final String ACTION_REQUEST_RETRY = "action_request_retry";
    public static final String ACTION_CHECK = "action_check";
    public static final String ACTION_APPLY = "action_apply";
    public static final String ACTION_DIALOG = "action_dialog";
    public static final String[] SPECIAL_PERMISSION = new String[]{
            Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.WRITE_SETTINGS
    };
}
