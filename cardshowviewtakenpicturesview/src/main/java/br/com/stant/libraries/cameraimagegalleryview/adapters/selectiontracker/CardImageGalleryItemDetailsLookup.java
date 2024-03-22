package br.com.stant.libraries.cameraimagegalleryview.adapters.selectiontracker;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import br.com.stant.libraries.cameraimagegalleryview.adapters.CardImageGalleryViewAdapter;

public class CardImageGalleryItemDetailsLookup extends ItemDetailsLookup<Long> {

    private RecyclerView recyclerView;

    public CardImageGalleryItemDetailsLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent event) {
        View view = recyclerView.findChildViewUnder( event.getX(), event.getY() );

        if( view != null ){
            CardImageGalleryViewAdapter.ItemImageViewHolder holder = (CardImageGalleryViewAdapter.ItemImageViewHolder) recyclerView.getChildViewHolder(view);

            return holder.getDetails();
        }
        return null;
    }
}
