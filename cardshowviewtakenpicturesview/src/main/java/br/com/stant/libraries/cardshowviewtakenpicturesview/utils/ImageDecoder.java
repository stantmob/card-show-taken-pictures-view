package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class ImageDecoder {

    private static final int fullPercentage = 80;
    private static final Double idealSize   = 10000000.0;

    public static Bitmap getBitmapFromFile(String localPhoto, Integer sampleSize){
        BitmapFactory.Options options = getOptions(sampleSize);

        return BitmapFactory.decodeFile(localPhoto, options);
    }

    public static Bitmap getBitmapFromFile(File localPath, String fileName, Integer sampleSize){
        BitmapFactory.Options options = getOptions(sampleSize);

        return BitmapFactory.decodeFile(localPath.toString() + "/" + fileName, options);
    }

    private static BitmapFactory.Options getOptions(Integer sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize      = sampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return options;
    }

    public static Integer getImageQualityPercent(Bitmap bitmap){
        int fullSize      = bitmap.getByteCount();
        Double percentage = fullPercentage*(idealSize/fullSize);

        if (percentage > fullPercentage) {
            return 80;
        }

        return percentage.intValue();
    }

}