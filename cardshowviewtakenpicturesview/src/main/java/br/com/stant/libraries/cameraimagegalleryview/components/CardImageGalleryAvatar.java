package br.com.stant.libraries.cameraimagegalleryview.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import br.com.stant.libraries.cameraimagegalleryview.activities.CardImageGalleryView;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryAvatarBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder;

public class CardImageGalleryAvatar extends LinearLayout {

    private final CardImageGalleryAvatarBinding mBinding;

    public CardImageGalleryAvatar(Context context, ViewGroup parent, CardShowTakenImage image) {
        super(context);

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.card_image_gallery_avatar, parent, true);
        mBinding.cardImageGalleryContainer.setTranslationZ(-image.getOrder());
        ImageDecoder.setImageBitmapToImageView(mBinding.cardImageGalleryAvatar, image, 1);

    }
}
