package com.duoku.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ================================================
 * Created by zhaokai on 2017/5/12.
 * Email zhaokai1033@126.com
 * Describe : 权限请求工具类
 * ================================================
 */
public class PermissionUtil {

    public static PermissionObject with(AppCompatActivity activity) {
        return new PermissionObject(activity);
    }

    public static PermissionObject with(Fragment fragment) {
        return new PermissionObject(fragment);
    }

    public static void init(Context context) {
        if (context instanceof Activity) {
            mActivity = new WeakReference<>(((Activity) context));
        }
        mApplication = new WeakReference<>(context.getApplicationContext());
    }

    private static WeakReference<Context> mApplication;
    private static WeakReference<Activity> mActivity;


    public static Context getContext() {
        if (mActivity != null && mActivity.get() != null) return mActivity.get();
        return mApplication.get();
    }

    public static void check(OnPermissionCallBack onPermissionCallBack, String[] permissionName) {
        TranslucentActivity.create(10, Constants.ACTION_CHECK, permissionName, onPermissionCallBack);
    }

    public static void createRequest(int requestCode, OnPermissionCallBack onPermissionCallBack, String[] permissionNames) {
        TranslucentActivity.create(requestCode, Constants.ACTION_REQUEST, permissionNames, onPermissionCallBack);
    }

    public static void createRequestSpecial(int requestCode, OnPermissionCallBack onPermissionCallBack, String writeSettings) {
        TranslucentActivity.create(requestCode, Constants.ACTION_SPECIAL, new String[]{writeSettings}, onPermissionCallBack);
    }

    public static class PermissionObject {

        private static SparseArray<PermissionRequestObject> objectSparseArray = new SparseArray<>();
        private AppCompatActivity mActivity;
        private Fragment mFragment;

        PermissionObject(AppCompatActivity activity) {
            mActivity = activity;
        }

        PermissionObject(Fragment fragment) {
            mFragment = fragment;
        }


        public boolean[] check(String... permissionName) {
            boolean[] permissionCheck = new boolean[permissionName.length];
            for (int i = 0; i < permissionName.length; i++) {
                if (mActivity != null) {
                    permissionCheck[i] = ContextCompat.checkSelfPermission(mActivity, permissionName[i]) == PackageManager.PERMISSION_GRANTED;
                } else if (mFragment != null) {
                    permissionCheck[i] = ContextCompat.checkSelfPermission(mFragment.getContext(), permissionName[i]) == PackageManager.PERMISSION_GRANTED;
                } else {
                    throw new IllegalArgumentException("'mActivity' or 'mFragment' is null");
                }
            }
            return permissionCheck;
        }

        /**
         * @param onPermissionCallBack 请求权限的结果
         * @param permissionNames      多个权限名
         * @return 请求体
         */
        public PermissionRequestObject createRequest(int requestCode, final OnPermissionCallBack onPermissionCallBack, String[] permissionNames) {
            final PermissionRequestObject permissionRequestObject;
            if (mActivity != null) {
                permissionRequestObject = new PermissionRequestObject(requestCode, mActivity, permissionNames, onPermissionCallBack);
            } else if (mFragment != null) {
                permissionRequestObject = new PermissionRequestObject(requestCode, mFragment, permissionNames, onPermissionCallBack);
            } else {
                throw new IllegalArgumentException("'mActivity' or 'mFragment' is null");
            }
            permissionRequestObject.setPermissionObject(this);
            objectSparseArray.put(requestCode, permissionRequestObject);
            return permissionRequestObject;
        }

        /**
         * @param requestCode  请求码
         * @param permissions  请求权限
         * @param grantResults 权限结果
         */
        public boolean onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            PermissionRequestObject permissionRequestObject = objectSparseArray.get(requestCode);
            if (permissionRequestObject != null) {
                permissionRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return true;
            }

