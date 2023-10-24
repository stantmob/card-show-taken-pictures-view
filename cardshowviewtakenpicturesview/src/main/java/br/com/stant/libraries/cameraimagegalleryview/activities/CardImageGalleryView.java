package br.com.stant.libraries.cameraimagegalleryview.activities;


import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_LIST_GALLERY;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryComponentView;
import br.com.stant.libraries.cameraimagegalleryview.adapters.CardImageGalleryViewAdapter;
import br.com.stant.libraries.cameraimagegalleryview.components.Camera;
import br.com.stant.libraries.cameraimagegalleryview.components.DeleteAlertDialog;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cameraimagegalleryview.model.ImageStatus;
import br.com.stant.libraries.cameraimagegalleryview.model.Theme;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraActivity;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.ActivityCardImageGalleryViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;


public class CardImageGalleryView extends AppCompatActivity {

    private ActivityCardImageGalleryViewBinding mBinding;
    private RecyclerView recyclerView;
    private CardImageGalleryViewAdapter cardImageGalleryViewAdapter;
    private GridLayoutManager gridLayoutManager;
    private CardShowTakenImageInjection mCardShowTakenImages;
    private Integer mImageQuantityLimit;
    private Camera mCamera;
    private ImageStatus mImageStatus;
    private List<CardShowTakenImage> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_card_image_gallery_view);

        configureToolbar();
        configureAdapter();
        mBinding.galleryActivity.setBackgroundColor(Color.parseColor(Theme.ActivityBackground));

        mImageQuantityLimit = getIntent().getIntExtra(Camera.KEY_LIMIT_IMAGES, 0);
        showQuantityOfImages();

        registerActivityForCamera();
        mCardShowTakenImages.addListener(() -> {
            cardImageGalleryViewAdapter.notifyDataSetChanged();
            showQuantityOfImages();
        });

        mBinding.iconCamera.setOnClickListener((view) -> {
            mCamera.pickPictureToFinishAction();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        this.selectedImages.clear();
    }

    private void configureToolbar() {
        setSupportActionBar(mBinding.topAppBar);
        mBinding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        String appBarName = getIntent().getStringExtra(CardImageGalleryComponentView.KEY_APP_BAR_NAME);
        getSupportActionBar().setTitle(appBarName);

        mBinding.topAppBar.setBackgroundColor(Color.parseColor(Theme.ToolBarColor));
        getWindow().setStatusBarColor(Color.parseColor(Theme.StatusBarColor));
        getSupportActionBar().setHomeAsUpIndicator(Theme.BackIcon);
        mBinding.topAppBar.setTitleTextColor(Color.parseColor(Theme.TitleToolBarColor));
    }

    private void configureAdapter() {
        recyclerView = findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mCardShowTakenImages = CardShowTakenImageInjection.getCardShowTakenPictureInjection();

        List<CardShowTakenImage> cardShowTakenImageList = new ArrayList<>();
        List<CardShowTakenImage> receivedImageList = mCardShowTakenImages.getAll();

        if (receivedImageList != null && !receivedImageList.isEmpty()) {
            cardShowTakenImageList.addAll(receivedImageList);
        }
        cardImageGalleryViewAdapter = new CardImageGalleryViewAdapter(this, cardShowTakenImageList);
        recyclerView.setAdapter(cardImageGalleryViewAdapter);
    }

    public void registerActivityForCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        mCamera = new Camera(this, this);

        mCamera.setCameraIntent(intent);
        mCamera.setOpenCamera(this.registerForActivityResult
                (new ActivityResultContracts.StartActivityForResult(), mCamera::addImageOnActivityResult));
    }

    private void showQuantityOfImages() {
        mBinding.quantity.setText(
                mCardShowTakenImages.getAll().size() + "/" + mImageQuantityLimit
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isSelectModeOn()) {
            MenuInflater menuInflater = getMenuInflater();

            menuInflater.inflate(R.menu.gallery_trash, menu);
            configureMenu(menu);
            return true;
        }
        return false;
    }

    private void configureMenu(Menu menu) {
        ((TextView) menu.findItem(R.id.gallery_trash_menu).getActionView()
                .findViewById(R.id.count_images_trash)).setText("(" + this.selectedImages.size() + ")");
        ((TextView) menu.findItem(R.id.gallery_trash_menu).getActionView()
                .findViewById(R.id.count_images_trash)).setTextColor(Color.parseColor(Theme.TitleToolBarColor));
        ((ImageView) menu.findItem(R.id.gallery_trash_menu).getActionView()
                .findViewById(R.id.gallery_trash)).setColorFilter(Color.parseColor(Theme.ColorIcons));
        menu.findItem(R.id.gallery_trash_menu).getActionView().setOnClickListener((view) -> {
            removeImages();
        });
    }



    public boolean isSelectModeOn() {
        return !this.selectedImages.isEmpty();
    }

    public void addImageSelect(CardShowTakenImage image) {
        this.selectedImages.add(image);
        invalidateOptionsMenu();
    }

    public void removeImageSelectFromSelectionMode(CardShowTakenImage image) {
        this.selectedImages.remove(image);
        invalidateOptionsMenu();

    }

    private void removeImages() {
        DeleteAlertDialog deleteAlertDialog = new DeleteAlertDialog(this, new DeleteAlertDialog.OnDelete() {
            @Override
            public void delete() {
                mCardShowTakenImages.removeList(selectedImages);
                selectedImages.clear();
                invalidateOptionsMenu();
                onAttachedToWindow();
            }

            @Override
            public void cancel() {
            }
        });

        deleteAlertDialog.onCreateDialog(null).show();
    }
}

