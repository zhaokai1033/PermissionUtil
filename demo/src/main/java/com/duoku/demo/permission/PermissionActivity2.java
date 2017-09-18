package com.duoku.demo.permission;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;

import com.duoku.permission.OnRequestPermissionCallBack;
import com.duoku.permission.PermissionStore;
import com.duoku.permission.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class PermissionActivity2 extends AppCompatActivity {

    /**
     * 权限请求工具
     */
    private PermissionUtil.PermissionObject mPermissionUtil;
    private static final String TAG = "PermissionActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        mPermissionUtil = PermissionUtil.with(this);

        findViewById(R.id.tv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean[] booleans = mPermissionUtil.check(Manifest.permission.SEND_SMS);
                ToastUtil.show(getApplicationContext(), booleans[0] ? "允许" : "禁止");
            }
        });

        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionUtil
                        .createRequest(1001, new OnRequestPermissionCallBack() {
                            @Override
                            public void onAllowed() {
                                ToastUtil.show(getApplicationContext(), "允许");
                            }

                            @Override
                            public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {
                                ToastUtil.show(getApplicationContext(), permissions.toString() + "有拒绝的 " + isCanShowTip.toString());
                            }
                            @Override
                            public void onUnSupport(int requestCode, String[] permissions) {
                                Log.d(TAG, Arrays.toString(permissions));
                            }

                        }, new String[]{Manifest.permission.SEND_SMS})
                        .request();
            }
        });

        findViewById(R.id.tv3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsMgr = SmsManager.getDefault();
                String message = "消息内容";
                String address = "15210087223";
                smsMgr.sendTextMessage(address, null, message, null, null);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionUtil != null) {
            mPermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
