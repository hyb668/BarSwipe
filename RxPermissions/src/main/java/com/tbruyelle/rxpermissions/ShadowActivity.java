package com.tbruyelle.rxpermissions;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class ShadowActivity extends EnsureSameProcessActivity {

    private final int DangerousPermissions_request_code = 32;
    private final int SystemAlerWindow_request_code = 33;
    private final int WriteSetting_request_code = 34;
    private final int OpenAppDetailSetting_managePermission = 35;

    private List<String> specialpermissions, permissions;
    private int[] specialGrantResult, grantResults;

    private int index = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * 有特殊先处理特殊的，然后是选择了不在询问的处理方式，再然后是处理正常的，
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        String[] tempPermissions = intent.getStringArrayExtra("permissions");

        if (tempPermissions != null) {
            for (int i = 0; i < tempPermissions.length; i++) {
                String permission = tempPermissions[i];
                if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW) || permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                    if (specialpermissions == null)
                        specialpermissions = new ArrayList<>();
                    specialpermissions.add(permission);
                } else {
                    if (permissions == null)
                        permissions = new ArrayList<>();
                    permissions.add(permission);
                }
            }
        }

        if (specialpermissions != null && specialpermissions.size() > 0) {
            specialGrantResult = new int[specialpermissions.size()];
            index = 0;
            startRequest(index);
        } else {
            requestPerm();
        }
    }

    /**
     * @return
     */
    private boolean isHaveSpeicalGrant() {
        return index != -1;
    }

    /**
     * 获取权限组的key,属于哪个权限组
     *
     * @param permissions
     * @return
     */
    private String getPermissionGroupKey(String permissions) {
        String key = "";
        switch (permissions) {
            case Manifest.permission.READ_CALENDAR:
            case Manifest.permission.WRITE_CALENDAR:
                key = Manifest.permission_group.CALENDAR;
                break;
            case Manifest.permission.CAMERA:
                key = Manifest.permission_group.CAMERA;
                break;
            case Manifest.permission.READ_CONTACTS:
            case Manifest.permission.WRITE_CONTACTS:
            case Manifest.permission.GET_ACCOUNTS:
                key = Manifest.permission_group.CONTACTS;
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                key = Manifest.permission_group.LOCATION;
                break;
            case Manifest.permission.RECORD_AUDIO:
                key = Manifest.permission_group.MICROPHONE;
                break;
            case Manifest.permission.READ_PHONE_STATE:
            case Manifest.permission.CALL_PHONE:
            case Manifest.permission.READ_CALL_LOG:
            case Manifest.permission.WRITE_CALL_LOG:
            case Manifest.permission.USE_SIP:
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
                key = Manifest.permission_group.PHONE;
                break;
            case Manifest.permission.BODY_SENSORS:
                key = Manifest.permission_group.SENSORS;
                break;
            case Manifest.permission.SEND_SMS:
            case Manifest.permission.RECEIVE_SMS:
            case Manifest.permission.READ_SMS:
            case Manifest.permission.RECEIVE_WAP_PUSH:
            case Manifest.permission.RECEIVE_MMS:
                key = Manifest.permission_group.SMS;
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                key = Manifest.permission_group.STORAGE;
                break;
        }

        return key.replace("-", "_");
    }

    /**
     * 给出合理提示文本
     *
     * @param ctx
     * @param permissions
     * @return
     */
    private String getNotGrantedPermissionMsg(Context ctx, List<String> permissions) {
        StringBuffer buffer = new StringBuffer(ctx.getResources().getString(R.string.str_permission_grant_needed));
        if (permissions != null && permissions.size() > 0) {
            //只有一个
            if (permissions.size() == 1) {
                int resouceid = ctx.getResources().getIdentifier("str_permission_" + getPermissionGroupKey(permissions.get(0)), "string", ctx.getPackageName());
                if (resouceid > 0) {
                    buffer.append(ctx.getResources().getString(resouceid));
                }
            } else {
                for (int i = 0; i < permissions.size(); i++) {
                    int resouceid = ctx.getResources().getIdentifier("str_permission_multe_" + getPermissionGroupKey(permissions.get(i)), "string", ctx.getPackageName());
                    if (resouceid > 0) {
                        buffer.append(ctx.getResources().getString(resouceid));
                        buffer.append("、");
                    }
                }

                buffer.deleteCharAt(buffer.length() - 1);
                buffer.append("权限。");
            }
        }

        return buffer.toString();
    }

    /**
     * 在不在提示的情况下，给出合理的解释
     *
     * @param content
     */
    private void showPermissionAlert(String content) {
        new AlertDialog.Builder(this)
                .setMessage(content)
                .setPositiveButton(R.string.str_permission_conform, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, OpenAppDetailSetting_managePermission);
                    }
                })
                .setNegativeButton(R.string.str_permission_cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dealResult();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * @param position
     */
    private void startRequest(int position) {
        if (position < specialpermissions.size()) {
            if (specialpermissions.get(position).equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                startSpecialGrant(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, SystemAlerWindow_request_code);
            } else if (specialpermissions.get(position).equals(Manifest.permission.WRITE_SETTINGS)) {
                startSpecialGrant(Settings.ACTION_MANAGE_WRITE_SETTINGS, WriteSetting_request_code);
            }
        }
    }

    /**
     * @param action
     * @param requestCode
     */
    private void startSpecialGrant(String action, int requestCode) {
        Intent intent = new Intent(action, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    /**
     *
     */
    private void requestPerm() {
        if (permissions != null && permissions.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), DangerousPermissions_request_code);
        } else {
            if (isHaveSpeicalGrant()) {
                RxPermissions.getInstance(this).onRequestPermissionsResult(DangerousPermissions_request_code, specialpermissions.toArray(new String[specialpermissions.size()]), specialGrantResult, new boolean[specialGrantResult.length]);
            }
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OpenAppDetailSetting_managePermission) {
            requestPerm();
        } else {
            if (requestCode == SystemAlerWindow_request_code) {
                specialGrantResult[index] = Settings.canDrawOverlays(this) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            } else if (requestCode == WriteSetting_request_code) {
                specialGrantResult[index] = Settings.System.canWrite(this) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            }

            index++;
            if (index == specialpermissions.size()) {
                requestPerm();
            } else {
                startRequest(index);
            }
        }
    }

    /**
     * @param permissions
     * @return
     */
    private boolean isAllCanShowRequestPermissionRationale(String[] permissions) {
        List<String> notShowRequestPermission = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            //可以给出授权提示
            if (!RxPermissions.getInstance(this).isGranted(permissions[i]) && !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                notShowRequestPermission.add(permissions[i]);
            }
        }

        if (notShowRequestPermission.size() > 0) {
            showPermissionAlert(getNotGrantedPermissionMsg(this, notShowRequestPermission));
            return false;
        }

        return true;
    }

    /**
     *
     */
    private void dealResult() {
        int length = isHaveSpeicalGrant() ? permissions.size() + specialpermissions.size() : permissions.size();

        boolean[] shouldShowRequestPermissionRationale = new boolean[length];
        String[] requestPermissions = new String[length];
        int[] requestGrantResults = new int[length];

        for (int i = 0; i < shouldShowRequestPermissionRationale.length; i++) {
            if (i >= permissions.size()) {
                shouldShowRequestPermissionRationale[i] = false;
                requestPermissions[i] = specialpermissions.get(i - permissions.size());
                requestGrantResults[i] = specialGrantResult[i - permissions.size()];
            } else {
                shouldShowRequestPermissionRationale[i] = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions.get(i));
                requestPermissions[i] = permissions.get(i);
                requestGrantResults[i] = grantResults[i];
            }
        }

        RxPermissions.getInstance(this).onRequestPermissionsResult(DangerousPermissions_request_code, requestPermissions, requestGrantResults, shouldShowRequestPermissionRationale);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] mGrantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        grantResults = mGrantResults;
        if (requestCode == DangerousPermissions_request_code) {
            if (isAllCanShowRequestPermissionRationale(permissions)) {
                dealResult();
            }
        }
    }
}
