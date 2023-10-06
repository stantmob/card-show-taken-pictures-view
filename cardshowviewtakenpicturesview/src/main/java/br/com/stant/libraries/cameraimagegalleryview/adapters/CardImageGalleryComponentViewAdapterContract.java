package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryComponentView;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryComponentRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VibratorUtils;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropHandler;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.ItemTouchHelperViewHolder;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

public class CardImageGalleryComponentViewAdapterContract
        extends RecyclerView.Adapter<CardImageGalleryComponentViewAdapterContract.ItemViewHolder> implements DragAndDropHandler {

    private CardImageGalleryComponentView mView;
    private List<CardShowTakenImage> mCurrentCardImageGalleryList;
    private List<CardShowTakenImage> mOriginalTempCardImageGalleryComponentList;
    private List<CardShowTakenImage> mCardImageGalleryComponentListAsAdded;
    private List<CardShowTakenImage> mCardImageGalleryComponentListAsRemoved;
    private ItemTouchHelper mItemTouchHelper;


    public CardImageGalleryComponentViewAdapterContract(CardImageGalleryComponentView view) {
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
        configureDefaultConstraintLayoutTouchListener(itemViewHolder);

        itemViewHolder.updateView(cardShowTakenImage);
    }

    @Override
    public int getItemCount() {
        return mCurrentCardImageGalleryList.size();
    }

    public void replaceData(List<CardShowTakenImage> imageUrlsList) {
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

    public List<CardShowTakenImage> getImagesAsAdded() {
        return mCardImageGalleryComponentListAsAdded;
    }

    public List<CardShowTakenImage> getImagesAsRemoved() {
        return mCardImageGalleryComponentListAsRemoved;
    }

    public void saveOriginalList() {
        mOriginalTempCardImageGalleryComponentList = (List) ((ArrayList) mCurrentCardImageGalleryList).clone();
        mCardImageGalleryComponentListAsAdded = new ArrayList<>();
        mCardImageGalleryComponentListAsRemoved = new ArrayList<>();
    }

    private void configureDefaultConstraintLayoutTouchListener(ItemViewHolder itemViewHolder) {
        itemViewHolder.mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryAvatar.setOnLongClickListener(
                view -> {
                    if (mView.isNotCanEditState() && mView.dragAndDropModeIsEnabled()) {
                        mItemTouchHelper.startDrag(itemViewHolder);
                    }
                    return true;
                }
        );
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
        mCurrentCardImageGalleryList.addAll(cardShowTakenImages);
        mCardImageGalleryComponentListAsAdded.addAll(cardShowTakenImages);

        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
        notifyItemInserted(mCurrentCardImageGalleryList.size());
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        mItemTouchHelper = itemTouchHelper;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        CardShowTakenImage targetCardShowTakenImage = mCurrentCardImageGalleryList.get(oldPosition);

        mCurrentCardImageGalleryList.remove(oldPosition);
        mCurrentCardImageGalleryList.add(newPosition, targetCardShowTakenImage);

        notifyItemMoved(oldPosition, newPosition);
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
            mServiceInspectionsFormFilledRecyclerItemBinding.setCardShowTakenImage(cardShowTakenImage);
            mServiceInspectionsFormFilledRecyclerItemBinding.setCardStateEnum(mView.getActualCardState());
            mServiceInspectionsFormFilledRecyclerItemBinding.setHasError(cardShowTakenImage.hasError());

            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryAvatar
                    .setOnClickListener(
                            v -> mView.showPreviewPicDialog(cardShowTakenImage, getBindingAdapterPosition(),
                                    (@NonNull String caption, int photoPosition) -> {
                                        mCurrentCardImageGalleryList.get(photoPosition).setCaption(caption);
                                    }
                            )
                    );
            mServiceInspectionsFormFilledRecyclerItemBinding.executePendingBindings();
        }

        @Override
        public void onItemSelected() {
            VibratorUtils.vibrate(mView.getContext(), 400);
            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryComponentConstraintLayout.setAlpha(0.75f);
            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryViewItemCloseIconContainer.setVisibility(View.GONE);
        }

        @Override
        public void onItemClear() {
            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryComponentConstraintLayout.setAlpha(1);
            mServiceInspectionsFormFilledRecyclerItemBinding.cardImageGalleryViewItemCloseIconContainer
                    .setVisibility(getItemDeleteIconVisibility());
        }

        private int getItemDeleteIconVisibility() {
            return mView.getActualCardState() == CardShowTakenPictureStateEnum.EDIT ? View.VISIBLE : View.GONE;
        }
    }
}
