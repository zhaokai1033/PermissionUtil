package com.duoku.demo.permission;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.duoku.permission.OnCheckPermissionCallBack;
import com.duoku.permission.OnRequestPermissionCallBack;
import com.duoku.permission.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class PermissionActivity1 extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PermissionUtil.init(this);


        setContentView(R.layout.activity_1);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtil.check(2000, new OnCheckPermissionCallBack() {
                    @Override
                    public void onCheck(boolean[] booleans) {
                        for (int i = 0; i < booleans.length; i++) {
                            if (!booleans[i]) {
                                ToastUtil.show(getApplicationContext(), "有未请求的");
                                PermissionUtil.createRequest(2001,
                                        new OnRequestPermissionCallBack() {
                                            @Override
                                            public void onAllowedWitOutSpecial() {
                                                ToastUtil.show(getApplicationContext(), "全部允许");
                                            }

                                            @Override
                                            public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {
                                                ToastUtil.show(getApplicationContext(), "拒绝：" + permissions.toString());
                                            }

                                            @Override
                                            public void onUnSupport(int requestCode, String[] permissions) {

                                            }

                                            @Override
                                            public boolean isRequired(String permission) {
                                                return true;
                                            }

                                            @Override
                                            public boolean onRequireFail(String[] permissions) {
                                                return super.onRequireFail(permissions);
                                            }
                                        },
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
                                                Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION});
                                break;
                            }
                            ToastUtil.show(getApplicationContext(), "已经允许");
                        }
                    }
                }, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        });

        //检查权限
        findViewById(R.id.tv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtil.check(1000, new OnCheckPermissionCallBack() {
                    @Override
                    public void onCheck(boolean[] booleans) {
                        ToastUtil.show(getApplicationContext(), booleans[0] && booleans[1] ? "允许" : "禁止");
                    }
                }, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
        });

        //请求权限
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionUtil.createRequest(1001, new OnRequestPermissionCallBack() {
                    @Override
                    public void onAllowedWitOutSpecial() {
                        ToastUtil.show(getApplicationContext(), "全部允许");
                    }

                    @Override
                    public void onRefused(ArrayList<String> permissions, ArrayList<Boolean> isCanShowTip) {
                        ToastUtil.show(getApplicationContext(), permissions.toString() + "有拒绝的 " + isCanShowTip.toString());
                    }

                    @Override
                    public void onUnSupport(int requestCode, String[] permissions) {
                        Log.d(TAG, "UnSupport:" + Arrays.toString(permissions));
                    }

                    @Override
                    public boolean isRequired(String permission) {
                        return true;
                    }

                    @Override
                    public boolean onRequireFail(String[] permissions) {
                        ToastUtil.show(getApplicationContext(), "未通过");
                        return true;
                    }
                }, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        , Manifest.permission.WRITE_SETTINGS
                });
            }
        });

        findViewById(R.id.tv3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = Environment.getExternalStorageState(); //拿到sdcard是否可用的状态码
                if (state.equals(Environment.MEDIA_MOUNTED)) {   //如果可用
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    startActivityForResult(intent, 2001);
                } else {
                    ToastUtil.show(PermissionActivity1.this, "sdcard不可用");
                }
            }
        });

        findViewById(R.id.tv4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PermissionActivity1.this, PermissionActivity2.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            switch (requestCode) {
                case 2001: //拍摄图片并选择
                    //两种方式 获取拍好的图片
                    if (data.getData() != null || data.getExtras() != null) { //防止没有返回结果
                        Uri uri = data.getData();
                        if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                            ToastUtil.show(PermissionActivity1.this, "图片地址：" + uri.getPath()); //拿到图片
                        } else {
                            Bundle bundle = data.getExtras();
                            if (bundle != null) {
                                Bitmap photo = (Bitmap) bundle.get("data");
                                if (photo != null) {
                                    ToastUtil.show(getApplicationContext(), "获取图片资源");
                                }
                            } else {
                                ToastUtil.show(getApplicationContext(), "找不到图片");
                            }
                        }
                    }
                    break;
            }
        }
    }
}
