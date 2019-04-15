package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VibratorUtils;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropHandler;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.ItemTouchHelperViewHolder;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder>
        implements DragAndDropHandler {

    private CardShowTakenPictureView mView;
    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalTempCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mCardShowTakenImageListAsRemoved;
    private ItemTouchHelper mItemTouchHelper;

    private static final int vibrationDuration = 400;

    public CardShowTakenPictureViewImagesAdapter(CardShowTakenPictureView view) {
        mView                               = view;
        mCurrentCardShowTakenImageList      = new ArrayList<>(0);
        mOriginalTempCardShowTakenImageList = new ArrayList<>(0);
        mCardShowTakenImageListAsAdded      = new ArrayList<>(0);
        mCardShowTakenImageListAsRemoved    = new ArrayList<>(0);
    }

    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        CardShowTakenPictureViewImageRecycleItemBinding mCardShowTakenPictureViewImageRecycleItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.card_show_taken_picture_view_image_recycle_item,
                parent,
                false);

        return new ItemViewHolder(mCardShowTakenPictureViewImageRecycleItemBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull ItemViewHolder itemViewHolder, int position) {
        CardShowTakenImage cardShowTakenImage = mCurrentCardShowTakenImageList.get(position);

        itemViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setHandler(this);

        configureDefaultConstraintLayoutTouchListener(itemViewHolder);

        itemViewHolder.updateView(cardShowTakenImage);
    }

    private void configureDefaultConstraintLayoutTouchListener(ItemViewHolder itemViewHolder) {
        itemViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView.setOnLongClickListener(
                view -> {
                    if (mView.isNotCanEditState() && mView.dragAndDropModeIsEnabled()) {
                        mItemTouchHelper.startDrag(itemViewHolder);
                    }
                    return true;
                }
        );
    }

    public void replaceData(List<CardShowTakenImage> imageUrlsList) {
        mCurrentCardShowTakenImageList = imageUrlsList;
        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
        notifyDataSetChanged();
    }

    public List<CardShowTakenImage> getCurrentImages() {
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
        mOriginalTempCardShowTakenImageList = (List) ((ArrayList) mCurrentCardShowTakenImageList).clone();
        mCardShowTakenImageListAsAdded      = new ArrayList<>();
        mCardShowTakenImageListAsRemoved    = new ArrayList<>();
    }

    public void removeImage(View view, CardShowTakenImage cardShowTakenImage) {
        int position = mCurrentCardShowTakenImageList.indexOf(cardShowTakenImage);

        mCurrentCardShowTakenImageList.remove(cardShowTakenImage);
        mCardShowTakenImageListAsRemoved.add(cardShowTakenImage);

        if (cardShowTakenImage != null) {
            mCardShowTakenImageListAsAdded.remove(cardShowTakenImage);
        }

        notifyItemRemoved(position);

        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
    }

    public void addPicture(CardShowTakenImage cardShowTakenImage) {
        mCurrentCardShowTakenImageList.add(cardShowTakenImage);
        mCardShowTakenImageListAsAdded.add(cardShowTakenImage);

        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
        notifyItemInserted(mCurrentCardShowTakenImageList.size());
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

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        mItemTouchHelper = itemTouchHelper;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        CardShowTakenImage targetCardShowTakenImage = mCurrentCardShowTakenImageList.get(oldPosition);

        mCurrentCardShowTakenImageList.remove(oldPosition);
        mCurrentCardShowTakenImageList.add(newPosition, targetCardShowTakenImage);

        notifyItemMoved(oldPosition, newPosition);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private CardShowTakenPictureViewImageRecycleItemBinding mServiceInspectionsFormFilledRecycleItemBinding;

        ItemViewHolder(CardShowTakenPictureViewImageRecycleItemBinding serviceInspectionsFormFilledRecycleItemBinding) {
            super(serviceInspectionsFormFilledRecycleItemBinding.getRoot());
            mServiceInspectionsFormFilledRecycleItemBinding = serviceInspectionsFormFilledRecycleItemBinding;
        }

        void updateView(CardShowTakenImage cardShowTakenImage) {
            mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

            setImageBitmapToImageView(mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView,
                    cardShowTakenImage, 8);

            mServiceInspectionsFormFilledRecycleItemBinding.setCardShowTakenImage(cardShowTakenImage);
            mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView
                    .setOnClickListener(
                            v -> mView.showPreviewPicDialog(cardShowTakenImage)
                    );
            mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
        }

        @Override
        public void onItemSelected() {
            VibratorUtils.vibrate(mView.getContext(), vibrationDuration);
            mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureContainerConstraintLayout.setAlpha(0.75f);
            mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPicturesItemDeleteIconContainerCardView.setVisibility(View.GONE);
        }

        @Override
        public void onItemClear() {
            mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureContainerConstraintLayout.setAlpha(1);
            mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPicturesItemDeleteIconContainerCardView
                    .setVisibility(getItemDeleteIconVisibility());
        }

        private int getItemDeleteIconVisibility() {
            return mView.getActualCardState() == CardShowTakenPictureStateEnum.EDIT ? View.VISIBLE : View.GONE;
        }


    }


}
