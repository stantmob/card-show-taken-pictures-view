package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.FileUtil;

/**
 * Created by denisvieira on 08/06/17.
 */

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder> {

    private static final String TEMP_IMAGE_NAME = "card_show_taken_picture_temp_image";

    private ItemViewHolder mViewHolder;
    private List<CardImage> mCurrentCardImageList;
    private List<CardImage> mOriginalTempCardImageList;
    private List<CardImage> mCardImageListAsAdded;
    private List<CardImage> mImagesAsRemoved;
    private Context mContext;
    private CardShowTakenPictureView mView;
    private Bitmap mImageBitmap;


    public CardShowTakenPictureViewImagesAdapter(Context context, List<CardImage> imageUrlsList, CardShowTakenPictureView view){
        this.mCurrentCardImageList = imageUrlsList;
        this.mContext       = context;
        this.mView          = view;
        this.mOriginalTempCardImageList = new ArrayList<>();
        this.mCardImageListAsAdded = new ArrayList<>();
        this.mImagesAsRemoved = new ArrayList<>();
    }


    public void replaceData(List<CardImage> imageUrlsList) {
        mCurrentCardImageList = imageUrlsList;
        notifyDataSetChanged();
    }

    public List<CardImage> getData(){
        return mCurrentCardImageList;
    }

    public void saveEditData(){
        notifyDataSetChanged();
    }

    public void cancelEditData(){
        replaceData(mOriginalTempCardImageList);
    }

    public List<CardImage> getImagesAsAdded(){
        return mCardImageListAsAdded;
    }

    public List<CardImage> getImagesAsRemoved() {
        return mImagesAsRemoved;
    }

    void saveOriginalList(){
        mOriginalTempCardImageList = ((List) ((ArrayList) mCurrentCardImageList).clone());
        mCardImageListAsAdded = new ArrayList<>();
        mImagesAsRemoved = new ArrayList<>();
    }

    public void removeImage(View view, CardImage cardImage){
        mCurrentCardImageList.remove(cardImage);
        mImagesAsRemoved.add(cardImage);
        replaceData(mCurrentCardImageList);
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
        mViewHolder = holder;

        CardImage cardImage = mCurrentCardImageList.get(position);

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setHandler(this);
        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setCardImage(cardImage);
        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.
                cardShowTakenPictureViewGeneralCircularImageView.setOnClickListener(
                        v -> mView.showPreviewPicDialog(cardImage));


        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
    }



    public CardImage generateCardImageFromBitmapBasedInImageView(String imageUrl, String imageFilename){
        mImageBitmap = FileUtil.createBitFromPath(imageUrl);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
            mImageBitmap = FileUtil.rotateBitmap(mImageBitmap, 90);
        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
            mImageBitmap = FileUtil.rotateBitmap(mImageBitmap, 270);
        } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
            mImageBitmap = FileUtil.rotateBitmap(mImageBitmap, 180);
        }

        CardImage cardImage = new CardImage(mImageBitmap, imageUrl, imageFilename);

        return cardImage;

//        mCardShowTakenPictureViewBinding.setImageUrl(imageUrl);


//
//        FileUtil.shouImageFromUrl(mImageUrl, serviceInspectionFormFilledDetailFinishSiffDialogBinding.serviceInspectionFormFilledDetailFinishSiffDialogCardTakePictureTakenPictureInclude.
//                inspectionDataCardShowTakenPictureImageView,getContext());
    }

    public void addPicture(CardImage cardImage){
        mCurrentCardImageList.add(cardImage);
        mCardImageListAsAdded.add(cardImage);
        replaceData(mCurrentCardImageList);
    }


    @Override
    public int getItemCount() {
        return mCurrentCardImageList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        private CardShowTakenPictureViewImageRecycleItemBinding mServiceInspectionsFormFilledRecycleItemBinding;

        public ItemViewHolder(CardShowTakenPictureViewImageRecycleItemBinding serviceInspectionsFormFilledRecycleItemBinding) {
            super(serviceInspectionsFormFilledRecycleItemBinding.getRoot());
            this.mServiceInspectionsFormFilledRecycleItemBinding = serviceInspectionsFormFilledRecycleItemBinding;
        }
    }
}
