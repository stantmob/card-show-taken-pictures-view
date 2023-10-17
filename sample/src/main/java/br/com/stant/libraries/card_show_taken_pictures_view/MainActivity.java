package br.com.stant.libraries.card_show_taken_pictures_view;

import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract;
import br.com.stant.libraries.card_show_taken_pictures_view.databinding.ActivityMainBinding;
import br.com.stant.libraries.card_show_taken_pictures_view.models.Gallery;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class MainActivity extends AppCompatActivity {


    public static final String KEY_GALLERY = "KEY_CHOSE_CONSTRUCTION_SITE_GUID";

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Gallery gallery = (Gallery) getIntent().getSerializableExtra(KEY_GALLERY);
        mBinding.cardShowViewTakenPicturesView.setActivity(this);
        mBinding.cardShowViewTakenPicturesView.setExampleImages();
        mBinding.cardShowViewTakenPicturesView.setOnSavedCardListener(
                new CardShowTakenPictureViewContract.OnSavedCardListener() {
                    @Override
                    public void onSaved(List<CardShowTakenImage> currentImages,
                                        List<CardShowTakenImage> imagesAsAdded,
                                        List<CardShowTakenImage> imagesAsRemoved) {

                    }

                    @Override
                    public void onCancel() {

                    }
                }
        );

        if (gallery != null)
            mBinding.cardShowViewTakenPicturesView.setCardImages(gallery.getImages());

        mBinding.cardShowViewTakenPicturesView.setBinding(mBinding.cardShowViewTakenPicturesView,
                "Denis Vieira", new Date());
        mBinding.cardShowViewTakenPicturesView.setImagesQuantityLimit(15, null);
        mBinding.cardShowViewTakenPicturesView.setIsMultipleGallerySelection(true);
        mBinding.cardShowViewTakenPicturesView.enableDragAndDrop();
        mBinding.cardShowViewTakenPicturesView.setStrokeColor(ContextCompat.getColor(getApplicationContext(),
                R.color.colorPrimary));
        mBinding.cardShowViewTakenPicturesView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.colorPrimary));
        mBinding.cardShowViewTakenPicturesView.enableSaveOnlyMode(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_cam),
                "oii", ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_close_dialog), "Tchau");


        // CardGalery

        mBinding.cardImageGalleryComponentView.setActivityAndRegisterForCamera(this);

        if (gallery != null)
            mBinding.cardImageGalleryComponentView.setCardImages(gallery.getImages());
        mBinding.cardImageGalleryComponentView.setExampleImages();
        mBinding.cardImageGalleryComponentView.setBinding(mBinding.cardImageGalleryComponentView,
                "Denis Vieira", new Date());
        mBinding.cardImageGalleryComponentView.setImagesQuantityLimit(15, null);
        mBinding.cardImageGalleryComponentView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.cardview_light_background));
        mBinding.cardImageGalleryComponentView.enableSaveOnlyMode(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_cam),
                "oii", ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_close_dialog), "Tchau");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBinding.cardShowViewTakenPicturesView.addImageOnActivityResult(requestCode, resultCode, data);
    }


}
