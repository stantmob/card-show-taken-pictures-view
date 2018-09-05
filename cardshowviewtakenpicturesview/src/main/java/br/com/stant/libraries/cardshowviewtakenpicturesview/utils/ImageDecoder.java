package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class ImageDecoder {

    public static Bitmap getBitmapFromFile(String localPhoto){
        return BitmapFactory.decodeFile(localPhoto);
    }

    public static Bitmap getBitmapFromFile(File localPath, String fileName){
        return BitmapFactory.decodeFile(localPath.toString() + "/" + fileName);
    }

    public static Integer getImagePercentProportion(Bitmap bitmap){

        int size = bitmap.getByteCount();

        return 100 - (10000000 / size);
    }

}