package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.FileUtil;

/**
 * Created by denisvieira on 08/06/17.
 */

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder> {

    private static final String TEMP_IMAGE_NAME = "card_show_taken_picture_temp_image";

    private ItemViewHolder mViewHolder;
    private List<String> mCurrentImageUrlsList;
    private List<String> mOriginalTempImageUrlsList;
    private List<String> mImagesAsAdded;
    private List<String> mImagesAsRemoved;
    private Context mContext;
    private CardShowTakenPictureView mView;
    private Bitmap mImageBitmap;


    public CardShowTakenPictureViewImagesAdapter(Context context, List<String> imageUrlsList, CardShowTakenPictureView view){
        this.mCurrentImageUrlsList = imageUrlsList;
        this.mContext       = context;
        this.mView          = view;
        this.mOriginalTempImageUrlsList = new ArrayList<>();
        this.mImagesAsAdded = new ArrayList<>();
        this.mImagesAsRemoved = new ArrayList<>();
    }


    public void replaceData(List<String> imageUrlsList) {
        mCurrentImageUrlsList = imageUrlsList;
        notifyDataSetChanged();
    }

    public List<String> getData(){
        return mCurrentImageUrlsList;
    }

    public void saveEditData(){
        notifyDataSetChanged();
    }

    public void cancelEditData(){
        replaceData(mOriginalTempImageUrlsList);
    }

    public List<String> getImagesAsAdded(){
        return mImagesAsAdded;
    }

    public List<String> getImagesAsRemoved() {
        return mImagesAsRemoved;
    }

    void saveOriginalList(){
        mOriginalTempImageUrlsList = ((List) ((ArrayList) mCurrentImageUrlsList).clone());
        mImagesAsAdded = new ArrayList<>();
        mImagesAsRemoved = new ArrayList<>();
    }

    public void removeImage(View view, String imageUrl){
        mCurrentImageUrlsList.remove(imageUrl);
        mImagesAsRemoved.add(imageUrl);
        replaceData(mCurrentImageUrlsList);
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

        String imageUrl = mCurrentImageUrlsList.get(position);

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setHandler(this);
        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setImageUrl(imageUrl);
        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.
                cardShowTakenPictureViewGeneralCircularImageView.setOnClickListener(
                        v -> mView.showPreviewPicDialog(imageUrl));


        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
    }



    public String generateImageUrlFromBitmapBasedInImageView(String imageUrl, Activity activity){
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

        imageUrl = MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                mImageBitmap, TEMP_IMAGE_NAME, null);

        return imageUrl;

//        mCardShowTakenPictureViewBinding.setImageUrl(imageUrl);


//
//        FileUtil.shouImageFromUrl(mImageUrl, serviceInspectionFormFilledDetailFinishSiffDialogBinding.serviceInspectionFormFilledDetailFinishSiffDialogCardTakePictureTakenPictureInclude.
//                inspectionDataCardShowTakenPictureImageView,getContext());
    }

    public void addPicture(String url){
        mCurrentImageUrlsList.add(url);
        mImagesAsAdded.add(url);
        replaceData(mCurrentImageUrlsList);
    }


    @Override
    public int getItemCount() {
        return mCurrentImageUrlsList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        private CardShowTakenPictureViewImageRecycleItemBinding mServiceInspectionsFormFilledRecycleItemBinding;

        public ItemViewHolder(CardShowTakenPictureViewImageRecycleItemBinding serviceInspectionsFormFilledRecycleItemBinding) {
            super(serviceInspectionsFormFilledRecycleItemBinding.getRoot());
            this.mServiceInspectionsFormFilledRecycleItemBinding = serviceInspectionsFormFilledRecycleItemBinding;
        }
    }
}
