package br.com.stant.libraries.cameraimagegalleryview.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryComponentView;
import br.com.stant.libraries.cameraimagegalleryview.adapters.CardImageGalleryViewAdapter;
import br.com.stant.libraries.cameraimagegalleryview.adapters.selectiontracker.CardImageGalleryItemDetailsLookup;
import br.com.stant.libraries.cameraimagegalleryview.adapters.selectiontracker.CardImageGalleryItemKeyProvider;
import br.com.stant.libraries.cameraimagegalleryview.components.Camera;
import br.com.stant.libraries.cameraimagegalleryview.components.DeleteAlertDialog;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cameraimagegalleryview.model.Theme;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraActivity;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.ActivityCardImageGalleryViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;


public class CardImageGalleryView extends AppCompatActivity {

    private ActivityCardImageGalleryViewBinding mBinding;
    private RecyclerView recyclerView;
    private CardImageGalleryViewAdapter mCardImageGalleryViewAdapter;
    private GridLayoutManager gridLayoutManager;
    private CardShowTakenImageInjection mCardShowTakenImages;
    private Integer mImageQuantityLimit;
    private Camera mCamera;
    private List<CardShowTakenImage> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_card_image_gallery_view);

        configureToolbar();
        configureAdapter();
        mBinding.galleryActivity.setBackgroundColor(Color.parseColor(Theme.ActivityBackground));
        mBinding.bottomBar.setBackgroundColor(Color.parseColor(Theme.BottomBarColor));

        mImageQuantityLimit = getIntent().getIntExtra(Camera.KEY_LIMIT_IMAGES, 0);
        showQuantityOfImages();

        registerActivityForCamera();
        mCardShowTakenImages.addListener(() -> {
            mCardImageGalleryViewAdapter.notifyDataSetChanged();
            showQuantityOfImages();
        });

        mBinding.iconCamera.setOnClickListener((view) -> {
            mCamera.pickPictureToFinishAction();
        });

        mBinding.delete.setOnClickListener(this::removeImages);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        mCardImageGalleryViewAdapter = new CardImageGalleryViewAdapter(this);
        recyclerView.setAdapter(mCardImageGalleryViewAdapter);
        configureSelectionTracker();
    }

    private void configureSelectionTracker(){
        SelectionTracker<Long> selectionTracker = new  SelectionTracker.Builder<>(
                "delete-items",
                this.recyclerView,
                new CardImageGalleryItemKeyProvider(),
                new CardImageGalleryItemDetailsLookup(this.recyclerView),
                StorageStrategy.createLongStorage())
                .build();
        mCardImageGalleryViewAdapter.setSelectionTracker(selectionTracker);
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

    public boolean isSelectModeOn() {
        return mCardImageGalleryViewAdapter.getSelectedCount() > 0;
    }

    public void changeSelectionMode() {
        String textCounter = getApplicationContext().getResources().getString(R.string.card_show_taken_picture_view_selection_counter);
        mBinding.selectedImagesCounter.setText(String.format(textCounter, mCardImageGalleryViewAdapter.getSelectedCount()));
        mBinding.setIsSelectionMode(isSelectModeOn());
    }

    private void removeImages(View view) {
        DeleteAlertDialog deleteAlertDialog = new DeleteAlertDialog(this, new DeleteAlertDialog.OnDelete() {
            @Override
            public void delete() {
                List<CardShowTakenImage> itemsToRemove = mCardImageGalleryViewAdapter.getSelectedItems();
                mCardShowTakenImages.removeList(itemsToRemove);
                deleteFinished();
            }

            @Override
            public void cancel() {
            }
        });

        deleteAlertDialog.onCreateDialog(null).show();
    }

    private void deleteFinished() {

        String textCounter = getApplicationContext().getResources().getString(R.string.card_show_taken_picture_view_confirm_image_deleted);
        textCounter = String.format(textCounter, mCardImageGalleryViewAdapter.getSelectedCount());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        builder
                .setTitle(textCounter)
                .setPositiveButton(R.string.permission_information_dialog_ok_hint, (dialogInterface, i) -> {
                    onAttachedToWindow();
                    mCardImageGalleryViewAdapter.clearSelections();
                    mBinding.setIsSelectionMode(isSelectModeOn());
                }).show();
    }
}

