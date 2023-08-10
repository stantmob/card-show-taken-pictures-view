package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryView;
import br.com.stant.libraries.cameraimagegalleryview.FullScreenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class CardImageGalleryViewAdapter extends RecyclerView.Adapter<CardImageGalleryViewAdapter.ViewHolder> {

    private CardImageGalleryView mView;
    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;
    private List<CardShowTakenImage> mOriginalTempCardShowTakenImageList;
    private List<CardShowTakenImage> mCardShowTakenImageListAsAdded;
    private List<CardShowTakenImage> mCardShowTakenImageListAsRemoved;

    public CardImageGalleryViewAdapter(CardImageGalleryView view, List<CardShowTakenImage> cardShowTakenImageList) {
        mView                               = view;
        mCurrentCardShowTakenImageList      = new ArrayList<>(cardShowTakenImageList);
        mOriginalTempCardShowTakenImageList = new ArrayList<>(0);
        mCardShowTakenImageListAsAdded      = new ArrayList<>(0);
        mCardShowTakenImageListAsRemoved    = new ArrayList<>(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardShowTakenImage cardShowTakenImage = mCurrentCardShowTakenImageList.get(position);
        ImageView imageView = holder.imageView;
        Glide.with(mView)
                .load(cardShowTakenImage.getRemoteImageUrl())
                .fitCenter()
                .into(imageView);

        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(mView, FullScreenImage.class);
            intent.putExtra("fullImageUrl", cardShowTakenImage.getRemoteImageUrl());
            mView.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mCurrentCardShowTakenImageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }

//    public void addPicture(CardShowTakenImage cardShowTakenImage) {
//        mCurrentCardShowTakenImageList.add(cardShowTakenImage);
//        mCardShowTakenImageListAsAdded.add(cardShowTakenImage);
//
//        mView.updateCurrentAndLimitImagesQuantityText(getItemCount());
//        notifyItemInserted(mCurrentCardShowTakenImageList.size());
//    }
//
//    public void setCardShowTakenImageList(List<CardShowTakenImage> cardShowTakenImageList) {
//        this.cardShowTakenImageList = cardShowTakenImageList;
//        notifyDataSetChanged();
//    }
}
