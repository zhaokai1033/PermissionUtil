package com.duoku.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
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

public class TranslucentActivity extends Activity {

    private static final String TAG = "TransparentActivity";
    /**
     * 权限请求工具
     */
//    private PermissionUtil.PermissionObject mPermissionUtil;
//    private int mRequestCode = -1;
//    private String[] mPermissions;
    private SparseArray<Temp> tempArray = new SparseArray<>();

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
        String mAction = intent.getStringExtra(Constants.ACTION);
        if(Constants.ACTION_DIALOG.equals(mAction)) {
            String title = intent.getStringExtra(Constants.TITLE);
            String message = intent.getStringExtra(Constants.MESSAGE);
            show(title,message);
        }else {
            Temp temp = new Temp();
            temp.requestCode = intent.getIntExtra(Constants.REQUEST_CODE, 0);
            temp.requestCodeRetry = intent.getIntExtra(Constants.REQUEST_CODE_RETRY,-1);
            temp.permission = intent.getStringArrayExtra(Constants.PERMISSIONS).clone();
            temp.permissionUtil = PermissionUtil.with(this);
//            if(tempArray.get(temp.requestCode)!=null){
//                temp.permissionRetry = temp.permission;
//                temp.permission = tempArray.get(temp.requestCode).permission;
//            }
            tempArray.put(temp.requestCode, temp);
            if (Constants.ACTION_CHECK.equals(mAction)) {
                boolean[] bs = temp.permissionUtil.check(temp.permission);
                PermissionStore.getPermissionCallBack(temp.requestCode).onCheck(bs);
                PermissionStore.getPermissionCallBack(temp.requestCode).onFinish();
                cleanTempArray(temp.requestCode);
                finish();
            } else if (Constants.ACTION_REQUEST.equals(mAction)||Constants.ACTION_REQUEST_RETRY.equals(mAction)) {

                boolean[] bs ;
//                if(Constants.ACTION_REQUEST.equals(mAction)) {
                    bs = temp.permissionUtil.check(temp.permission);
//                }else {
//                    temp.requestCode = intent.getIntExtra(Constants.REQUEST_CODE_RETRY,-1);
//                    bs = temp.permissionUtil.check(temp.permissionRetry);
//                }
                boolean flag = true;
                for (boolean b:bs){
                    flag = b&&flag;
                }
                //有未通过的
                if(!flag) {
                    temp.permissionUtil
                            .createRequest(temp.requestCode
                                    , getOnPermissionCallBack(temp.requestCodeRetry,temp.requestCode), temp.permission)
                            .request();
                }else {
                    getOnPermissionCallBack(temp.requestCodeRetry,temp.requestCode).onAllowedWitOutSpecial();
                    getOnPermissionCallBack(temp.requestCodeRetry,temp.requestCode).onFinish();
                    cleanTempArray(temp.requestCode);
                    finish();
                }
            } else if (Constants.ACTION_SPECIAL.equals(mAction)) {
                PermissionUtilSpecial.request(Constants.CODE_SPECIAL, this, null, getOnPermissionCallBack(temp.requestCodeRetry,temp.requestCode), temp.permission[0]);
            } else {
                Log.d(TAG, "这是做什么？");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Temp temp = tempArray.get(requestCode);
        if (temp != null) {
           temp.permissionUtil.onRequestPermissionsResult(temp.requestCode, temp.permission, new int[]{resultCode});
            cleanTempArray(requestCode);
        }
        if(tempArray.size()<=0){
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Temp temp = tempArray.get(requestCode);
        if (temp != null) {
            temp.permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
           cleanTempArray(requestCode);
        }

        if(tempArray.size()<=0){
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    private void cleanTempArray(int requestCode){
        tempArray.remove(requestCode);
        if(requestCode<0)
        PermissionStore.remove(requestCode);
    }

    public static OnPermissionCallBack getOnPermissionCallBack(final int mRequestCode,final int original) {
        return  new OnPermissionCallBack() {
            @Override
            public void onAllowedWitOutSpecial() {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                    callBack.onAllowedWitOutSpecial();
                }
            }

            @Override
            public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                    callBack.onRefused(permissions, isCanShowTip);
                }
            }

            @Override
            public void onResult(int requestCode, String[] permissions, int[] grantResults) {

                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                    callBack.onResult(requestCode, permissions, grantResults);
                }
            }

            @Override
            public void onCheck(boolean[] booleans) {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                    callBack.onCheck(booleans);
                }
            }

            @Override
            public void onError(String msg) {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                    callBack.onError(msg);
                }
            }

            @Override
            public boolean isRequired(String permission) {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                    return callBack.isRequired(permission);
                }
                return true;
            }

            @Override
            public boolean onRequireFail(String[] permissions) {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null&&callBack!=this) {
                   return callBack.onRequireFail(permissions);
                }
                return true;
            }

            @Override
            public void onUnSupport(int requestCode, String[] permissions) {
                OnPermissionCallBack callBack = PermissionStore.getPermissionCallBack(mRequestCode);
                if(callBack==this||callBack==null){
                    callBack =PermissionStore.getPermissionCallBack(original);
                }
                if(callBack!=null) {
                    callBack.onUnSupport(requestCode, permissions);
                }
            }

            @Override
            public void onFinish() {

            }
        };
    }


    public static void create(int requestCode, String action, String[] permissions, OnPermissionCallBack onPermissionCallBack) {
        if (PermissionUtil.getContext() == null) {
            onPermissionCallBack.onError("'context' is null ");
            return;
        }
        Intent intent = new Intent(PermissionUtil.getContext(), TranslucentActivity.class);
        if(Constants.ACTION_REQUEST_RETRY.equals(action)){
            int r = -requestCode;
            while (PermissionStore.getPermissionCallBack(r)!=null){
                r=r-1;
            }
            intent.putExtra(Constants.REQUEST_CODE_RETRY, r);
        }else {
            PermissionStore.addPermissionCallBack(requestCode, onPermissionCallBack);
        }
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
    }

    @Override
    public void finish() {
        if(tempArray.size()<=0) {
            super.finish();
        }
    }

    public static void showDialog(String title, String message) {
        if (PermissionUtil.getContext() == null) {
            return;
        }
        Intent intent = new Intent(PermissionUtil.getContext(), TranslucentActivity.class);
        intent.putExtra(Constants.ACTION, Constants.ACTION_DIALOG);
        intent.putExtra(Constants.MESSAGE, message);
        intent.putExtra(Constants.TITLE, title);
        if (!(PermissionUtil.getContext() instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PermissionUtil.getContext().startActivity(intent);
    }

    private void show(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(title)
                .setMessage(message)
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtil.getAppDetailSetting(getApplication());
                        PermissionUtil.killSelf();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtil.killSelf();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    public static class Temp{
        PermissionUtil.PermissionObject permissionUtil;
        String[] permission;
        String[] permissionRetry;
        int requestCode;
        int requestCodeRetry;
    }
}
