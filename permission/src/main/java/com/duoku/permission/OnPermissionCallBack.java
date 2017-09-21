package com.duoku.permission;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * ================================================
 * Created by zhaokai on 2017/5/12.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

public interface OnPermissionCallBack {
    /**
     * 除特殊权限外所有权限被允许
     */
    void onAllowedWitOutSpecial();

    /**
     * 有权限被拒绝
     *
     * @param permissions  被拒绝的权限
     * @param isCanShowTip 是否可以显示提示内容
     */
    void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip);

    /**
     * 未处理过的结果
     */
    void onResult(int requestCode, String permissions[], int[] grantResults);

    /**
     * 不支持统一请求的特殊权限
     *
     * @param requestCode 请求码
     * @param permissions 特殊权限
     */
    void onUnSupport(int requestCode, String permissions[]);

    /**
     * 检查回调
     *
     * @param booleans 检查结果
     */
    void onCheck(boolean[] booleans);

    /**
     * 请求错误
     *
     * @param msg 错误信息
     */
    void onError(String msg);

    /**
     * 当前权限是否是必须的
     *
     * @param permission 权限名称
     * @return 是否必须、如必须 申请不通过退出
     */
    boolean isRequired(String permission);

    /**
     * 权限请求失败 有强制权限未通过
     * @return false 默认处理 / true 已处理
     */
    boolean onRequireFail(String[] permissions);

    /**
     * 结束
     */
    void onFinish();
}
