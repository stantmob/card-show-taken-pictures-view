package br.com.stant.libraries.cameraimagegalleryview.components;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import br.com.stant.libraries.cameraimagegalleryview.activities.CardImageGalleryView;
import br.com.stant.libraries.cameraimagegalleryview.activities.FullScreenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.ItemImageBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class ItemImage extends LinearLayout {

    private final ItemImageBinding mBinding;
    private CardShowTakenImage cardShowTakenImage;
    private CardImageGalleryView mView;
    private Drawable mSelectedDrawableLayer;
    private Drawable mErrorDrawableLayer;

    public ItemImage(Context context, ViewGroup parent, CardImageGalleryView view) {
        super(context);
        mBinding = DataBindingUtil.inflate(LayoutInflater.
                from(super.getContext()), R.layout.item_image, parent, false);

        this.mView = view;
    }

    public CardShowTakenImage getImage() {
        return cardShowTakenImage;
    }

    public void setImage(CardShowTakenImage cardShowTakenImage) {
        this.cardShowTakenImage = cardShowTakenImage;
        this.loadImage();
        this.loadDrawables();
        this.setErrorIfExist();
        this.onClick();
    }

    public View getView() {
        return mBinding.getRoot();
    }

    private void setErrorIfExist() {
        if(this.cardShowTakenImage.hasError()){
            mBinding.imageView.getOverlay().add(mErrorDrawableLayer);
        }
    }

    private void loadImage() {
        setImageBitmapToImageView(mBinding.imageView,
                cardShowTakenImage, 1);
    }

    private void onClick() {
        mBinding.imageView.setOnClickListener(view -> {
            Intent intent = new Intent(mView, FullScreenImage.class);
            intent.putExtra(KEY_IMAGE_FULL_SCREEN, cardShowTakenImage);
            mView.startActivity(intent);
        });
    }

    private void loadDrawables() {
        mSelectedDrawableLayer = getContext().getDrawable(R.drawable.selected_image);
        mErrorDrawableLayer = getContext().getDrawable(R.drawable.image_error_layer);
        mBinding.imageView.post(() -> {
            Rect react = new Rect(
                    0,
                    0,
                    mBinding.imageView.getWidth(),
                    mBinding.imageView.getHeight()
            );
            mSelectedDrawableLayer.setBounds(react);
            mErrorDrawableLayer.setBounds(react);
        });

    }

    public void changeImageToSelectedMode() {
        mBinding.imageView.getOverlay().add(mSelectedDrawableLayer);
    }

    public void removeFromSelectedMode(){
        mBinding.imageView.getOverlay().remove(mSelectedDrawableLayer);
    }


}
