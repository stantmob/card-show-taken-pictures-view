package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
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
    private List<CardShowTakenImage> mAllCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mCardShowTakenImageListAsRemoved;
    private List<CardShowTakenImage> mCardShowTakenImageListAsUpdated;
    private ItemTouchHelper mItemTouchHelper;

    public CardShowTakenPictureViewImagesAdapter(CardShowTakenPictureView view) {
        mView                               = view;
        mAllCardShowTakenImageList = new ArrayList<>(0);
        mOriginalCardShowTakenImageList = new ArrayList<>(0);
        mCardShowTakenImageListAsAdded      = new ArrayList<>(0);
        mCardShowTakenImageListAsRemoved    = new ArrayList<>(0);
        mCardShowTakenImageListAsUpdated    = new ArrayList<>(0);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardShowTakenPictureViewImageRecycleItemBinding mCardShowTakenPictureViewImageRecycleItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.card_show_taken_picture_view_image_recycle_item,
                parent,
                false);

        return new ItemViewHolder(mCardShowTakenPictureViewImageRecycleItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int position) {
        CardShowTakenImage cardShowTakenImage = mAllCardShowTakenImageList.get(position);

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
        mAllCardShowTakenImageList = imageUrlsList;
        mOriginalCardShowTakenImageList = (List) ((ArrayList) mAllCardShowTakenImageList).clone();
        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
        notifyDataSetChanged();
    }

    public List<CardShowTakenImage> getAllImages() {
        return mAllCardShowTakenImageList;
    }

    public void saveEditData() {
        notifyDataSetChanged();
    }

    public void cancelEditData() {
        replaceData(mOriginalCardShowTakenImageList);
    }

    public List<CardShowTakenImage> getImagesAsAdded() {
        return mCardShowTakenImageListAsAdded;
    }
    public List<CardShowTakenImage> getImagesAsUpdated() {
        return mCardShowTakenImageListAsUpdated;
    }

    public List<CardShowTakenImage> getImagesAsRemoved() {
        return mCardShowTakenImageListAsRemoved;
    }

    void saveOriginalList() {
        mCardShowTakenImageListAsAdded      = new ArrayList<>();
        mCardShowTakenImageListAsUpdated    = new ArrayList<>();
        mCardShowTakenImageListAsRemoved    = new ArrayList<>();
    }

    public void removeImage(View view, CardShowTakenImage cardShowTakenImage) {
        int position = mAllCardShowTakenImageList.indexOf(cardShowTakenImage);

        if(mOriginalCardShowTakenImageList.contains(cardShowTakenImage)){
            mCardShowTakenImageListAsRemoved.add(cardShowTakenImage);
        }
        mAllCardShowTakenImageList.remove(cardShowTakenImage);
        mCardShowTakenImageListAsAdded.remove(cardShowTakenImage);
        mCardShowTakenImageListAsUpdated.remove(cardShowTakenImage);


        notifyItemRemoved(position);

        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
    }

    public void addPicture(CardShowTakenImage cardShowTakenImage) {
        mAllCardShowTakenImageList.add(cardShowTakenImage);
        mCardShowTakenImageListAsAdded.add(cardShowTakenImage);

        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
        notifyItemInserted(mAllCardShowTakenImageList.size());
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
        return mAllCardShowTakenImageList.size();
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        mItemTouchHelper = itemTouchHelper;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        CardShowTakenImage targetCardShowTakenImage = mAllCardShowTakenImageList.get(oldPosition);

        mAllCardShowTakenImageList.remove(oldPosition);
        mAllCardShowTakenImageList.add(newPosition, targetCardShowTakenImage);

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
                            v -> mView.showPreviewPicDialog(cardShowTakenImage, getAdapterPosition(), new OnCaptionSavedCallback() {
                                @Override
                                public void onCaptionSaved(@NonNull String caption, int photoPosition) {
                                    mAllCardShowTakenImageList.get(photoPosition).setCaption(caption);
                                    if(mCardShowTakenImageListAsAdded.contains(mAllCardShowTakenImageList.get(photoPosition))){
                                        int index = mCardShowTakenImageListAsAdded.indexOf(mAllCardShowTakenImageList.get(photoPosition));
                                        mCardShowTakenImageListAsAdded.get(index).setCaption(caption);
                                    } else {
                                        mCardShowTakenImageListAsUpdated.add(mAllCardShowTakenImageList.get(photoPosition));
                                    }
                                }
                            })
                    );
            mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
        }

        @Override
        public void onItemSelected() {
            VibratorUtils.vibrate(mView.getContext(), 400);
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
