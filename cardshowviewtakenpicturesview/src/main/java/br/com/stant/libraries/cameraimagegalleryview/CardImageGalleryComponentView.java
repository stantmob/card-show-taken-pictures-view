package br.com.stant.libraries.cameraimagegalleryview;

import static com.annimon.stream.Optional.ofNullable;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.activities.CardImageGalleryView;
import br.com.stant.libraries.cameraimagegalleryview.adapters.CardImageGalleryComponentViewAdapterContract;
import br.com.stant.libraries.cameraimagegalleryview.enums.ImageStatus;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraActivity;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryComponentViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowPictureDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropTouchHelper;

public class CardImageGalleryComponentView extends LinearLayout implements CardImageGalleryViewContract {

    public static final String KEY_LIMIT_IMAGES = "limit_images";
    public static final String KEY_IMAGE_LIST_SIZE = "image_list_size";
    public static final String KEY_IMAGE_CAMERA_LIST = "image_camera_list";
    public static final String KEY_IS_MULTIPLE_GALLERY_SELECTION = "is_multiple_gallery_selection";
    public static final String KEY_SAVE_ONLY_MODE = "save_only_mode";
    public static final String KEY_DRAG_AND_DROP_MODE = "drag_and_drop_mode";
    public static final String KEY_IS_CAPTION_ENABLED = "is_caption_enabled";

    private AppCompatActivity mActivity;
    private Fragment mFragment;
    private Context mContext;
    private File mSdCardTempImagesDirectory = getPrivateTempDirectory(getContext());
    private File mImageTaken;
    private CardImageGalleryComponentViewBinding mCardImageGalleryComponentViewBinding;
    private CardImageGalleryComponentViewAdapterContract mCardImagesAdapterContract;
    private CardShowPictureDialogBinding mCardShowPictureDialogBinding;
    private Dialog mPreviewPicDialog;
    private TypedArray mStyledAttributes;
    private Integer mImagePosition;
    private Integer mImagesQuantityLimit;
    private OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit;
    private Intent cameraIntent;
    private ActivityResultLauncher<Intent> openCamera;
    private ImageGenerator imageGenerator;
    private ImageStatus imageStatus;
    private SaveOnlyMode mSaveOnlyMode;
    private OnSavedCardListener mOnSavedCardListener;
    private OnCaptionSavedCallback mOnCaptionSavedCallback;
    private boolean editModeOnly = false;
    private boolean canEditState = true;
    private boolean mDragAndDropMode = false;
    private boolean mIsCaptionEnabled = false;
    private boolean mIsMultipleGallerySelection = false;


