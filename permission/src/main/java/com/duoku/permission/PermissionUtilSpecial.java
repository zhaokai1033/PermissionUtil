package com.duoku.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class PermissionUtilSpecial {

    public static void request(int requestCode, Activity activity, Fragment fragment, OnPermissionCallBack permissionCallBack, String permission) {

        if (Manifest.permission.WRITE_SETTINGS.equals(permission)) {
            String packageName;
            if (activity != null) {
                packageName = activity.getPackageName();
            } else if (fragment != null) {
                packageName = fragment.getActivity().getPackageName();
            } else {
                permissionCallBack.onError("'Activity' or 'Fragment' is null");
                return;
            }
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + packageName));
                if (activity != null) {
                    activity.startActivityForResult(intent, requestCode);
                } else {
                    fragment.startActivityForResult(intent, requestCode);
                }
            }
        }
    }

    public static boolean isSpecial(String mPermissionName) {
        for (int i = 0; i < Constants.SPECIAL_PERMISSION.length; i++) {
            if (Constants.SPECIAL_PERMISSION[i].equals(mPermissionName)) {
                return true;
            }
        }
        return false;
    }
}
