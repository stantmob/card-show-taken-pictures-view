package br.com.stant.libraries.cameraimagegalleryview.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;

public class DeleteAlertDialog extends DialogFragment {
    public interface OnDelete {
        void delete();

        void cancel();
    }

    private final Context mContext;
    private OnDelete onDelete;

    public DeleteAlertDialog(@NonNull Context context, @NonNull OnDelete onDelete) {
        this.onDelete = onDelete;
        this.mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mContext, R.style.AlertDialogTheme);
        builder.setTitle(R.string.delete_alert_dialog_question)
                .setPositiveButton(R.string.delete_alert_dialog_confirm, (DialogInterface dialog, int id) -> {
                    onDelete.delete();
                })
                .setNegativeButton(R.string.delete_alert_dialog_cancel, (DialogInterface dialog, int id) -> {
                    onDelete.cancel();
                });

        return builder.create();
    }
}