    public CardImageGalleryComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CardShowTakenPictureView);

        mCardImageGalleryComponentViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.card_image_gallery_component_view, this, true);
        mCardImageGalleryComponentViewBinding.setHandler(this);

        setOrientation(HORIZONTAL);
        setImageListAdapter(mCardImageGalleryComponentViewBinding.cardImageGalleryComponentListRecyclerView);
        setupEditMode();
        setupDialog();
        attachDragAndDropTouchHelper(mCardImageGalleryComponentViewBinding.cardImageGalleryComponentListRecyclerView);
    }

    public void setActivityAndRegisterForCamera(Activity activity) {
        mActivity = (AppCompatActivity) activity;
        registerActivityForCamera();
    }

    public void setFragment(Fragment fragment){
        this.mFragment = fragment;
       setActivityAndRegisterForCamera(fragment.getActivity());
    }

    // Begin Component
    @Override
    public void setImagesQuantityLimit(Integer limitQuantity, OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit) {
        Integer currentImagesQuantity = mCardImagesAdapterContract.getItemCount();
        mImagesQuantityLimit = limitQuantity;
        mOnReachedOnTheImageCountLimit = onReachedOnTheImageCountLimit;

        mCardImageGalleryComponentViewBinding.cardImageGalleryComponentPhotosQuantityTextView.setVisibility(VISIBLE);

        updateCurrentAndLimitImagesQuantityText(currentImagesQuantity);
    }

    @Override
    public void updateCurrentAndLimitImagesQuantityText(Integer currentQuantity) {
        mCardImageGalleryComponentViewBinding.setCurrentAndLimitPhotosQuantityText(
                currentQuantity + "/" + mImagesQuantityLimit
        );
    }

    public boolean hasUpdatedAt() {
        return mCardImageGalleryComponentViewBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mCardImageGalleryComponentViewBinding.getPictureByName() != null;
    }
    @Override
    public boolean hasImages() {
        return mCardImagesAdapterContract.getItemCount() > 0;
    }

    @Override
    public boolean hasImageByIdentifier(String identifier) {
        List<CardShowTakenImage> cardShowTakenPictures = mCardImagesAdapterContract.getCurrentImages();
        for (CardShowTakenImage cardShowTakenImage : cardShowTakenPictures) {
            if (identifier.equals(cardShowTakenImage.getIdentifier()))
                return true;
        }

        return false;
    }

    @Override
    public void setCardImages(@NonNull List<CardShowTakenImage> cardShowTakenImages) {
        mCardImagesAdapterContract.replaceData(cardShowTakenImages);
    }

    @Override
    public void addCardImages(List<CardShowTakenImage> cardShowTakenImages) {
        if (cardShowTakenImages != null) {
            mCardImagesAdapterContract.addPictures(cardShowTakenImages);
        }
    }

    @Override
    public List<CardShowTakenImage> getCardImages() {
        return mCardImagesAdapterContract.getCurrentImages();
    }

    @Override
    public List<CardShowTakenImage> getCardImagesAsAdded() {
        return mCardImagesAdapterContract.getImagesAsAdded();
    }

    @Override
    public List<CardShowTakenImage> getCardImagesAsRemoved() {
        return mCardImagesAdapterContract.getImagesAsRemoved();
    }
    public void setCaptionEnable(boolean isEnable) {
        this.mIsCaptionEnabled = isEnable;
    }
    public void setImageStatus(ImageStatus imageStatus) {
        this.imageStatus = imageStatus;
    }
    public void setStrokeColor(int color) {
        GradientDrawable drawable = (GradientDrawable) mCardImageGalleryComponentViewBinding
                .cardImageGalleryComponentContainerLinearLayout.getBackground().mutate();
        mCardImageGalleryComponentViewBinding.cardImageGalleryPictureHeaderTitleTextView.setTextColor(color);
        drawable.setStroke(3, color);
    }

    public void setExampleImages() {
        List<CardShowTakenImage> images = new ArrayList<>();

        images.add(new CardShowTakenImage("", "https://upload.wikimedia.org/wikipedia/commons/b/b6/Image_created_with_a_mobile_phone.png", "", "", new Date(),
                new Date(), "CAPTION", ImageStatus.Approved, Arrays.asList("Error 1", "Error 2", "Error 3")));
        images.add(new CardShowTakenImage("", "https://www.freecodecamp.org/news/content/images/size/w2000/2022/09/jonatan-pie-3l3RwQdHRHg-unsplash.jpg", "", "", new Date(),
                new Date(), "CAPTION", ImageStatus.Disapproved, Arrays.asList()));
        setCardImages(images);
    }

    // End Component

    //Begin Dialog
    private void setupDialog() {
        mPreviewPicDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mCardShowPictureDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.card_show_picture_dialog, null, false);

        mCardShowPictureDialogBinding.setHandler(this);

        mPreviewPicDialog.setContentView(mCardShowPictureDialogBinding.getRoot());
    }

    @Override
    public void showPreviewPicDialog(CardShowTakenImage cardShowTakenImage, Integer photoPosition,
                                     OnCaptionSavedCallback onCaptionSavedCallback) {
        mImagePosition = photoPosition;
        mOnCaptionSavedCallback = onCaptionSavedCallback;

        setImageBitmapToImageView(mCardShowPictureDialogBinding.componentImage, cardShowTakenImage, 1);
        mCardShowPictureDialogBinding.cardComponentImageDialogEditCaption.setText(cardShowTakenImage.getCaption());
        mCardShowPictureDialogBinding.cardComponentImageDialogEditCaption.clearFocus();
        if (mIsCaptionEnabled) {
            mCardShowPictureDialogBinding.cardComponentDialogCaptionContainer.setVisibility(View.VISIBLE);
        } else {
            mCardShowPictureDialogBinding.cardComponentDialogCaptionContainer.setVisibility(View.GONE);
        }
        mPreviewPicDialog.show();
    }

    @Override
    public void closePreviewPicDialog(View View) {
        mPreviewPicDialog.cancel();
    }

    public void saveCaption(View view) {
        String captionText = mCardShowPictureDialogBinding.cardComponentImageDialogEditCaption.getText().toString();
        mOnCaptionSavedCallback.onCaptionSaved(captionText, mImagePosition);
        mPreviewPicDialog.dismiss();
    }

    // End Dialog

    //Begin EditState
    public void setupEditMode() {
        editModeOnly = mStyledAttributes.getBoolean(R.styleable.CardShowTakenPictureView_editModeOnly, false);

        if (editModeOnly) {
            mCardImageGalleryComponentViewBinding.cardImageGalleryPictureSaveEditIconContainerLinearLayout.setVisibility(GONE);
            mCardImageGalleryComponentViewBinding.cardImageGalleryPictureEditIconContainerLinearLayout.setVisibility(GONE);
            showEditStateViewConfiguration(this);
        }
    }

    @Override
    public void showEditStateViewConfiguration(View view) {
        mCardImagesAdapterContract.saveOriginalList();
        mCardImageGalleryComponentViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.EDIT);
        blockEditStateViewConfiguration();
        mCardImagesAdapterContract.notifyDataSetChanged();
    }

    @Override
    public void showNormalStateViewConfiguration() {
        mCardImageGalleryComponentViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();
    }

    @Override
    public void saveImageStateViewConfiguration(View view) {
        mCardImageGalleryComponentViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();
        mCardImagesAdapterContract.saveEditData();

        if (mOnSavedCardListener != null) {
            mOnSavedCardListener.onSaved(mCardImagesAdapterContract.getCurrentImages(),
                    mCardImagesAdapterContract.getImagesAsAdded(),
                    mCardImagesAdapterContract.getImagesAsRemoved());
        }

        List<CardShowTakenImage> imagesAsRemoved = mCardImagesAdapterContract.getImagesAsRemoved();

        deleteFromFileImageAsRemoved(imagesAsRemoved);
    }

    @Override
    public void cancelEditImagesStateViewConfiguration(View view) {
        mCardImageGalleryComponentViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();
        mCardImagesAdapterContract.cancelEditData();

        if (mOnSavedCardListener != null) {
            mOnSavedCardListener.onCancel();
        }
    }

    private void deleteFromFileImageAsRemoved(List<CardShowTakenImage> imagesAsRemoved) {
        if (imagesAsRemoved.size() > 0) {
            for (CardShowTakenImage cardShowTakenImage :
                    imagesAsRemoved) {
                if (cardShowTakenImage.getLocalImageFilename() != null) {
                    File file = new File(getPrivateTempDirectory(getContext()) + "/" + cardShowTakenImage.getLocalImageFilename());
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Override
    public void blockEditStateViewConfiguration() {
        canEditState = false;
        mCardImageGalleryComponentViewBinding.setCanEditState(false);
    }

    @Override
    public void unblockEditStateViewConfiguration() {
        canEditState = true;
        mCardImageGalleryComponentViewBinding.setCanEditState(true);
    }

    @Override
    public void setCardStateEnum(CardShowTakenPictureStateEnum cardStateEnum) {
        mCardImageGalleryComponentViewBinding.setCardStateEnum(cardStateEnum);
    }

    public CardShowTakenPictureStateEnum getActualCardState() {
        return mCardImageGalleryComponentViewBinding.getCardStateEnum();
    }

    public boolean isNotCanEditState() {
        return !canEditState;
    }

    @Override
    public void ifNoImagesShowEditStateViewConfigurationOnInit() {
        checkIfHasImages();
    }

    @Override
    public void checkIfHasImages() {
        if (editModeOnly || mCardImagesAdapterContract.getItemCount() == 0)
            showEditStateViewConfiguration(this);
        else
            showNormalStateViewConfiguration();
    }

    @Override
    public void setOnSavedCardListener(OnSavedCardListener mOnSavedCardListener) {
        this.mOnSavedCardListener = mOnSavedCardListener;
    }

    // End EditState

    // Begin Camera
    @Override
    public void registerActivityForCamera() {
        Intent intent = new Intent(mActivity, CameraActivity.class);

        if(mFragment != null){
            openCamera = mFragment.registerForActivityResult
                    (new ActivityResultContracts.StartActivityForResult(), this::addImageOnActivityResult);
        } else {
            openCamera = mActivity.registerForActivityResult
                    (new ActivityResultContracts.StartActivityForResult(), this::addImageOnActivityResult);
        }



        cameraIntent = intent;
    }

    @Override
    public void pickPictureToFinishAction(View view) {
        if (notAtTheImageCountLimit()) {
            openPickGalleryIntent();
        } else if (mOnReachedOnTheImageCountLimit != null) {
            mOnReachedOnTheImageCountLimit.onReached();
        }
    }

    private boolean notAtTheImageCountLimit() {
        if (mImagesQuantityLimit != null) {
            return mCardImagesAdapterContract.getItemCount() != mImagesQuantityLimit;
        }

        return true;
    }

    private void openPickGalleryIntent() {
        if (!AppPermissions.hasPermissionsOn((mActivity))) {
            AppPermissions.requestPermissionsFor(mActivity);
        }  else {
            dispatchTakePictureOrPickGalleryIntent();
        }
    }

    @Override
    public void dispatchTakePictureOrPickGalleryIntent() {

        cameraIntent.putExtra(KEY_LIMIT_IMAGES, mImagesQuantityLimit);
        cameraIntent.putExtra(KEY_IMAGE_LIST_SIZE, mCardImagesAdapterContract.getItemCount());
        cameraIntent.putExtra(KEY_IS_MULTIPLE_GALLERY_SELECTION, mIsMultipleGallerySelection);
        cameraIntent.putExtra(KEY_SAVE_ONLY_MODE, mSaveOnlyMode);
        cameraIntent.putExtra(KEY_DRAG_AND_DROP_MODE, mDragAndDropMode);
        cameraIntent.putExtra(KEY_IS_CAPTION_ENABLED, mIsCaptionEnabled);

        openCamera.launch(cameraIntent);
    }

    public void addImageOnActivityResult(ActivityResult result) {
        imageGenerator = new ImageGenerator(getContext());
        Intent data = result.getData();
        if (result.getResultCode() == Activity.RESULT_OK && data != null) {

            ArrayList<CameraPhoto> cameraImages = (ArrayList<CameraPhoto>) data.getSerializableExtra(KEY_IMAGE_CAMERA_LIST);

            for (CameraPhoto cameraImage : cameraImages) {
                String localImage = cameraImage.getLocalImageFilename();
                File mImageDirectory = new File(mSdCardTempImagesDirectory.toString() + "/" + localImage);

                imageGenerator.generateCardShowTakenImageFromCamera(mImageDirectory,
                        new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(
                                        imageFilename, tempImagePath, cameraImage.getCreatedAt(),
                                        cameraImage.getUpdatedAt(), cameraImage.getCaption(), imageStatus);

                                mCardImagesAdapterContract.addPicture(cardShowTakenImage);
                                mCardImageGalleryComponentViewBinding.cardImageGalleryComponentListRecyclerView
                                        .smoothScrollToPosition(mCardImagesAdapterContract.getItemCount() - 1);
                            }

                            @Override
                            public void onError(String message) {
                                //tratamento de erro para implementar
                            }
                        });
            }

            mCardImagesAdapterContract.notifyDataSetChanged();

        } else {
            mImageTaken = null;
        }
    }

    public void setIsMultipleGallerySelection(boolean isMultipleGallerySelection) {
        mIsMultipleGallerySelection = isMultipleGallerySelection;
    }

    public void enableSaveOnlyMode(Drawable enabledIcon, String enabledWarning, Drawable disabledIcon, String disabledWarning) {
        mSaveOnlyMode = new SaveOnlyMode(getBitmapFromDrawable(enabledIcon), enabledWarning,
                getBitmapFromDrawable(disabledIcon), disabledWarning);
    }

    public Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
                return getBitmapFromVectorDrawable(drawable);
            } else {
                throw new IllegalArgumentException("Unsupported drawable type");
            }
        } else if (drawable instanceof VectorDrawableCompat) {
            return getBitmapFromVectorDrawable(drawable);
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }
    }

    private Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    // End camera

    // Begin RecyclerViewAdapter

    public void setImageListAdapter(RecyclerView cardImageGalleryComponentViewRecyclerView) {
        mCardImagesAdapterContract = new CardImageGalleryComponentViewAdapterContract(this);
        cardImageGalleryComponentViewRecyclerView.setNestedScrollingEnabled(true);
        cardImageGalleryComponentViewRecyclerView.setHasFixedSize(true);
        cardImageGalleryComponentViewRecyclerView.setAdapter(mCardImagesAdapterContract);
    }

    public void removeImage(CardShowTakenImage image) {
        mCardImagesAdapterContract.removeImage(this, image);
    }
    private void attachDragAndDropTouchHelper(RecyclerView cardGalleryImageListRecyclerView) {
        ofNullable(mCardImagesAdapterContract).ifPresent(
                (adapter) -> {
                    DragAndDropTouchHelper dragAndDropTouchHelper = new DragAndDropTouchHelper(adapter);
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragAndDropTouchHelper);

                    adapter.setTouchHelper(itemTouchHelper);
                    itemTouchHelper.attachToRecyclerView(cardGalleryImageListRecyclerView);
                }
        );
    }
    public void goToGallery(View view) {
        List<CardShowTakenImage> imageList = mCardImagesAdapterContract.getCurrentImages();

        if (imageList != null && !imageList.isEmpty()) {

            try {
                Intent intent = new Intent(mContext, CardImageGalleryView.class);
                intent.putExtra(KEY_IMAGE_LIST_GALLERY, new ArrayList<>(imageList));
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.d("Galeria erro", e.getMessage());
            }
        } else {
            Toast.makeText(mContext, "Não há imagens disponíveis para a galeria.", Toast.LENGTH_LONG).show();
        }
    }
    public void enableDragAndDrop() {
        mDragAndDropMode = true;
    }
    public boolean dragAndDropModeIsEnabled() {
        return mDragAndDropMode;
    }
    // End RecyclerViewAdapter

    @BindingAdapter(value = {"pictureByName", "updatedAt"}, requireAll = false)
    public static void setBinding(CardImageGalleryComponentView view,
                                  String mPictureByName, Date updatedAt) {
        if (mPictureByName != null) {
            view.mCardImageGalleryComponentViewBinding.setPictureByName(mPictureByName);
        }

        if (updatedAt != null) {
            String pattern = "MM/dd/yyyy";
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            view.mCardImageGalleryComponentViewBinding.setUpdatedAt(format.format(updatedAt));
        }
    }
}