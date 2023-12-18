package br.com.stant.libraries.cameraimagegalleryview.adapters.selectiontracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;

import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class CardImageGalleryItemKeyProvider  extends ItemKeyProvider<Long> {
    private List<CardShowTakenImage> cardShowTakenImagesList;
    public CardImageGalleryItemKeyProvider() {
        super(ItemKeyProvider.SCOPE_MAPPED);
        cardShowTakenImagesList = CardShowTakenImageInjection.getCardShowTakenPictureInjection().getAll();
    }

    @Nullable
    @Override
    public Long getKey(int position) {
        return (long) cardShowTakenImagesList.get(position).hashCode();
    }

    @Override
    public int getPosition(@NonNull Long key) {
        for(int i = 0; i < cardShowTakenImagesList.size(); i++){
            if(cardShowTakenImagesList.get(i).hashCode() == key){
                return i;
            }
        }
        return 0;
    }
}
