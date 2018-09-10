package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil;
import io.reactivex.Observable;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getFile;

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder> {

    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalTempCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mCardShowTakenImageListAsRemoved;
    private Context mContext;
    private CardShowTakenPictureView mView;


    public CardShowTakenPictureViewImagesAdapter(Context context, List<CardShowTakenImage> imageUrlsList, CardShowTakenPictureView view){
        this.mCurrentCardShowTakenImageList      = imageUrlsList;
        this.mContext                            = context;
        this.mView                               = view;
        this.mOriginalTempCardShowTakenImageList = new ArrayList<>();
        this.mCardShowTakenImageListAsAdded      = new ArrayList<>();
        this.mCardShowTakenImageListAsRemoved    = new ArrayList<>();
    }


    public void replaceData(List<CardShowTakenImage> imageUrlsList) {
        mCurrentCardShowTakenImageList = imageUrlsList;
        notifyDataSetChanged();
    }

    public List<CardShowTakenImage> getData(){
        return mCurrentCardShowTakenImageList;
    }

    public void saveEditData(){
        notifyDataSetChanged();
    }

    public void cancelEditData(){
        replaceData(mOriginalTempCardShowTakenImageList);
    }

    public List<CardShowTakenImage> getImagesAsAdded(){
        return mCardShowTakenImageListAsAdded;
    }

    public List<CardShowTakenImage> getImagesAsRemoved() {
        return mCardShowTakenImageListAsRemoved;
    }

    void saveOriginalList(){
        mOriginalTempCardShowTakenImageList = ((List) ((ArrayList) mCurrentCardShowTakenImageList).clone());
        mCardShowTakenImageListAsAdded = new ArrayList<>();
        mCardShowTakenImageListAsRemoved = new ArrayList<>();
    }

    public void removeImage(View view, CardShowTakenImage cardShowTakenImage){
        int position = mCurrentCardShowTakenImageList.indexOf(cardShowTakenImage);

        mCurrentCardShowTakenImageList.remove(cardShowTakenImage);
        mCardShowTakenImageListAsRemoved.add(cardShowTakenImage);

        if(hasCardShowTakenImageAsAdded(cardShowTakenImage))
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

    public void addPicture(CardShowTakenImage cardShowTakenImage){
        mCurrentCardShowTakenImageList.add(cardShowTakenImage);
        mCardShowTakenImageListAsAdded.add(cardShowTakenImage);
        notifyItemInserted(mCurrentCardShowTakenImageList.size());
        mView.updateCurrentAndLimitPhotosQuantityText(getItemCount());
    }

    public void addPictures(List<CardShowTakenImage> cardShowTakenImages){
        for (CardShowTakenImage cardShowTakenImage:
                cardShowTakenImages) {
            addPicture(cardShowTakenImage);
        }

        mCardShowTakenImageListAsAdded.addAll(cardShowTakenImages);
    }

    private String getCorrectImageUrlToShow(CardShowTakenImage cardShowTakenImage, ItemViewHolder itemViewHolder) {

        if (hasTempImagePathToShow(cardShowTakenImage)) {
            if (cardShowTakenImage.getLocalImageFilename() != null) {
                itemViewHolder
                        .mServiceInspectionsFormFilledRecycleItemBinding
                        .cardShowTakenPictureViewGeneralCircularImageView
                        .setImageBitmap(getImage(cardShowTakenImage));
            } else {
                return cardShowTakenImage.getTempImagePathToShow();
            }
        } else if (hasRemoteImageUrl(cardShowTakenImage)) {
            return cardShowTakenImage.getRemoteImageUrl();
        } else if (hasLocalImage(cardShowTakenImage)) {
            return getTempImageFileToShowFromLocalImageFilename(cardShowTakenImage.getLocalImageFilename());
        }

        return null;
    }

    private Bitmap getImage(CardShowTakenImage cardShowTakenImage) {
        if (hasLocalImage(cardShowTakenImage)) {
            return ImageDecoder.getBitmapFromFile(getFile(), cardShowTakenImage.getLocalImageFilename(), 2);
        }

        return null;
    }

    private boolean hasTempImagePathToShow(CardShowTakenImage cardShowTakenImage) {
        return cardShowTakenImage.getTempImagePathToShow() != null;
    }

    private boolean hasRemoteImageUrl(CardShowTakenImage cardShowTakenImage) {
        return cardShowTakenImage.getRemoteImageUrl() != null;
    }

    private boolean hasLocalImage(CardShowTakenImage cardShowTakenImage) {
        return cardShowTakenImage.getLocalImageFilename() != null;
    }

    private String getTempImageFileToShowFromLocalImageFilename(String localImageFilename){
        String tempImageFilePath;
        Bitmap bitmap = ImageDecoder.getBitmapFromFile(getFile(), localImageFilename,2);

        tempImageFilePath = MediaStore.Images.Media.insertImage(mView.getContext().getContentResolver(),
                bitmap, "temp_image_stant", null);

        return tempImageFilePath;
    }

    @Override
    public int getItemCount() {
        return mCurrentCardShowTakenImageList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        private CardShowTakenPictureViewImageRecycleItemBinding mServiceInspectionsFormFilledRecycleItemBinding;

        public ItemViewHolder(CardShowTakenPictureViewImageRecycleItemBinding serviceInspectionsFormFilledRecycleItemBinding) {
            super(serviceInspectionsFormFilledRecycleItemBinding.getRoot());
            this.mServiceInspectionsFormFilledRecycleItemBinding = serviceInspectionsFormFilledRecycleItemBinding;
        }

        public void updateView(CardShowTakenImage cardShowTakenImage) {
            this.mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

            cardShowTakenImage.setTempImagePathToShow(getCorrectImageUrlToShow(cardShowTakenImage, this));
            this.mServiceInspectionsFormFilledRecycleItemBinding.setCardShowTakenImage(cardShowTakenImage);
            this.mServiceInspectionsFormFilledRecycleItemBinding.
                    cardShowTakenPictureViewGeneralCircularImageView.setOnClickListener(
                    v -> mView.showPreviewPicDialog(cardShowTakenImage));

            this.mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
        }
    }
}
