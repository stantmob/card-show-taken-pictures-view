package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryComponentView;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryComponentRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VibratorUtils;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.ItemTouchHelperViewHolder;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

public class CardImageGalleryComponentViewAdapter extends RecyclerView.Adapter<CardImageGalleryComponentViewAdapter.ItemViewHolder> {

    private CardImageGalleryComponentView mView;
    private List<CardShowTakenImage> mCurrentCardImageGalleryList;
    private List<CardShowTakenImage> mOriginalTempCardImageGalleryComponentList;
    private List<CardShowTakenImage> mCardImageGalleryComponentListAsAdded;
    private List<CardShowTakenImage> mCardImageGalleryComponentListAsRemoved;

    public CardImageGalleryComponentViewAdapter(CardImageGalleryComponentView view) {
        mView = view;
        mCurrentCardImageGalleryList = new ArrayList<>(0);
        mOriginalTempCardImageGalleryComponentList = new ArrayList<>(0);
        mCardImageGalleryComponentListAsAdded = new ArrayList<>(0);
        mCardImageGalleryComponentListAsRemoved = new ArrayList<>(0);
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
        CardShowTakenImage cardShowTakenImage = mCurrentCardImageGalleryList.get(position);

        itemViewHolder.mServiceInspectionsFormFilledRecyclerItemBinding.setHandler(mView);

        itemViewHolder.updateView(cardShowTakenImage);
    }

    @Override
    public int getItemCount() {
        return mCurrentCardImageGalleryList.size();
    }

    private void replaceData(List<CardShowTakenImage> imageUrlsList) {
        mCurrentCardImageGalleryList = imageUrlsList;
        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
        notifyDataSetChanged();
    }

    public List<CardShowTakenImage> getCurrentImages() {
        return mCurrentCardImageGalleryList;
    }

    public void saveEditData() {
        notifyDataSetChanged();
    }

    public void cancelEditData() {
        replaceData(mOriginalTempCardImageGalleryComponentList);
    }

    public List<CardShowTakenImage> getImageAsAdded() {
        return mCardImageGalleryComponentListAsAdded;
    }

    public List<CardShowTakenImage> getImagesAsRemoved() {
        return mCardImageGalleryComponentListAsRemoved;
    }

    void saveOriginalList() {
        mOriginalTempCardImageGalleryComponentList = (List) ((ArrayList) mCurrentCardImageGalleryList).clone();
        mCardImageGalleryComponentListAsAdded = new ArrayList<>();
        mCardImageGalleryComponentListAsRemoved = new ArrayList<>();
    }

    public void removeImage(View view, CardShowTakenImage cardShowTakenImage) {
        int position = mCurrentCardImageGalleryList.indexOf(cardShowTakenImage);

        mCurrentCardImageGalleryList.remove(cardShowTakenImage);
        mCardImageGalleryComponentListAsRemoved.add(cardShowTakenImage);

        if (cardShowTakenImage != null) {
            mCardImageGalleryComponentListAsAdded.remove(cardShowTakenImage);
        }

        notifyItemRemoved(position);
        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
    }

    public void addPicture(CardShowTakenImage cardShowTakenImage) {
        mCurrentCardImageGalleryList.add(cardShowTakenImage);
        mCardImageGalleryComponentListAsAdded.add(cardShowTakenImage);

        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
        notifyItemInserted(mCurrentCardImageGalleryList.size());
    }

    public void addPictures(List<CardShowTakenImage> cardShowTakenImages) {
        for (CardShowTakenImage cardShowTakenImage : cardShowTakenImages) {
            addPicture(cardShowTakenImage);
        }

        mCardImageGalleryComponentListAsAdded.addAll(cardShowTakenImages);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private CardImageGalleryComponentRecycleItemBinding mServiceInspectionsFormFilledRecyclerItemBinding;

        ItemViewHolder(CardImageGalleryComponentRecycleItemBinding serviceInspectionsFormFilledRecyclerItemBinding) {
            super(serviceInspectionsFormFilledRecyclerItemBinding.getRoot());
            mServiceInspectionsFormFilledRecyclerItemBinding = serviceInspectionsFormFilledRecyclerItemBinding;
        }

        void updateView(CardShowTakenImage cardShowTakenImage) {
            setImageBitmapToImageView(mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryAvatar,
                    cardShowTakenImage, 8);
        }

        @Override
        public void onItemSelected() {
            VibratorUtils.vibrate(mView.getContext(), 400);
            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryComponentConstraintLayout.setAlpha(0.75f);
            //implementar
        }

        @Override
        public void onItemClear() {
            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryComponentConstraintLayout.setAlpha(1);
            //implementar
        }
    }
}
