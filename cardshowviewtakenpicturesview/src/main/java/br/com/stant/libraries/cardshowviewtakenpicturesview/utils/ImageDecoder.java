package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import java.io.File;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;
import static io.reactivex.Observable.just;

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
            Double idealWidthAsDouble = ((width.doubleValue()/height) * desiredWidth);
            return idealWidthAsDouble.intValue();
        }
    }

    private static Integer calculateIdealHeight(Integer width, Integer height, Integer desiredHeight) {
        if (height >= width) {
            return desiredHeight;
        } else {
            Double idealHeightAsDouble = ((height.doubleValue()/width) * desiredHeight);
            return idealHeightAsDouble.intValue();
        }
    }

    static Bitmap getBitmapFromFileSync(@NonNull String localPhoto, @NonNull Integer desiredSize){
        Options options = new Options();

        options.inSampleSize       = 1;
        options.inJustDecodeBounds = true;

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

    public static void getBitmapFromFile(String localPhoto, Integer sampleSize, BitmapFromFileCallback callback) {
        Options options = getOptions(sampleSize);

        just(BitmapFactory.decodeFile(localPhoto, options))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        callback::onBitmapDecoded
                );
    }

    public static void getBitmapFromFile(File localPath, String fileName, Integer sampleSize, BitmapFromFileCallback callback) {
        Options options = getOptions(sampleSize);

        just(BitmapFactory.decodeFile(localPath.toString() + "/" + fileName, options))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        callback::onBitmapDecoded
                );
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
            getBitmapFromFile(getFile(), cardShowTakenImage.getLocalImageFilename(), sampleSize, imageView::setImageBitmap);
        } else {
            getBitmapFromFile(cardShowTakenImage.getTempImagePathToShow(), sampleSize, imageView::setImageBitmap);
        }
    }

    private static void setBitmapFromInternet(ImageView imageView, String url) {
        if (url == null || url.isEmpty()) return;

        try{
            Picasso.Builder builder = new Picasso.Builder(imageView.getContext());
            builder.listener(
                    (picasso, uri, exception) -> exception.printStackTrace()
            );

            builder.build()
                    .load(url)
                    .resize(500, 500)
                    .centerInside()
                    .placeholder(R.drawable.stant_city)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
                            imageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                            imageView.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            imageView.setImageDrawable(placeHolderDrawable);
                        }
                    });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    static Integer getImageQualityPercent(Bitmap bitmap){
        final int fullPercentage = 80;
        final Double idealSize   = 10000000.0;

        int fullSize      = bitmap.getByteCount();
        Double percentage = fullPercentage*(idealSize/fullSize);

        if (percentage > fullPercentage) {
            return fullPercentage;
        }

        return percentage.intValue();
    }


}
