# PermissionUtil

*Android permission application*

  ![image](https://github.com/zhaokai1033/PermissionUtil/blob/master/Demo.gif)

#### 一、引用

1. [jar](https://github.com/zhaokai1033/PermissionUtil/blob/master/demo/libs/permission.jar)

2. ```xml
      <!--Activity Used-->
   	<activity
           android:name="com.duoku.permission.TranslucentActivity"
           android:launchMode="singleTop"
           android:theme="@style/TranslucentTheme" />

   	<!--Translucent Style-->
       <style name="TranslucentTheme" parent="Theme.AppCompat.NoActionBar">
           <item name="android:windowBackground">@android:color/transparent</item>
           <item name="android:colorBackgroundCacheHint">@null</item>
           <item name="android:windowIsTranslucent">true</item>
           <item name="android:windowAnimationStyle">@android:style/Animation</item>
           <item name="android:windowNoTitle">true</item>
           <item name="android:windowContentOverlay">@null</item>
           <item name="android:windowFrame">@null</item>
           <item name="android:windowIsFloating">true</item>
           <item name="android:backgroundDimEnabled">true</item>
           <item name="android:windowFullscreen">true</item>
       </style>
   ```

#### 二、使用

1、检查权限

```java
PermissionUtil.check(new OnCheckPermissionCallBack() {
    @Override
    public void onCheck(boolean[] booleans) {
        ToastUtil.show(getApplicationContext(), booleans[0] && booleans[1] ? "允许" : "禁止");
    }
}, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
```

2、请求权限

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
