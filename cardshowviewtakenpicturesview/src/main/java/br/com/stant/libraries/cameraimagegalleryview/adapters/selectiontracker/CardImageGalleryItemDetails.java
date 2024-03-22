package br.com.stant.libraries.cameraimagegalleryview.adapters.selectiontracker;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;

import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class CardImageGalleryItemDetails extends ItemDetailsLookup.ItemDetails<Long>{

    private int adapterPosition;
    private CardShowTakenImage image;

    public void setImage(CardShowTakenImage image) {
        this.image = image;
    }

    public void setAdapterPosition(int adapterPosition) {
        this.adapterPosition = adapterPosition;
    }

    @Override
    public int getPosition() {
        return adapterPosition;
    }

    @Nullable
    @Override
    public Long getSelectionKey() {
        return (long) image.hashCode();
    }
}
