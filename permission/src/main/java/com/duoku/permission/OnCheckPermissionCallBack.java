package com.duoku.permission;

import java.util.ArrayList;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public abstract class OnCheckPermissionCallBack implements OnPermissionCallBack {
    @Override
    public void onAllowed() {

    }

    @Override
    public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {

    }

    @Override
    public void onResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public abstract void onCheck(boolean[] booleans);

    @Override
    public void onError(String msg) {

    }

    @Override
    public final void onFinish() {

    }

    @Override
    public final void onUnSupport(int requestCode, String[] permissions) {

    }
}
