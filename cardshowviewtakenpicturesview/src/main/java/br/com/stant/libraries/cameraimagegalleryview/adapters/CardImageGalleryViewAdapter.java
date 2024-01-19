package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.activities.CardImageGalleryView;
import br.com.stant.libraries.cameraimagegalleryview.adapters.selectiontracker.CardImageGalleryItemDetails;
import br.com.stant.libraries.cameraimagegalleryview.components.ItemImage;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class CardImageGalleryViewAdapter extends RecyclerView.Adapter<CardImageGalleryViewAdapter.ItemImageViewHolder> {

    private CardImageGalleryView mView;

    private List<CardShowTakenImage> mCardShowTakenImages;

    private SelectionTracker<Long> selectionTracker;

    public CardImageGalleryViewAdapter(CardImageGalleryView view) {
        mView = view;
        mCardShowTakenImages = CardShowTakenImageInjection.getCardShowTakenPictureInjection().getAll();
    }

    @NonNull
    @Override
    public ItemImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemImage itemImage = new ItemImage(parent.getContext(), parent, mView);
        return new ItemImageViewHolder(itemImage.getView(), itemImage);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemImageViewHolder holder, int position) {
        CardShowTakenImage cardShowTakenImage = mCardShowTakenImages.get(position);
        if(holder.isLoaded){
            holder.selectImage(cardShowTakenImage);
        } else {
            holder.setItemImage(cardShowTakenImage, position);
        }
    }

    @Override
    public int getItemCount() {
        return mCardShowTakenImages.size();
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    public List<CardShowTakenImage> getSelectedItems(){
        List<CardShowTakenImage> itemsSelected = new ArrayList<>();

        for(CardShowTakenImage image : mCardShowTakenImages) {
            if(selectionTracker.getSelection().contains((long)image.hashCode())){
                itemsSelected.add(image);
            }
        }
        return itemsSelected;
    }

    public int getSelectedCount(){
        return selectionTracker.getSelection().size();
    }

    public void clearSelections(){
        selectionTracker.clearSelection();
    }


    public class ItemImageViewHolder extends RecyclerView.ViewHolder {
        ItemImage itemImage;
        boolean isLoaded;
        CardImageGalleryItemDetails details;

        public ItemImageViewHolder(@NonNull View itemView, ItemImage itemImage) {
            super(itemView);
            this.itemImage = itemImage;
            isLoaded = false;
            details = new CardImageGalleryItemDetails();
        }

        public CardImageGalleryItemDetails getDetails() {
            return details;
        }

        public void setItemImage(CardShowTakenImage cardShowTakenImage, int position){
            isLoaded = true;
            details.setImage(cardShowTakenImage);
            details.setAdapterPosition(position);
            itemImage.setImage(cardShowTakenImage);
        }

        public void selectImage(CardShowTakenImage cardShowTakenImage){
            if(selectionTracker.isSelected((long)cardShowTakenImage.hashCode())){
                itemImage.changeImageToSelectedMode();
            } else {
                itemImage.removeFromSelectedMode();
            }
            mView.changeSelectionMode();
        }
    }
}
