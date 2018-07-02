package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * this class provide methods that can help compress the image size.
 *
 */
public class ImageCompressHelper {

    /**
     * Calcuate how much to compress the image
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
// Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * resize image to 480x800
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {

        File file = new File(filePath);
        long originalSize = file.length();


        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize based on a preset ratio
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap compressedImage = BitmapFactory.decodeFile(filePath, options);

        return compressedImage;
    }


    public void compressBitmap(File file, int sampleSize, int quality) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            FileInputStream inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream("location to save");
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.close();
            long lengthInKb = 1024; //in kb
            if (lengthInKb > 1000) {
                compressBitmap(file, (sampleSize*2), (quality/4));
            }

            selectedBitmap.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
