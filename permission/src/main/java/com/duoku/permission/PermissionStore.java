package com.duoku.permission;

import android.util.SparseArray;

import java.util.HashMap;

/**
 * ========================================
 * Created by zhaokai on 2017/9/15.
 * Email zhaokai1033@126.com
 * des:
 * ========================================
 */

public class PermissionStore {

    private static final PermissionStore store = new PermissionStore();
    private SparseArray<OnPermissionCallBack> objectSparseArray = new SparseArray<>();
    private HashMap<String,Permission> permissions = new HashMap<>();

    static PermissionStore getStore() {
        return store;
    }

    private PermissionStore() {

    }

    public static void addPermissionCallBack(int requestCode, OnPermissionCallBack onPermissionCallBack) {
        getStore().addPermissionCallBackImpl(requestCode, onPermissionCallBack);
    }

    private void addPermissionCallBackImpl(int requestCode, OnPermissionCallBack onPermissionCallBack) {
        objectSparseArray.put(requestCode, onPermissionCallBack);
    }

    public static void addPermission(Permission p) {
        getStore().addPermissionImpl(p);
    }

    private void addPermissionImpl(Permission p) {
        permissions.put(p.name, p);
    }

    public static OnPermissionCallBack getPermissionCallBack(int requestCode) {
        return getStore().getPermissionCallBackImpl(requestCode);
    }

    private OnPermissionCallBack getPermissionCallBackImpl(int requestCode) {
        return objectSparseArray.get(requestCode);
    }


    public static Permission getPermission(String p) {
        return getStore().getPermissionImpl(p);
    }

    private Permission getPermissionImpl(String p) {
        return permissions.get(p);
    }

    public static void removePermission(String p) {
        getStore().removePermissionImpl(p);
    }

    private void removePermissionImpl(String p) {
        permissions.remove(p);
    }

    public static void remove(int requestCode) {
        getStore().removeImpl(requestCode);
    }

    public void removeImpl(int requestCode) {
        objectSparseArray.delete(requestCode);
    }
}
