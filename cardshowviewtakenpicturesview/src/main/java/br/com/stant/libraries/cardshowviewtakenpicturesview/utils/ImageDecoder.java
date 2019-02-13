package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;
import static io.reactivex.Single.just;

public class ImageDecoder {

    static Bitmap scaleBitmap(@NonNull Bitmap bitmap, @NonNull Integer desiredSize) {
        Integer width  = bitmap.getWidth();
        Integer height = bitmap.getHeight();

        Integer idealWidth  = calculateIdealWidth(width, height, desiredSize);
        Integer idealHeight = calculateIdealHeight(width, height, desiredSize);

        return Bitmap.createScaledBitmap(bitmap, idealWidth, idealHeight, false);
    }

    private static Integer calculateIdealWidth(Integer width, Integer height, Integer desiredWidth) {
        if (width >= height) {
            return desiredWidth;
        } else {
            Double idealWidthAsDouble = ((width.doubleValue() / height) * desiredWidth);
            return idealWidthAsDouble.intValue();
        }
    }

    private static Integer calculateIdealHeight(Integer width, Integer height, Integer desiredHeight) {
        if (height >= width) {
            return desiredHeight;
        } else {
            Double idealHeightAsDouble = ((height.doubleValue() / width) * desiredHeight);
            return idealHeightAsDouble.intValue();
        }
    }

    static Bitmap getBitmapFromFileSync(@NonNull String localPhoto, @NonNull Integer desiredSize) {
        Options options = new Options();

        final int defaultSampleSize = 1;
        options.inSampleSize        = defaultSampleSize;
        options.inJustDecodeBounds  = true;

        BitmapFactory.decodeFile(localPhoto, options);

        options.inSampleSize       = calculateInSampleSize(options, desiredSize, desiredSize);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(localPhoto, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width  = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth  = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void getBitmapFromFile(String filePath, Integer sampleSize, BitmapFromFileCallback callback) {
        Options options = getOptions(sampleSize);

        if (fileExits(filePath)) {
            final Disposable subscribe = just(BitmapFactory.decodeFile(filePath, options))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            (bitmap) -> {
                                try {
                                    callback.onBitmapDecoded(bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
        } else {
            callback.fileNotFound();
        }
    }

    public static void getBitmapFromFile(File localPath, String fileName, Integer sampleSize, BitmapFromFileCallback callback) {
        Options options = getOptions(sampleSize);
        String filePath = localPath.toString() + "/" + fileName;

        if (fileExits(filePath)) {
            final Disposable subscribe = just(BitmapFactory.decodeFile(localPath.toString() + "/" + fileName, options))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            (bitmap) -> {
                                try {
                                    callback.onBitmapDecoded(bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
        } else {
            callback.fileNotFound();
        }
    }

    private static boolean fileExits(String filePath) {
        File file = new File(filePath);

        return file.exists() && !file.isDirectory();
    }

    private static Options getOptions(Integer sampleSize) {
        Options options = new Options();

        options.inSampleSize       = sampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig  = Bitmap.Config.ARGB_8888;

        return options;
    }

    public static void setImageBitmapToImageView(ImageView imageView, CardShowTakenImage cardShowTakenImage, Integer sampleSize) {
        if (cardShowTakenImage.hasOnlyRemoteUrl()) {
            setBitmapFromInternet(imageView, cardShowTakenImage.getRemoteImageUrl());
        } else if (cardShowTakenImage.hasLocalImage()) {
            getBitmapFromFile(getPrivateTempDirectory(imageView.getContext()), cardShowTakenImage.getLocalImageFilename(), sampleSize, new BitmapFromFileCallback() {
                @Override
                public void onBitmapDecoded(Bitmap bitmap) throws IOException {
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void fileNotFound() {

                }
            });
        } else {
            getBitmapFromFile(cardShowTakenImage.getTempImagePathToShow(), sampleSize, new BitmapFromFileCallback() {
                @Override
                public void onBitmapDecoded(Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void fileNotFound() {

                }
            });
        }
    }

    private static void setBitmapFromInternet(ImageView imageView, String url) {
        if (url == null || url.isEmpty()) return;

        try {
            Glide.with(imageView.getContext()).applyDefaultRequestOptions(
                    new RequestOptions().placeholder(R.drawable.stant_city).centerInside()
                            .override(500, 500)).load(url).into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Integer getImageQualityPercent(Bitmap bitmap) {
        final int fullPercentage = 90;
        final Double idealSize   = 15000000.0;

        int fullSize      = bitmap.getByteCount();
        Double percentage = fullPercentage * (idealSize / fullSize);

        if (percentage > fullPercentage) {
            return fullPercentage;
        }

        return percentage.intValue();
    }


}
