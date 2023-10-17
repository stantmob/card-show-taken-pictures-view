package br.com.stant.libraries.cameraimagegalleryview.components;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

    private final ItemImageBinding itemImageBinding;
    private CardShowTakenImage cardShowTakenImage;
    private CardImageGalleryView mView;

    public ItemImage(Context context, ViewGroup parent, CardImageGalleryView view) {
        super(context);
        itemImageBinding = DataBindingUtil.inflate(LayoutInflater.
                from(super.getContext()), R.layout.item_image, parent, false);
        this.mView = view;
    }


    public void setImage(CardShowTakenImage cardShowTakenImage) {
        this.cardShowTakenImage = cardShowTakenImage;
        this.loadImage();
        this.setErrorIfExist();
        this.onClick();
        this.onLongClick();
        itemImageBinding.setSelected(false);
    }

    public View getView() {
        return itemImageBinding.getRoot();
    }

    private void setErrorIfExist() {
        this.itemImageBinding.setHasError(this.cardShowTakenImage.hasError());
    }

    private void loadImage() {
        setImageBitmapToImageView(itemImageBinding.imageView,
                cardShowTakenImage, 8);
    }

    private void onClick() {
        itemImageBinding.imageView.setOnClickListener(view -> {
            if (mView.isSelectModeOn() && !itemImageBinding.getSelected()) {
                changeImageToSelectedMode();
            } else if (mView.isSelectModeOn()) {
                mView.removeImageSelectFromSelectionMode(cardShowTakenImage);
                itemImageBinding.setSelected(false);
            } else {
                Intent intent = new Intent(mView, FullScreenImage.class);
                intent.putExtra(KEY_IMAGE_FULL_SCREEN, cardShowTakenImage);
                mView.startActivity(intent);
            }
        });
    }

    private void onLongClick() {
        itemImageBinding.imageView.setOnLongClickListener(view -> {
            if (!itemImageBinding.getSelected()) {
                changeImageToSelectedMode();
                return true;
            }
            return false;
        });
    }

    private void changeImageToSelectedMode() {
        mView.addImageSelect(cardShowTakenImage);
        itemImageBinding.setSelected(true);
    }

}
