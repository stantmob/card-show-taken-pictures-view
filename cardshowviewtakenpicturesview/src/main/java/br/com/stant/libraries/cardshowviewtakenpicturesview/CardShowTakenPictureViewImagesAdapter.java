package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewImageRecycleItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

/**
 * Created by denisvieira on 08/06/17.
 */

public class CardShowTakenPictureViewImagesAdapter extends RecyclerView.Adapter<CardShowTakenPictureViewImagesAdapter.ItemViewHolder> {


    private ItemViewHolder mViewHolder;
    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalTempCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mImagesAsRemoved;
    private Context mContext;
    private CardShowTakenPictureView mView;


    public CardShowTakenPictureViewImagesAdapter(Context context, List<CardShowTakenImage> imageUrlsList, CardShowTakenPictureView view){
        this.mCurrentCardShowTakenImageList = imageUrlsList;
        this.mContext       = context;
        this.mView          = view;
        this.mOriginalTempCardShowTakenImageList = new ArrayList<>();
        this.mCardShowTakenImageListAsAdded = new ArrayList<>();
        this.mImagesAsRemoved = new ArrayList<>();
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
        return mImagesAsRemoved;
    }

    void saveOriginalList(){
        mOriginalTempCardShowTakenImageList = ((List) ((ArrayList) mCurrentCardShowTakenImageList).clone());
        mCardShowTakenImageListAsAdded = new ArrayList<>();
        mImagesAsRemoved = new ArrayList<>();
    }

    public void removeImage(View view, CardShowTakenImage cardShowTakenImage){
        mCurrentCardShowTakenImageList.remove(cardShowTakenImage);
        mImagesAsRemoved.add(cardShowTakenImage);
        replaceData(mCurrentCardShowTakenImageList);
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

        CardShowTakenImage cardShowTakenImage = mCurrentCardShowTakenImageList.get(position);

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setHandler(this);
        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setCardStateEnum(mView.getActualCardState());

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.setCardShowTakenImage(cardShowTakenImage);
        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.
                cardShowTakenPictureViewGeneralCircularImageView.setOnClickListener(
                        v -> mView.showPreviewPicDialog(cardShowTakenImage));

        mViewHolder.mServiceInspectionsFormFilledRecycleItemBinding.executePendingBindings();
    }

    public void addPicture(CardShowTakenImage cardShowTakenImage){
        mCurrentCardShowTakenImageList.add(cardShowTakenImage);
        mCardShowTakenImageListAsAdded.add(cardShowTakenImage);
        replaceData(mCurrentCardShowTakenImageList);
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
    }
}
