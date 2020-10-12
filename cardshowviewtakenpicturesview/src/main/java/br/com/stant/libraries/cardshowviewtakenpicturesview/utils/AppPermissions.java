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
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;

/**
 * Created by gabrielrosa on 20/01/17.
 */

public class AppPermissions {

    public static final Integer PERMISSIONS_CODE = 1;
    private static boolean doNotAskIsChecked = false;
    private static String[] deniedPermissions;
    private static String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE
            , Manifest.permission.CAMERA
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void requestPermissions(Activity activity) {
        if(isNotApiAndroidM()) return;

        buildPermissionDialog(activity).show();
    }

    private static Dialog buildPermissionDialog(Activity activity) {
        Dialog informationDialog = new Dialog(activity);
        informationDialog.setContentView(R.layout.permission_information_dialog);

        if (donNotAskIsChecked()) {
            configureDialogs(activity, informationDialog, View.VISIBLE, R.string.permission_information_dialog_never_checked_message);
        }
        else {
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
                                         int visibility, int contentText){
        TextView informationText = (TextView) informationDialog.findViewById(R.id.permission_information_dialog_text_view);
        TextView settingsButton  = (TextView) informationDialog.findViewById(R.id.permission_information_dialog_go_to_settings_text_view);
        TextView okButton        = (TextView) informationDialog.findViewById(R.id.permission_information_dialog_confirmation_text_view);

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
        if (!hasPermissions(activity.getApplicationContext()) && deniedPermissions != null) {
            ActivityCompat.requestPermissions(activity, deniedPermissions, PERMISSIONS_CODE);
            deniedPermissions = null;
        } else if (!hasPermissions(activity.getApplicationContext()) && deniedPermissions == null ){
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSIONS_CODE);
        }
    }

    public static boolean hasPermissions(Context context) {
        boolean result = true;
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            int permissionStatus = ActivityCompat.checkSelfPermission(context, permission);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
                result = false;
            }
        }
        AppPermissions.deniedPermissions = new String[deniedPermissions.size()];
        deniedPermissions.toArray(AppPermissions.deniedPermissions);
        return result;
    }

    public static void setDoNotAskAsChecked() {
        doNotAskIsChecked = true;
    }
}
