package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import io.reactivex.Observable;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder> {

    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalTempCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mCardShowTakenImageListAsRemoved;
    private CardShowTakenPictureView mView;

    public CardShowTakenPictureViewImagesAdapter(List<CardShowTakenImage> imageUrlsList, CardShowTakenPictureView view) {
        this.mCurrentCardShowTakenImageList      = imageUrlsList;
        this.mView                               = view;
        this.mOriginalTempCardShowTakenImageList = new ArrayList<>();
        this.mCardShowTakenImageListAsAdded      = new ArrayList<>();
        this.mCardShowTakenImageListAsRemoved    = new ArrayList<>();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardShowTakenPictureViewImageRecycleItemBinding mCardShowTakenPictureViewImageRecycleItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.card_show_taken_picture_view_image_recycle_item,
                parent,
                false);

        final ItemViewHolder vh = new ItemViewHolder(mCardShowTakenPictureViewImageRecycleItemBinding);
        return vh;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        CardShowTakenImage cardShowTakenImage = mCurrentCardShowTakenImageList.get(position);

        holder.mServiceInspectionsFormFilledRecycleItemBinding.setHandler(this);

        holder.updateView(cardShowTakenImage);
    }

    public void replaceData(List<CardShowTakenImage> imageUrlsList) {
        mCurrentCardShowTakenImageList = imageUrlsList;
        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
        notifyDataSetChanged();
    }

    public List<CardShowTakenImage> getData() {
        return mCurrentCardShowTakenImageList;
    }

    public void saveEditData() {
        notifyDataSetChanged();
    }

    public void cancelEditData() {
        replaceData(mOriginalTempCardShowTakenImageList);
    }

    public List<CardShowTakenImage> getImagesAsAdded() {
        return mCardShowTakenImageListAsAdded;
    }

    public List<CardShowTakenImage> getImagesAsRemoved() {
        return mCardShowTakenImageListAsRemoved;
    }

    void saveOriginalList() {
        mOriginalTempCardShowTakenImageList = ((List) ((ArrayList) mCurrentCardShowTakenImageList).clone());
        mCardShowTakenImageListAsAdded      = new ArrayList<>();
        mCardShowTakenImageListAsRemoved    = new ArrayList<>();
    }

    public void removeImage(View view, CardShowTakenImage cardShowTakenImage) {
        int position = mCurrentCardShowTakenImageList.indexOf(cardShowTakenImage);

        mCurrentCardShowTakenImageList.remove(cardShowTakenImage);
        mCardShowTakenImageListAsRemoved.add(cardShowTakenImage);

        if (hasCardShowTakenImageAsAdded(cardShowTakenImage))
            mCardShowTakenImageListAsAdded.remove(cardShowTakenImage);

        notifyItemRemoved(position);

        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
    }

    private boolean hasCardShowTakenImageAsAdded(CardShowTakenImage cardShowTakenImage) {
        Observable<CardShowTakenImage> cardShowTakenImageObservable = Observable.just(cardShowTakenImage);

        cardShowTakenImageObservable.filter(cardShowTakenImageAsAdded -> cardShowTakenImage.equals(cardShowTakenImageAsAdded));

        CardShowTakenImage cardShowTakenImageAsRemoved = cardShowTakenImageObservable.blockingFirst();

        return cardShowTakenImageAsRemoved != null;
    }

    public void addPicture(CardShowTakenImage cardShowTakenImage) {
        mCurrentCardShowTakenImageList.add(cardShowTakenImage);
        mCardShowTakenImageListAsAdded.add(cardShowTakenImage);
        notifyItemInserted(mCurrentCardShowTakenImageList.size());
        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
    }

    public void addPictures(List<CardShowTakenImage> cardShowTakenImages) {
        for (CardShowTakenImage cardShowTakenImage :
                cardShowTakenImages) {
            addPicture(cardShowTakenImage);
        }

        mCardShowTakenImageListAsAdded.addAll(cardShowTakenImages);
    }

    @Override
    public int getItemCount() {
        return mCurrentCardShowTakenImageList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private CardShowTakenPictureViewImageRecycleItemBinding mServiceInspectionsFormFilledRecycleItemBinding;

        ItemViewHolder(CardShowTakenPictureViewImageRecycleItemBinding serviceInspectionsFormFilledRecycleItemBinding) {
            super(serviceInspectionsFormFilledRecycleItemBinding.getRoot());
            this.mServiceInspectionsFormFilledRecycleItemBinding = serviceInspectionsFormFilledRecycleItemBinding;
        }

        void updateView(CardShowTakenImage cardShowTakenImage) {
            this.mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

            setImageBitmapToImageView(this.mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView, cardShowTakenImage, 8);

            this.mServiceInspectionsFormFilledRecycleItemBinding.setCardShowTakenImage(cardShowTakenImage);
            this.mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView
                    .setOnClickListener(
                            v -> mView.showPreviewPicDialog(cardShowTakenImage)
            );
            this.mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
        }
    }


}
