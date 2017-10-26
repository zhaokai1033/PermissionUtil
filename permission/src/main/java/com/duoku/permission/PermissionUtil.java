package com.duoku.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    public static void init(Context context,OnPermissionRefused onRefused) {
        if (context instanceof Activity) {
            mActivity = new WeakReference<>(((Activity) context));
        }
        mApplication = new WeakReference<>(context.getApplicationContext());
        if (onRefused != null)
            mOnRefused = new WeakReference<>(onRefused);
    }
    private static WeakReference<OnPermissionRefused> mOnRefused;
    private static WeakReference<Context> mApplication;
    private static WeakReference<Activity> mActivity;


    public static Context getContext() {
        if (mActivity != null && mActivity.get() != null) return mActivity.get();
        return mApplication.get();
    }

    public static void check(int requestCode,OnPermissionCallBack onPermissionCallBack, Permission... permissions) {
        TranslucentActivity.create(requestCode, Constants.ACTION_CHECK, getPermissionNames(permissions), onPermissionCallBack);
    }

    public static void createRequest(int requestCode, OnPermissionCallBack onPermissionCallBack, Permission... permissions) {
        TranslucentActivity.create(requestCode, Constants.ACTION_REQUEST, getPermissionNames(permissions), onPermissionCallBack);
    }

    public static void createRequestRetry(int requestCode, OnPermissionCallBack onPermissionCallBack, Permission... permissions) {
        TranslucentActivity.create(requestCode, Constants.ACTION_REQUEST_RETRY, getPermissionNames(permissions), onPermissionCallBack);
    }

    public static void createRequestSpecial(int requestCode, OnPermissionCallBack onPermissionCallBack, Permission permission) {
        TranslucentActivity.create(requestCode, Constants.ACTION_SPECIAL, getPermissionNames(permission), onPermissionCallBack);
    }

    public static void createApplyRequest(int requestCode, OnPermissionCallBack onPermissionCallBack, Permission... permissions) {
        TranslucentActivity.create(requestCode, Constants.ACTION_APPLY, getPermissionNames(permissions), onPermissionCallBack);
    }

    private static String[] getPermissionNames(Permission... permissions) {
        String[] strings = new String[permissions.length];
        for (int i=0;i<permissions.length;i++) {
            PermissionStore.addPermission(permissions[i]);
            strings[i] = permissions[i].name;
        }
        return strings;
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
                    //已经全部通过
                    if (mPermissionCallBack != null) {
                        mPermissionCallBack.onAllowedWitOutSpecial();
                    }
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

        private void requestSpecial(Permission permission) {
            if (permission!=null) {
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
                if (permissions.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        SinglePermission per = mPermissionsWeDoNotHave.get(permissions[i]);
                        if (per != null && grantResults.length > i) {
                            per.setGrant(grantResults[i]);
                        }
                    }
                }
                ArrayList<String> permissionNames = new ArrayList<>();
                ArrayList<Boolean> canShowTips = new ArrayList<>();
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
                            requiredFailed.add(p);
                        }
                    }
                }
                //必须权限处理 开启设计界面
                if(requiredFailed.size()>0){
                    //默认处理
                    if (!mPermissionCallBack.onRequireFail(requiredFailed.toArray(new String[requiredFailed.size()]))) {
                        StringBuilder builder = new StringBuilder();
                        for (String per:requiredFailed) {
                            builder.append(PermissionStore.getPermission(per).message).append("\n");
                        }
                        TranslucentActivity.showDialog("权限申请",builder.toString());
                    }
                    return;
                }

                //必须权限多次请求
                if (requiredPermissionsRetry.size() > 0) {
                    Permission[] ps = new Permission[requiredPermissionsRetry.size()];
                    for (int i = 0; i < requiredPermissionsRetry.size(); i++) {
                        ps[i] = PermissionStore.getPermission(requiredPermissionsRetry.get(i));
                    }
                    createRequestRetry(mRequestCode, mPermissionCallBack,ps);
                    return ;
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
                //权限请求正常结束
                return;
            }
            //非当前请求
            return;
        }

        void setPermissionObject(PermissionObject permissionObject) {
            this.mPermissionObject = permissionObject;
        }
    }

    /**
     * 杀死程序
     */
    static void killSelf() {
        if (mOnRefused.get() == null || mOnRefused.get().onRefused("权限请求失败")) {
            if (mActivity.get() != null) {
                mActivity.get().finish();
            }
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    static void getAppDetailSetting(Context context) {
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
