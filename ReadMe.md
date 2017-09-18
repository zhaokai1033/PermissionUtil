# PermissionUtil

*Android permission application*

1、使用

```java
PermissionUtil.createRequest(1001,
        new OnRequestPermissionCallBack() {
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
                Log.d(TAG, "UnSupport:" + Arrays.toString(permissions));
            }
        },
        new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS});
```

2、引用

[jar](https://github.com/zhaokai1033/PermissionUtil/blob/master/demo/libs/permission.jar)