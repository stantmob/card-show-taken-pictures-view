package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraPhotosAdapter;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VibratorUtils;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropHandler;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.ItemTouchHelperViewHolder;
import io.reactivex.Observable;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder>
        implements DragAndDropHandler {

    private CardShowTakenPictureView mView;
    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalTempCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mCardShowTakenImageListAsRemoved;
    private ItemTouchHelper mItemTouchHelper;

    public CardShowTakenPictureViewImagesAdapter(CardShowTakenPictureView view) {
        this.mView                               = view;
        this.mCurrentCardShowTakenImageList      = new ArrayList<>(0);
        this.mOriginalTempCardShowTakenImageList = new ArrayList<>(0);
        this.mCardShowTakenImageListAsAdded      = new ArrayList<>(0);
        this.mCardShowTakenImageListAsRemoved    = new ArrayList<>(0);
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
        itemViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureContainerConstraintLayout.setOnTouchListener(
                (view, motionEvent) -> {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                                mItemTouchHelper.startDrag(itemViewHolder);
                            }

                            break;
                        case MotionEvent.ACTION_UP:
                            view.performClick();
                            break;
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
            this.mServiceInspectionsFormFilledRecycleItemBinding = serviceInspectionsFormFilledRecycleItemBinding;
        }

        void updateView(CardShowTakenImage cardShowTakenImage) {
            this.mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

            setImageBitmapToImageView(this.mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView,
                    cardShowTakenImage, 8);

            this.mServiceInspectionsFormFilledRecycleItemBinding.setCardShowTakenImage(cardShowTakenImage);
            this.mServiceInspectionsFormFilledRecycleItemBinding.cardShowTakenPictureViewGeneralCircularImageView
                    .setOnClickListener(
                            v -> mView.showPreviewPicDialog(cardShowTakenImage)
                    );
            this.mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
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
                    .setVisibility(mView.getActualCardState() == CardShowTakenPictureStateEnum.EDIT ? View.VISIBLE: View.GONE);
        }


    }


}
