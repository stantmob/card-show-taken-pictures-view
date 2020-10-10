package br.com.stant.libraries.cardshowviewtakenpicturesview.utils.bindings;

import androidx.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImageBinding {

    @BindingAdapter({"cardImgUrl", "cardImgSize", "cardImgHolder"})
    public static void loadCardImage(ImageView imageView, String url, String size, Drawable holder) {
        if (hasNoUlr(url)) return;

        try {
            Uri uri = Uri.parse(url);

            final Disposable subscribe = Single.just(Glide.with(imageView.getContext()).load(uri))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            (requestBuilder) ->
                                    requestBuilder.into(new SimpleTarget<Drawable>(Integer.parseInt(size),
                                            Integer.parseInt(size)) {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            imageView.setImageDrawable(resource);
                                        }
                                    })
                    );

        } catch (Exception e) {
            System.out.println("error on image");
        }


    }

    @BindingAdapter({"cardImgUrl", "cardImgHolder"})
    public static void loadPreviewImage(ImageView imageView, String url, Drawable holder) {
        if (hasNoUlr(url)) return;

        try {
            Glide.with(imageView.getContext()).load(url)
                    .apply(new RequestOptions().placeholder(holder)).into(imageView);
        } catch (Exception e) {
            System.out.println(" error vei");
        }
    }

    private static boolean hasNoUlr(String url) {
        return url == null || url.equals("");
    }


}
