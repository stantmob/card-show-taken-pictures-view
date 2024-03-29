package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;

/**
 * Created by gabrielrosa on 20/01/17.
 */

public class AppPermissions {

    public static final Integer PERMISSIONS_CODE = 100;
    private static boolean doNotAskIsChecked = false;
    private static String[] deniedPermissions;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE};
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static final String[] PERMISSIONS_SDK_29 = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE};
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static final String[] PERMISSIONS_SDK_33 = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.VIBRATE};

    private static String[] getPermissions() {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.TIRAMISU) {
            return PERMISSIONS_SDK_33;
        } else if (sdkVersion >= Build.VERSION_CODES.Q) {
            return PERMISSIONS_SDK_29;
        } else {
            return PERMISSIONS;
        }
    }

    public static void requestPermissionsFor(Activity activity) {
        if (isNotApiAndroidM()) return;
        buildPermissionDialog(activity).show();
    }

    private static Dialog buildPermissionDialog(Activity activity) {
        Dialog informationDialog = new Dialog(activity);
        informationDialog.setContentView(R.layout.permission_information_dialog);
        informationDialog.setCancelable(false);

        if (donNotAskIsChecked()) {
            configureDialogs(activity, informationDialog, View.VISIBLE, R.string.permission_information_dialog_never_checked_message);
        } else {
            configureDialogs(activity, informationDialog, View.GONE, R.string.permission_information_dialog_message);
        }

        return informationDialog;
    }

    private static boolean donNotAskIsChecked() {
        return doNotAskIsChecked;
    }

    private static boolean isNotApiAndroidM() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    private static void configureDialogs(Activity activity, Dialog informationDialog,
                                         int visibility, int contentText) {
        TextView informationText = informationDialog.findViewById(R.id.permission_information_dialog_text_view);
        TextView settingsButton = informationDialog.findViewById(R.id.permission_information_dialog_go_to_settings_text_view);
        TextView okButton = informationDialog.findViewById(R.id.permission_information_dialog_confirmation_text_view);

        informationText.setText(activity.getResources().getString(contentText));
        settingsButton.setVisibility(visibility);
        addListeners(activity, informationDialog, okButton, settingsButton);

    }

    private static void addListeners(Activity activity, Dialog informationDialog, TextView okButton, TextView settingsOption) {
        okButton.setOnClickListener(view -> {
            informationDialog.dismiss();
            requestPermission(activity);
        });

        settingsOption.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivityForResult(intent, 1);
        });
    }

    private static void requestPermission(Activity activity) {
        if (!hasPermissionsOn(activity.getApplicationContext()) && deniedPermissions != null) {
            ActivityCompat.requestPermissions(activity, deniedPermissions, PERMISSIONS_CODE);
            deniedPermissions = null;
        } else if (!hasPermissionsOn(activity.getApplicationContext()) && deniedPermissions == null) {
            ActivityCompat.requestPermissions(activity, getPermissions(), PERMISSIONS_CODE);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasPermissionsOn(Context context) {
        boolean result = true;
        List<String> denied = new ArrayList<>();

        for (String permission : getPermissions()) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                denied.add(permission);
                result = false;
            }
        }

        deniedPermissions = new String[denied.size()];
        denied.toArray(deniedPermissions);

        return result;
    }

    public static boolean hasLocationPermissionsOn(Context context) {
        boolean hasFineLocationPermission = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocationPermission = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return hasFineLocationPermission && hasCoarseLocationPermission;
    }

    @SuppressWarnings("unused")
    public static void setDoNotAskAsChecked() {
        doNotAskIsChecked = true;
    }
}