            return false;
        }
    }

    public static class PermissionRequestObject {

        private AppCompatActivity mActivity;
        private Fragment mFragment;
        private String[] mPermissionNames;
        private HashMap<String, SinglePermission> mPermissionsWeDoNotHave = new HashMap<>();
        private HashMap<String, SinglePermission> mPermissionsSpecial = new HashMap<>();
        private int mRequestCode;
        private OnPermissionCallBack mPermissionCallBack;
        private PermissionObject mPermissionObject;

        private PermissionRequestObject(int requestCode, AppCompatActivity activity, String[] permissionNames, OnPermissionCallBack onPermissionCallBack) {
            mActivity = activity;
            mPermissionNames = permissionNames;
            mRequestCode = requestCode;
            this.mPermissionCallBack = onPermissionCallBack;
        }

        private PermissionRequestObject(int requestCode, Fragment fragment, String[] permissionNames, OnPermissionCallBack onPermissionCallBack) {
            mFragment = fragment;
            mPermissionNames = permissionNames;
            mRequestCode = requestCode;
            this.mPermissionCallBack = onPermissionCallBack;
        }

        /**
         * Execute the permission request with the given Request Code
         */
        public void request() {
            //第一次请求 初始化权限列表
            if (mPermissionsSpecial.size() == 0 && mPermissionsWeDoNotHave.size() == 0) {
                for (String mPermissionName : mPermissionNames) {
                    if (PermissionUtilSpecial.isSpecial(mPermissionName)) {
                        mPermissionsSpecial.put(mPermissionName, new SinglePermission(mPermissionName));
                    } else {
                        mPermissionsWeDoNotHave.put(mPermissionName, new SinglePermission(mPermissionName));
                    }
                }
                //判断是否有需要请求的
                if (needToAsk()) {
                    //有需要请求的
                    if (mActivity != null) {
                        ActivityCompat.requestPermissions(mActivity, mPermissionNames, mRequestCode);
                    } else if (mFragment != null) {
                        mFragment.requestPermissions(mPermissionNames, mRequestCode);
                    } else {
                        throw new IllegalArgumentException("'mActivity' or 'mFragment' is null");
                    }
                } else {
//                    if (mPermissionsSpecial.size() <= 0) {
                    //已经全部通过
                    if (mPermissionCallBack != null) {
                        mPermissionCallBack.onAllowedWitOutSpecial();
                    }
//                    } else {
                    //有特殊权限
                    String[] sp = new String[mPermissionsSpecial.size()];
                    int i = 0;
                    //查找未请求的
                    for (String permission : mPermissionsSpecial.keySet()) {
                        //TODO 特殊权限 整合未完成
//                            //还未请求
//                            if (!mPermissionsSpecial.get(permission).isRequest()) {
//                                requestSpecial(permission);
//                                return;
//                            }
                        sp[i] = permission;
                        i++;
                    }
                    //特殊权限的暂不请求直接返回
                    if (mPermissionCallBack != null) {
                        mPermissionCallBack.onUnSupport(mRequestCode, sp);
                    }
                    if (mPermissionCallBack != null) {
                        mPermissionCallBack.onFinish();
                    }
//                    }
                }
            }

        }

        private void requestSpecial(String permission) {
            if (!TextUtils.isEmpty(permission)) {
                createRequestSpecial(mRequestCode, mPermissionCallBack, permission);
            }
        }

        private boolean needToAsk() {
            HashMap<String, SinglePermission> neededPermissions = new HashMap<>(mPermissionsWeDoNotHave);
            int i = 0;
            for (String permission : mPermissionsWeDoNotHave.keySet()) {

//            for (int i = 0; i < mPermissionsWeDoNotHave.size(); i++) {
                SinglePermission perm = mPermissionsWeDoNotHave.get(permission);
                int checkRes;
                if (mActivity != null) {
                    checkRes = ContextCompat.checkSelfPermission(mActivity, perm.getPermissionName());
                } else if (mFragment != null) {
                    checkRes = ContextCompat.checkSelfPermission(mFragment.getContext(), perm.getPermissionName());
                } else {
                    throw new IllegalArgumentException("'mActivity' or 'mFragment' is null");
                }
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm.getPermissionName());
                }
                i++;
            }
            mPermissionsWeDoNotHave = neededPermissions;
            mPermissionNames = new String[mPermissionsWeDoNotHave.size()];
            i = 0;
            for (String permissionName : mPermissionsWeDoNotHave.keySet()) {
                mPermissionNames[i] = permissionName;
                i++;
            }
            return mPermissionsWeDoNotHave.size() != 0;
        }

        /**
         * This Method should be called from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} with all the same incoming operands
         * <pre>
         * {@code
         *
         * public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         *      if (mStoragePermissionRequest != null)
         *          mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions,grantResults);
         * }
         * }
         * </pre>
         */
        private void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            if (mRequestCode == requestCode) {
                //所有请求的回调
                if (mPermissionCallBack != null) {
                    mPermissionCallBack.onResult(requestCode, permissions, grantResults);
                }
//                //有特殊权限
//                if (mPermissionsSpecial.size() > 0) {
//                    //当前申请列表是一个 则是特殊申请
//                    if (permissions.length == 1 && PermissionUtilSpecial.isSpecial(permissions[0])) {
//                        String per = null;
//                        for (SinglePermission p : mPermissionsSpecial.values()) {
//                            //尚未申请
//                            if (!p.isRequest()) {
//                                //于当前返回的一致
//                                if (p.getPermissionName().equals(permissions[0])) {
//                                    p.setRequest(true);
//                                }
//                                //不一致 且未申请
//                                else {
//                                    per = p.getPermissionName();
//                                }
//                            }
//                        }
//                        // 有未申请的
//                        if (!TextUtils.isEmpty(per)) {
//                            PermissionUtilSpecial.request(mRequestCode, mActivity, mFragment, mPermissionCallBack, per);
//                            return;
//                        }
//                    }
                //有特殊权限时 普通权限申请回调
//                    else {
                //记录普通权限的申请状态
                if (permissions.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        SinglePermission per = mPermissionsWeDoNotHave.get(permissions[i]);
                        if (per != null && grantResults.length > i) {
                            per.setGrant(grantResults[i]);
                        }
                    }
                }
