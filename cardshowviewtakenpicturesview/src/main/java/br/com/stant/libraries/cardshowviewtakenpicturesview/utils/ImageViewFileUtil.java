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

    public static final String JPG_FILE_SUFFIX   = ".jpg";
    public static final String JPG_FILE_PREFIX   = "IMG-";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF                 = -1;

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

    public static Bitmap rotateImage(Bitmap source, float angle) throws OutOfMemoryError {
        Matrix matrix = new Matrix();

        if (angle % 180 == 0) {
            matrix.postRotate(angle);
        } else {
            matrix.postRotate(angle + 180);
        }

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static File from(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        String fileName         = getFileName(context, uri);
        String[] splitName      = splitFileName(fileName);
        File tempFile           = File.createTempFile(splitName[0], splitName[1]);
        tempFile                = rename(tempFile, fileName);

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

    public static boolean deleteFile(Context context, String localImage) {
        File directory = getPrivateTempDirectory(context);
        File image     = new File(directory.getAbsolutePath() + File.separator + localImage);

        if (image.exists() && !image.isDirectory()) {
            return image.delete();
        }

        return false;
    }


}

