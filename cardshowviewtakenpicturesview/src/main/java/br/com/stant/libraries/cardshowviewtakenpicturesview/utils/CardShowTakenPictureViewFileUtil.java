package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

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
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CardShowTakenPictureViewFileUtil {

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;
    private static final String TEMP_IMAGE_NAME = "image_temp_stant";
    private static String path = "/<br.com.stant>/temp";

    public static File getFile() {
        return new File(
                Environment.getExternalStorageDirectory(),
                path);
    }

    public static Bitmap decodeBitmapFromFile(String path) {
        Bitmap result;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        BitmapFactory.decodeFile(path, options);
        try {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            result = BitmapFactory.decodeFile(path, options);

            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    result = rotateBitmap(result, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    result = rotateBitmap(result, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    result = rotateBitmap(result, 270);
                    break;
            }

        } catch (OutOfMemoryError oe) {
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inSampleSize = 4;
            result = BitmapFactory.decodeFile(path, options);
        } catch (IOException io) {
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            options.inSampleSize = 4;
            result = BitmapFactory.decodeFile(path, options);
        }

        return result;
    }

    public static Bitmap getBitMapFromFile(String fileName, File sdcard) {
        File[] files = getFiles(fileName, sdcard);
        if (files != null && files.length > 0) {
            return decodeBitmapFromFile(files[files.length - 1].getAbsolutePath());
        }
        return null;
    }

    private static File[] getFiles(final String fileName, File sdcard) {
        return sdcard.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(fileName) && ((name.endsWith(".jpg")) || (name.endsWith(".png")));
            }
        });
    }

    public static void createTempDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getFile();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private static File setUpPhotoFile() throws IOException {
        File file = createImageFile();
        return file;
    }

    public static File prepareFile(Intent takePictureIntent) {
        File file = null;
        try {
            file = setUpPhotoFile();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static File from(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        String fileName = getFileName(context, uri);
        String[] splitName = splitFileName(fileName);
        File tempFile = File.createTempFile(splitName[0], splitName[1]);
        tempFile = rename(tempFile, fileName);
        tempFile.deleteOnExit();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            copy(inputStream, out);
            inputStream.close();
        }

        if (out != null) {
            out.close();
        }
        return tempFile;
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old " + newName + " file");
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

}

