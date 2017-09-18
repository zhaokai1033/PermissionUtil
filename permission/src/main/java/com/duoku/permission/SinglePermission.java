package com.duoku.permission;

/**
 * ================================================
 * Created by zhaokai on 2017/5/12.
 * Email zhaokai1033@126.com
 * Describe : 单个权限
 * ================================================
 */
class SinglePermission {

    private String mPermissionName;
    private boolean mRationalNeeded = false;
    private String mReason;
    private int grant = 0;
    private boolean mSpecial = false;
    private boolean mIsRequest = false;

    SinglePermission(String permissionName) {
        mPermissionName = permissionName;
    }

    public SinglePermission(String permissionName, String reason) {
        mPermissionName = permissionName;
        mReason = reason;
    }

    boolean isRationalNeeded() {
        return mRationalNeeded;
    }

    void setRationalNeeded(boolean rationalNeeded) {
        mRationalNeeded = rationalNeeded;
    }

    public String getReason() {
        return mReason == null ? "" : mReason;
    }

    public void setReason(String reason) {
        mReason = reason;
    }

    String getPermissionName() {
        return mPermissionName;
    }

    public void setPermissionName(String permissionName) {
        mPermissionName = permissionName;
    }

    public boolean isSpecial() {
        return mSpecial;
    }

    public void setSpecial(boolean mSpecial) {
        this.mSpecial = mSpecial;
    }

    public boolean isRequest() {
        return mIsRequest;
    }

    public void setRequest(boolean request) {
        this.mIsRequest = request;
    }

    public int getGrant() {
        return grant;
    }

    public void setGrant(int grant) {
        this.grant = grant;
    }
}
