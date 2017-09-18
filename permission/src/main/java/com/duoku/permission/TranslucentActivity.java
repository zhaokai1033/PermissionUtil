package com.duoku.permission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class TranslucentActivity extends AppCompatActivity {

    private static final String TAG = "TransparentActivity";
    /**
     * 权限请求工具
     */
    private PermissionUtil.PermissionObject mPermissionUtil;
    private int mRequestCode;
    private String[] mPermissions;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        setTheme(android.R.style.Theme_Translucent);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Window window = getWindow();
//        WindowManager.LayoutParams params = window.getAttributes();
//        params.alpha = 0.0f;//这句就是设置窗口里控件的透明度的．０.０全透明．１.０不透明．
//        window.setAttributes(params);
//        window.setBackgroundDrawable(new ColorDrawable(Color.BLUE));
//        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        request(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        request(intent);
    }

    private void request(Intent intent) {
        mRequestCode = intent.getIntExtra(Constants.REQUEST_CODE, 0);
        mPermissions = intent.getStringArrayExtra(Constants.PERMISSIONS);
        String mAction = intent.getStringExtra(Constants.ACTION);
        mPermissionUtil = PermissionUtil.with(this);

        if (Constants.ACTION_CHECK.equals(mAction)) {
            boolean[] bs = mPermissionUtil.check(mPermissions);
            PermissionStore.getPermissionCallBack(mRequestCode).onCheck(bs);
            finish();
        } else if (Constants.ACTION_REQUEST.equals(mAction)) {
            mPermissionUtil
                    .createRequest(mRequestCode, new OnPermissionCallBack() {
                        @Override
                        public void onAllowed() {
                            PermissionStore.getPermissionCallBack(mRequestCode).onAllowed();
                        }

                        @Override
                        public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {
                            PermissionStore.getPermissionCallBack(mRequestCode).onRefused(permissions, isCanShowTip);
                        }

                        @Override
                        public void onResult(int requestCode, String[] permissions, int[] grantResults) {
                            PermissionStore.getPermissionCallBack(mRequestCode).onResult(requestCode, permissions, grantResults);
                        }

                        @Override
                        public void onCheck(boolean[] booleans) {
                            PermissionStore.getPermissionCallBack(mRequestCode).onCheck(booleans);
                        }

                        @Override
                        public void onError(String msg) {
                            PermissionStore.getPermissionCallBack(mRequestCode).onError(msg);
                        }

                        @Override
                        public void onUnSupport(int requestCode, String[] permissions) {
                            PermissionStore.getPermissionCallBack(mRequestCode).onUnSupport(requestCode, permissions);
                        }

                        @Override
                        public void onFinish() {
                            finish();
                        }
                    }, mPermissions)
                    .request();
        } else if (Constants.ACTION_SPECIAL.equals(mAction)) {
            PermissionUtilSpecial.request(Constants.CODE_SPECIAL, this, null, new OnPermissionCallBack() {
                @Override
                public void onAllowed() {
                    PermissionStore.getPermissionCallBack(mRequestCode).onAllowed();
                }

                @Override
                public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {
                    PermissionStore.getPermissionCallBack(mRequestCode).onRefused(permissions, isCanShowTip);
                }

                @Override
                public void onResult(int requestCode, String[] permissions, int[] grantResults) {
                    PermissionStore.getPermissionCallBack(mRequestCode).onResult(requestCode, permissions, grantResults);
                }

                @Override
                public void onCheck(boolean[] booleans) {
                    PermissionStore.getPermissionCallBack(mRequestCode).onCheck(booleans);
                }

                @Override
                public void onError(String msg) {
                    PermissionStore.getPermissionCallBack(mRequestCode).onError(msg);
                }

                @Override
                public void onUnSupport(int requestCode, String[] permissions) {
                    PermissionStore.getPermissionCallBack(mRequestCode).onUnSupport(requestCode, permissions);
                }

                @Override
                public void onFinish() {
                    finish();
                }
            }, mPermissions[0]);
        } else {
            Log.d(TAG, "这是做什么？");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean flag = false;
        if (mPermissionUtil != null) {
            flag = mPermissionUtil.onRequestPermissionsResult(mRequestCode, mPermissions, new int[]{resultCode});
        }
        if (!flag) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = false;
        if (mPermissionUtil != null) {
            flag = mPermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (!flag) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    public static void create(int requestCode, String action, String[] permissions, OnPermissionCallBack onPermissionCallBack) {
        if (PermissionUtil.getContext() == null) {
            onPermissionCallBack.onError("'context' is null ");
            return;
        }
        PermissionStore.addPermissionCallBack(requestCode, onPermissionCallBack);
        Intent intent = new Intent(PermissionUtil.getContext(), TranslucentActivity.class);
        intent.putExtra(Constants.REQUEST_CODE, requestCode);
        intent.putExtra(Constants.ACTION, action);
        intent.putExtra(Constants.PERMISSIONS, permissions);
        if (!(PermissionUtil.getContext() instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PermissionUtil.getContext().startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PermissionStore.remove(mRequestCode);
    }
}
