package br.com.stant.libraries.cameraimagegalleryview.activities;


import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_LIST_GALLERY;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.adapters.CardImageGalleryViewAdapter;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.ActivityCardImageGalleryViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;


public class CardImageGalleryView extends AppCompatActivity{

    private ActivityCardImageGalleryViewBinding mBinding;
    private RecyclerView recyclerView;
    private CardImageGalleryViewAdapter cardImageGalleryViewAdapter;
    private GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_card_image_gallery_view);

        setSupportActionBar(mBinding.topAppBar);
        mBinding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());

        recyclerView = findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        List<CardShowTakenImage> cardShowTakenImageList = new ArrayList<>();

        List<CardShowTakenImage> receivedImageList = (List<CardShowTakenImage>) getIntent().getSerializableExtra(KEY_IMAGE_LIST_GALLERY);

        if (receivedImageList != null && !receivedImageList.isEmpty()) {
            cardShowTakenImageList.addAll(receivedImageList);
        }
        cardImageGalleryViewAdapter = new CardImageGalleryViewAdapter(this, cardShowTakenImageList);
        recyclerView.setAdapter(cardImageGalleryViewAdapter);

    }
}
