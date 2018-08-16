package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewImagesAdapter;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraPhotosAdapter;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil.JPEG_FILE_SUFFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil.getFile;

public class ImageGenerator {

    private CardContract mCardContract;
    private Context mContext;
    private File mCompressedImage = null;

    public ImageGenerator(Context context, CardContract cardContract) {
        this.mContext      = context;
        this.mCardContract = cardContract;
    }

    public void generateCardShowTakenImageFromCamera(Bitmap bitmap,
                                                     CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {

        File tempImagePathToShow = createTempImageFileToShow(bitmap);

        cardShowTakenCompressedCallback.onSuccess(bitmap, tempImagePathToShow.getName(), tempImagePathToShow.toString());
    }

    public void generateCardShowTakenImageFromCamera(File photoTaken,
                                                     CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        if (photoTaken == null) {
            return;
        }

        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(photoTaken.getAbsolutePath());

        File file = new File(getFile() + "/" + photoTaken.getName());

        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, photoTaken.getName(), file.toString());
    }

    public void generateCardShowTakenImageFromImageGallery(Uri data,
                                                           CardShowTakenPictureViewContract.CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        File photoTaken = new File(PhotoViewFileUtil.getFile().toString());

        try {
            photoTaken = PhotoViewFileUtil.from(mContext, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(photoTaken.getAbsolutePath());
        File tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath);

        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, tempImagePathToShow.getName(), tempImagePathToShow.toString());
    }

    private File createTempImageFileToShow(Bitmap bitmap) {
        String uuid = UUID.randomUUID().toString();
        File file = new File(PhotoViewFileUtil.getFile().toString() + "/" + uuid + JPEG_FILE_SUFFIX);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return file;
    }


}
