package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.activities.CardImageGalleryView;
import br.com.stant.libraries.cameraimagegalleryview.components.ItemImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class CardImageGalleryViewAdapter extends RecyclerView.Adapter<CardImageGalleryViewAdapter.ViewHolder> {

    private CardImageGalleryView mView;
    private List<CardShowTakenImage> mCurrentCardShowTakenImageList;

    public CardImageGalleryViewAdapter(CardImageGalleryView view, List<CardShowTakenImage> cardShowTakenImageList) {
        mView                               = view;
        mCurrentCardShowTakenImageList      = new ArrayList<>(cardShowTakenImageList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImage itemImage = new ItemImage(parent.getContext(), parent, mView);
        return new ViewHolder(itemImage.getView(), itemImage);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardShowTakenImage cardShowTakenImage = mCurrentCardShowTakenImageList.get(position);
        ItemImage itemImage = holder.itemImage;
        itemImage.setImage(cardShowTakenImage);
    }

    @Override
    public int getItemCount() {
        return mCurrentCardShowTakenImageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemImage itemImage;

        public ViewHolder(@NonNull View itemView, ItemImage itemImage) {
            super(itemView);
            this.itemImage = itemImage;
        }
    }
}
