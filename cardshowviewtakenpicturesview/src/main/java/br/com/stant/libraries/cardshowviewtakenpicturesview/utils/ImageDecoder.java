package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import java.io.File;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;
import static io.reactivex.Observable.fromCallable;

public class ImageDecoder {

    public static Bitmap getBitmapFromFileSync(String localPhoto, Integer sampleSize){
        Options options = getOptions(sampleSize);

        return BitmapFactory.decodeFile(localPhoto, options);
    }

    public static void getBitmapFromFile(String localPhoto, Integer sampleSize, BitmapFromFileCallback callback){
        Options options = getOptions(sampleSize);

        fromCallable(() -> BitmapFactory.decodeFile(localPhoto, options))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        callback::onBitmapDecoded
                );
    }

    public static void getBitmapFromFile(File localPath, String fileName, Integer sampleSize, BitmapFromFileCallback callback){
        Options options = getOptions(sampleSize);


        fromCallable(() -> BitmapFactory.decodeFile(localPath.toString() + "/" + fileName, options))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        callback::onBitmapDecoded
                );
    }

    private static Options getOptions(Integer sampleSize) {
        Options options = new Options();

        options.inSampleSize      = sampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

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

    public static Integer getImageQualityPercent(Bitmap bitmap){
        final int fullPercentage = 70;
        final Double idealSize   = 10000000.0;

        int fullSize      = bitmap.getByteCount();
        Double percentage = fullPercentage*(idealSize/fullSize);

        if (percentage > fullPercentage) {
            return fullPercentage;
        }

        return percentage.intValue();
    }


}
