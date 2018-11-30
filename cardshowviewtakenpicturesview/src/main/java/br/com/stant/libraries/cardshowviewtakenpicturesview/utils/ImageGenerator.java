package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract.CardShowTakenCompressedCallback;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_PREFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_SUFFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPublicAlbumDirectoryAtPictures;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.rotateImage;
import static io.reactivex.Observable.just;

public class ImageGenerator {

    public static final Integer fromCameraBack  = 1;
    public static final Integer fromGallery     = 2;
    public static final Integer fromCameraFront = 3;

    private Context mContext;

    public ImageGenerator(Context context) {
        this.mContext = context;
    }

    public void generateCardShowTakenImageFromCamera(Bitmap bitmap, Integer photoType, Integer orientation,
                                                     CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        File tempImagePathToShow = createTempImageFileToShow(bitmap, photoType, orientation);

        cardShowTakenCompressedCallback.onSuccess(bitmap, tempImagePathToShow.getName(), tempImagePathToShow.toString());
    }

    public void generateCardShowTakenImageFromCamera(File photoTaken,
                                                     CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        if (photoTaken == null) {
            return;
        }

        final File file = new File(getPrivateTempDirectory(mContext) + "/" + photoTaken.getName());

        final Integer sampleSizeForSmallImages = 2;
        getBitmapFromFile(photoTaken.getAbsolutePath(), sampleSizeForSmallImages, new BitmapFromFileCallback() {
            @Override
            public void onBitmapDecoded(Bitmap bitmap) throws IOException {
                cardShowTakenCompressedCallback.onSuccess(bitmap, photoTaken.getName(), file.toString());
            }

            @Override
            public void fileNotFound() {

            }
        });
    }

    public void generateCardShowTakenImageFromImageGallery(Uri data, Integer photoType,
                                                           CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        try {
            final Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), data);

            final Integer desiredSize = 1400;
            final Bitmap scaledBitmap = ImageDecoder.scaleBitmap(bitmap, desiredSize);

            File tempImagePathToShow = createTempImageFileToShow(scaledBitmap, photoType, null);
            cardShowTakenCompressedCallback.onSuccess(bitmap, tempImagePathToShow.getName(), tempImagePathToShow.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createTempImageFileToShow(Bitmap bitmap, Integer typePhoto, Integer orientation) {
        String uuid = UUID.randomUUID().toString();
        File file   = new File(ImageViewFileUtil.getPrivateTempDirectory(mContext).toString() + "/" + JPG_FILE_PREFIX + uuid + JPG_FILE_SUFFIX);

        final Integer desiredSize = 1400;
        Bitmap scaledBitmap       = ImageDecoder.scaleBitmap(bitmap, desiredSize);
        final int quality         = ImageDecoder.getImageQualityPercent(scaledBitmap);

        try {
            FileOutputStream fileOutputStream  = mContext.openFileOutput(file.getName(), Context.MODE_PRIVATE);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (typePhoto.equals(fromCameraBack) || typePhoto.equals(fromCameraFront)) {
                saveInPictures(scaledBitmap, orientation, uuid);

                try {
                    rotateImage(scaledBitmap, orientation).compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                } catch (OutOfMemoryError outOfMemoryError) {
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                }
            } else if (typePhoto.equals(fromGallery)) {
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            }

            fileOutputStream.write(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void saveInPictures(Bitmap bitmap, Integer orientation, String uuid){
        try {
            subscribeSaveImageInPicturesThread(rotateImage(bitmap, orientation), uuid);
        } catch (OutOfMemoryError outOfMemoryError) {
            subscribeSaveImageInPicturesThread(bitmap, uuid);
        }
    }

    private void subscribeSaveImageInPicturesThread(Bitmap bitmap, String uuid) {
        if (isExternalStorageWritable()) {
            try {
                final Calendar calendar = Calendar.getInstance();
                final String todayDate  = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

                final File directory              = getPublicAlbumDirectoryAtPictures("Stant");
                final File imageFile              = File.createTempFile(todayDate + "-" + JPG_FILE_PREFIX + calendar.getTimeInMillis(), JPG_FILE_SUFFIX, directory);
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);

                final Disposable subscribe = just(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream))
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(
                                (compressed) -> {
                                    if (compressed) {
                                        MediaScannerConnection.scanFile(mContext,
                                                new String[]{imageFile.toString()}, null, null
                                        );
                                    }
                                }
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


}
