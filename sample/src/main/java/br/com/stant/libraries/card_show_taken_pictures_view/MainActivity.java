package br.com.stant.libraries.card_show_taken_pictures_view;

import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.List;

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

        //noinspection AccessStaticViaInstance
        mBinding.cardShowViewTakenPicturesView.setBinding(mBinding.cardShowViewTakenPicturesView, "Denis Vieira", new Date());
//        mBinding.cardShowViewTakenPicturesView.setImagesQuantityLimit(15, null);
        mBinding.cardShowViewTakenPicturesView.setIsMultipleGallerySelection(true);
        mBinding.cardShowViewTakenPicturesView.enableDragAndDrop();
        mBinding.cardShowViewTakenPicturesView.setStrokeColor(ContextCompat.getColor(getApplicationContext(),
                R.color.colorPrimary));
        mBinding.cardShowViewTakenPicturesView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.colorPrimary));
        mBinding.cardShowViewTakenPicturesView.enableSaveOnlyMode(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_cam),
                "oii", ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_close_dialog), "Tchau");
        mBinding.cardImageGalleryComponentView.setGalleryAppName("Imagens inspeções");

        // CardGalery

        mBinding.cardImageGalleryComponentView.setActivityAndRegisterForCamera(this);


        mBinding.cardImageGalleryComponentView.setExampleImages();
        mBinding.cardImageGalleryComponentView.setReadyModeOn(false);
        mBinding.cardImageGalleryComponentView.setBinding(mBinding.cardImageGalleryComponentView,
                "Denis Vieira", new Date());
        mBinding.cardImageGalleryComponentView.setImagesQuantityLimit(100, null);
        mBinding.cardImageGalleryComponentView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.cardview_light_background));
        mBinding.cardImageGalleryComponentView.enableSaveOnlyMode(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_cam),
                "oii", ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_close_dialog), "Tchau");
        mBinding.cardImageGalleryComponentView.setBackIcon(getDrawable(R.drawable.ic_arrow_left_white));
        mBinding.cardImageGalleryComponentView.setColorIcons("#ee3923");
        mBinding.cardImageGalleryComponentView.setTitleToolBarColor("#ee3923");
        mBinding.cardImageGalleryComponentView.setGalleryAppName("Imagens das inspeções");
        mBinding.cardImageGalleryComponentView.setToolBarColor("#989898");
        mBinding.cardImageGalleryComponentView.setStatusBarColor("#a3a3a3");
        mBinding.cardImageGalleryComponentView.setActivityBackgroundColor("#FFFFFF");
        mBinding.cardImageGalleryComponentView.setBottomBarColor("#FFFFFF");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mBinding.cardShowViewTakenPicturesView.addImageOnActivityResult(requestCode, resultCode, data);
    }


}
