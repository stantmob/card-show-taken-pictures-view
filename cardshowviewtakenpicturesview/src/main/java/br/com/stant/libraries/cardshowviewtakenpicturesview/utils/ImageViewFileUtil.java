package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageViewFileUtil {

    public static final String JPG_FILE_SUFFIX = ".jpg";
    public static final String JPG_FILE_PREFIX = "IMG-";

    public static File getPrivateTempDirectory(Context context) {
        return context.getFilesDir();
    }

    public static File getPublicAlbumDirectoryAtPictures(String albumName) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);

        if (directoryDoesNotExists(directory)) {
            if (!createDirectory(directory)) {
                Log.e(ImageGenerator.class.getCanonicalName(), "Directory not created");
                return null;
            }
        }

        return directory;
    }

    private static boolean directoryDoesNotExists(File directory) {
        return !directory.exists();
    }

    private static boolean createDirectory(File directory) {
        return directory.mkdirs();
    }

    static Bitmap rotateImage(Bitmap source, float angle, String tagText) throws OutOfMemoryError {
        Matrix matrix = new Matrix();

        if (angle % 180 == 0) {
            matrix.postRotate(angle);
        } else {
            matrix.postRotate(angle + 180);
        }

        Bitmap createdBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        createdBitmap = ImageWatermarkUtil.Companion.addWatermark(
                createdBitmap,
                tagText,
                new ImageWatermarkUtil.WatermarkOptions()
        );

        return createdBitmap;
    }

    static Bitmap flipImage(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static boolean deleteFile(Context context, String localImage) {
        File directory = getPrivateTempDirectory(context);
        File image     = new File(directory.getAbsolutePath() + File.separator + localImage);

        if (image.exists() && !image.isDirectory()) {
            return image.delete();
        }

        return false;
    }


}

