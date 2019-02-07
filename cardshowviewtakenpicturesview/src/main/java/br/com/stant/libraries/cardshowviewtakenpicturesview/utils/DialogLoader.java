package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.DialogLoaderBinding;

public class DialogLoader extends Dialog {

    private Context mContext;
    private DialogLoaderBinding mDialogLoaderBinding;

    public DialogLoader(@NonNull Context context) {
        super(context);
        this.mContext = context;

        dialogSetup();
    }

    private void dialogSetup() {
        mDialogLoaderBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_loader, null, false);
        setContentView(mDialogLoaderBinding.getRoot());
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    public void showLocalLoader() {
        show();
    }

    public void hideLocalLoader() {
        dismiss();
    }


}
