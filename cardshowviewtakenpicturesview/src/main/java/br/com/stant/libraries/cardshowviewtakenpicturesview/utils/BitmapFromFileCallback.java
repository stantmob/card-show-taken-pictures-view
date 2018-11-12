package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;

import java.io.IOException;

public interface BitmapFromFileCallback {

    void onBitmapDecoded(Bitmap bitmap) throws IOException;

    void fileNotFound();


}
