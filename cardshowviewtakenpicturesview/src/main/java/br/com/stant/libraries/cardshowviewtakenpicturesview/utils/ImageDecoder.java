package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import java.io.File;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;

public class ImageDecoder {

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

    public static void setImageBitmapToImageView(ImageView imageView, CardShowTakenImage cardShowTakenImage, Integer sampleSize) {
        if (cardShowTakenImage.hasOnlyRemoteUrl()) {
            ImageDecoder.setBitmapFromInternet(imageView, cardShowTakenImage.getRemoteImageUrl());
        } else if (cardShowTakenImage.hasLocalImage()) {
            imageView.setImageBitmap(ImageDecoder.getBitmapFromFile(getFile(), cardShowTakenImage.getLocalImageFilename(), sampleSize));
        } else {
            imageView.setImageBitmap(ImageDecoder.getBitmapFromFile(cardShowTakenImage.getTempImagePathToShow(), sampleSize));
        }
    }

    public static void setImageBitmapToImageView(CircularImageView circularImageView, CardShowTakenImage cardShowTakenImage, Integer sampleSize) {
        if (cardShowTakenImage.hasOnlyRemoteUrl()) {
            ImageDecoder.setBitmapFromInternet(circularImageView, cardShowTakenImage.getRemoteImageUrl());
        } else if (cardShowTakenImage.hasLocalImage()) {
            circularImageView.setImageBitmap(ImageDecoder.getBitmapFromFile(getFile(), cardShowTakenImage.getLocalImageFilename(), sampleSize));
        } else {
            circularImageView.setImageBitmap(ImageDecoder.getBitmapFromFile(cardShowTakenImage.getTempImagePathToShow(), sampleSize));
        }
    }

    private static void setBitmapFromInternet(ImageView imageView, String url) {
        if (url == null || url.isEmpty()) return;

        try{
            Picasso.with(imageView.getContext())
                    .load(url)
                    .resize(55, 55)
                    .centerCrop()
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
