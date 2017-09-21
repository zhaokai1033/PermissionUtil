package com.duoku.permission;

import java.util.ArrayList;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public abstract class OnRequestPermissionCallBack implements OnPermissionCallBack {
    @Override
    public abstract void onAllowedWitOutSpecial();

    @Override
    public abstract void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip);

    @Override
    public abstract void onUnSupport(int requestCode, String[] permissions);

    @Override
    public boolean onRequireFail(String[] permissions) {
        return false;
    }

    @Override
    public void onResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void onCheck(boolean[] booleans) {

    }

    @Override
    public void onError(String msg) {

    }

    @Override
    public final void onFinish() {

    }
}
