package com.wryel.android.cordova.plugin.systemalertwindowpermission;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

public class SystemAlertWindowPermissionPlugin extends CordovaPlugin {

    private CallbackContext callbackContext = null;

    /* methods */

    public static final String ACTION_HAS_PERMISSION = "hasPermission";

    public static final String ACTION_OPEN_NOTIFICATION_SETTINGS = "openNotificationSettings";

    public static final String ACTION_REQUEST_PERMISSION = "requestPermission";

    /* return values */

    public static final int TRUE = 1;

    public static final int FALSE = 0;

    /* other */

    public static final int ANDROID_VERSION_MARSHMALLOW = 23;

    public static final int INVALID_ACTION = -1;

    public static final int REQUEST_SYSTEM_ALERT_WINDOW = 1;

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) throws JSONException {

        boolean success = true;

        if (ACTION_HAS_PERMISSION.equals(action)) {

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    callbackContext.success(hasPermission());
                }
            });

        }
        else if (ACTION_OPEN_NOTIFICATION_SETTINGS.equals(action)) {

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    SystemAlertWindowPermissionPlugin.this.callbackContext = callbackContext;
                    requestPermission();
                }
            });

        }
        else if (ACTION_REQUEST_PERMISSION.equals(action)) {

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    SystemAlertWindowPermissionPlugin.this.callbackContext = callbackContext;
                    requestPermission();
                }
            });

        } else {

            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    callbackContext.error(INVALID_ACTION);
                }
            });

            success = false;

        }

        return success;
    }

    protected int hasPermission() {

        if (Build.VERSION.SDK_INT < ANDROID_VERSION_MARSHMALLOW) {
            return TRUE;
        } else {
            return Settings.canDrawOverlays(cordova.getActivity()) ? TRUE : FALSE;
        }
    }

    protected void openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= ANDROID_VERSION_MARSHMALLOW) {
            String packageName = cordova.getActivity().getPackageName();
            try {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {

                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", packageName);
                    intent.putExtra("app_uid", cordova.getActivity().getApplicationInfo().uid);

                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + packageName));

                } else {
                    return;
                }

                cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_SYSTEM_ALERT_WINDOW);

            } catch (Exception e) {
                // log goes here

            }

        }
    }

    protected void requestPermission() {

        if (Build.VERSION.SDK_INT >= ANDROID_VERSION_MARSHMALLOW) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + cordova.getActivity().getPackageName()));
            cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_SYSTEM_ALERT_WINDOW);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (this.callbackContext == null) {
            return;
        }

        if (requestCode == REQUEST_SYSTEM_ALERT_WINDOW) {
            this.callbackContext.success(hasPermission());
        }

        this.callbackContext = null;
    }
}