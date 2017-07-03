package br.com.stant.libraries.cardshowviewtakenpicturesview.utils.bindings;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by stant on 13/01/17.
 */

public class ImageBinding {

    @BindingAdapter({"imgUrl","imgSize","imgHolder"})
    public static void loadImage(ImageView imageView, String url, String size, Drawable holder){
        if (hasNoUlr(url)) return;

        try{
            Picasso.with(imageView.getContext())
                    .load(url)
                    .resize(Integer.valueOf(size), Integer.valueOf(size))
                    .centerCrop()
                    .placeholder(holder)
                    .into(imageView);
        }
        catch (Exception e){
            System.out.println("error on image");
        }
    }

    @BindingAdapter({"imgUrl","imgHolder"})
    public static void simpleLoadImage(ImageView imageView, String url, Drawable holder){
        if (hasNoUlr(url)) return;

        try {

            Picasso.with(imageView.getContext())
                    .load(url)
                    .fit()
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
