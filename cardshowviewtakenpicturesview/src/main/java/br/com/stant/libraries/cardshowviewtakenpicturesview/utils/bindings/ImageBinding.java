package br.com.stant.libraries.cardshowviewtakenpicturesview.utils.bindings;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by stant on 13/01/17.
 */

public class ImageBinding {

    @BindingAdapter({"cardImgUrl","cardImgSize","cardImgHolder"})
    public static void loadCardImage(ImageView imageView, String url, String size, Drawable holder){
        if (hasNoUlr(url)) return;

        try{
            Picasso.with(imageView.getContext())
                    .load(url)
                    .resize(Integer.valueOf(size), Integer.valueOf(size))
                    .centerCrop()
                    .placeholder(holder)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
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
            System.out.println("error on image");
        }
    }

    @BindingAdapter({"cardImgUrl","cardImgHolder"})
    public static void loadPreviewImage(ImageView imageView, String url, Drawable holder){
        if (hasNoUlr(url)) return;

        try {
            Picasso.with(imageView.getContext())
                    .load(url)
                    .placeholder(holder)
                    .into(imageView);
        }
        catch (Exception e){
            System.out.println(" error vei");
        }
    }

    private static boolean hasNoUlr(String url){
        return url == null || url.equals("");
    }
}
