package br.com.stant.libraries.card_show_taken_pictures_view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.stant.libraries.card_show_taken_pictures_view.databinding.ActivityMainBinding;
import br.com.stant.libraries.card_show_taken_pictures_view.models.Gallery;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class MainActivity extends AppCompatActivity {


    public static final String KEY_GALLERY = "KEY_CHOSE_CONSTRUCTION_SITE_GUID";

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_main);

        Gallery gallery = (Gallery) getIntent().getSerializableExtra(KEY_GALLERY);


        mBinding.cardShowViewTakenPicturesView.setActivity(this);
        mBinding.cardShowViewTakenPicturesView.setExampleImages();
        mBinding.cardShowViewTakenPicturesView.setOnSavedCardListener(new CardShowTakenPictureViewContract.OnSavedCardListener() {
            @Override
            public void onSaved(List<CardShowTakenImage> imagesAsAdded, List<CardShowTakenImage> imagesAsRemoved) {

            }

            @Override
            public void onCancel() {

            }
        });


        if(gallery != null)
            mBinding.cardShowViewTakenPicturesView.setCardImages(gallery.getImages());

        mBinding.cardShowViewTakenPicturesView.setBinding(mBinding.cardShowViewTakenPicturesView,"Denis Vieira", new Date());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mBinding.cardShowViewTakenPicturesView.addImageOnActivityResult(requestCode, resultCode, data);
    }



}