//
//                        String per = null;
//                        for (SinglePermission p : mPermissionsSpecial.values()) {
//                            //尚未申请
//                            if (!p.isRequest()) {
//                                //于当前返回的一致
//                                if (p.getPermissionName().equals(permissions[0])) {
//                                    p.setRequest(true);
//                                }
//                                //不一致 且未申请
//                                else {
//                                    per = p.getPermissionName();
//                                }
//                            }
//                        }
//
//                        // 有未申请的
//                        if (!TextUtils.isEmpty(per)) {
//                            requestSpecial(per);
//                            return;
//                        }
//                    }
//                }
                ArrayList<String> permissionNames = new ArrayList<>();
                ArrayList<Boolean> canShowTips = new ArrayList<>();
                //有特殊权限的
//                if (mPermissionsSpecial.size() > 0) {
//                    for (String per : mPermissionsWeDoNotHave.keySet()) {
//                        permissionNames.add(per);
//                        canShowTips.add(mPermissionsWeDoNotHave.get(per).isRationalNeeded());
//                    }
//                }
                //正常权限列表
//                else {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        permissionNames.add(permissions[i]);
                        boolean shouldShowRequestPermissionRationale;
                        if (mActivity != null) {
                            shouldShowRequestPermissionRationale =
                                    ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[i]);
                        } else if (mFragment != null) {
                            shouldShowRequestPermissionRationale = mFragment.shouldShowRequestPermissionRationale(permissions[i]);
                        } else {
                            throw new IllegalArgumentException("'mActivity' or 'mFragment' is null");
                        }
                        canShowTips.add(shouldShowRequestPermissionRationale);
                    }
                }
//                }
                ArrayList<String> requiredPermissionsRetry = new ArrayList<>();
                ArrayList<String> requiredFailed = new ArrayList<>();
                String p;
                for (int i = 0; i < permissionNames.size(); i++) {
                    p = permissionNames.get(i);
                    if (mPermissionCallBack.isRequired(p)) {
                        //必须权限未允许 可提示
                        if (canShowTips.get(i)) {
                            requiredPermissionsRetry.add(p);
                        }
                        //必须权限未允许且不可再提示
                        else {
                            //默认处理
                            if (!mPermissionCallBack.onRequireFail(requiredPermissionsRetry.toArray(new String[requiredPermissionsRetry.size()]))) {
                                getAppDetailSetting(getContext());
                                killSelf();
                            } else {
                                requiredFailed.add(p);
                            }
                        }
                    }
                }

                if (requiredPermissionsRetry.size() > 0) {
                    createRequest(mRequestCode, mPermissionCallBack, requiredPermissionsRetry.toArray(new String[requiredPermissionsRetry.size()]));
                    return;
                }


                //结尾处理
                if (mPermissionCallBack != null) {
                    //有被拒绝的
                    if (requiredFailed.size() > 0) {
                        mPermissionCallBack.onRequireFail(requiredFailed.toArray(new String[requiredFailed.size()]));
                    } else if (permissionNames.size() > 0) {
                        mPermissionCallBack.onRefused(permissionNames, canShowTips);
                    } else {//全部成功
                        mPermissionCallBack.onAllowedWitOutSpecial();
                    }

                    if (mPermissionsSpecial.size() > 0) {
                        mPermissionCallBack.onUnSupport(requestCode, mPermissionsSpecial.keySet().toArray(new String[mPermissionsSpecial.size()]));
                    }
                    mPermissionCallBack.onFinish();
                }
                if (mPermissionObject != null && PermissionObject.objectSparseArray != null) {
                    PermissionObject.objectSparseArray.remove(requestCode);
                }

            }
        }

        void setPermissionObject(PermissionObject permissionObject) {
            this.mPermissionObject = permissionObject;
        }
    }

    /**
     * 杀死程序
     */
    private static void killSelf() {
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    private static void getAppDetailSetting(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }

}
