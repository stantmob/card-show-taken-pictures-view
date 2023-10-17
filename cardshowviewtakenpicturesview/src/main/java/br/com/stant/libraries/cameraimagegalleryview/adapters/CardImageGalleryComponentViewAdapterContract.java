package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryComponentView;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryComponentRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

public class CardImageGalleryComponentViewAdapterContract
        extends RecyclerView.Adapter<CardImageGalleryComponentViewAdapterContract.ItemViewHolder> {

    private CardImageGalleryComponentView mView;
    private ItemTouchHelper mItemTouchHelper;

    private CardShowTakenImageInjection mCardShowTakenImages;


    public CardImageGalleryComponentViewAdapterContract(CardImageGalleryComponentView view) {
        mView = view;
        mCardShowTakenImages = CardShowTakenImageInjection.getCardShowTakenPictureInjection();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardImageGalleryComponentRecycleItemBinding mCardImageGalleryComponentRecycleItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.card_image_gallery_component_recycle_item,
                parent,
                false);
        return new ItemViewHolder(mCardImageGalleryComponentRecycleItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int position) {
        CardShowTakenImage cardShowTakenImage = mCardShowTakenImages.getAll().get(position);

        itemViewHolder.mServiceInspectionsFormFilledRecyclerItemBinding.setHandler(mView);
        itemViewHolder.updateView(cardShowTakenImage);
    }

    @Override
    public int getItemCount() {
        return mCardShowTakenImages.getAll().size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private CardImageGalleryComponentRecycleItemBinding mServiceInspectionsFormFilledRecyclerItemBinding;

        ItemViewHolder(CardImageGalleryComponentRecycleItemBinding serviceInspectionsFormFilledRecyclerItemBinding) {
            super(serviceInspectionsFormFilledRecyclerItemBinding.getRoot());
            mServiceInspectionsFormFilledRecyclerItemBinding = serviceInspectionsFormFilledRecyclerItemBinding;
        }

        void updateView(CardShowTakenImage cardShowTakenImage) {
            setImageBitmapToImageView(mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryAvatar,
                    cardShowTakenImage, 8);
            if(cardShowTakenImage.hasError()){
                mView.showStrokeError();
            }
            mServiceInspectionsFormFilledRecyclerItemBinding.setCardShowTakenImage(cardShowTakenImage);
            mServiceInspectionsFormFilledRecyclerItemBinding.executePendingBindings();
        }
    }
}
