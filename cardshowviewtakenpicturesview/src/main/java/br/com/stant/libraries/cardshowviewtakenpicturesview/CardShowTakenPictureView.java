package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraActivity;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPicturePreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;

public class CardShowTakenPictureView extends LinearLayout implements CardShowTakenPictureViewContract {

    public static final String KEY_LIMIT_IMAGES                  = "limit_images";
    public static final String KEY_IMAGE_LIST_SIZE               = "image_list_size";
    public static final String KEY_IMAGE_CAMERA_LIST             = "image_camera_list";
    public static final String KEY_IS_MULTIPLE_GALLERY_SELECTION = "is_multiple_gallery_selection";
    public static final int REQUEST_IMAGE_LIST_RESULT            = 2;
    public boolean canEditState;
    private File mSdcardTempImagesDirectory = getPrivateTempDirectory(getContext());
    private File mPhotoTaken;
    private CardShowTakenPictureViewBinding mCardShowTakenPictureViewBinding;
    private CardShowTakenPicturePreviewDialogBinding mCardShowTakenPicturePreviewDialogBinding;
    private Context mContext;
    private Activity mActivity;
    private CardShowTakenPictureViewImagesAdapter mCardShowTakenPictureViewImagesAdapter;
    private Fragment mFragment;
    private Dialog mPreviewPicDialog;
    private boolean editModeOnly;
    private OnSavedCardListener mOnSavedCardListener;
    private TypedArray mStyledAttributes;
    private Integer mImagesQuantityLimit;
    private OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit;
    private ImageGenerator imageGenerator;
    private boolean mIsMultipleGallerySelection = false;

    public CardShowTakenPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext          = context;
        this.mStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CardShowTakenPictureView);

        mCardShowTakenPictureViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.card_show_taken_picture_view, this, true);
        mCardShowTakenPictureViewBinding.setHandler(this);
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);

        unblockEditStateViewConfiguration();

        setOrientation(HORIZONTAL);

        setAdapter();

        setupDialog();
        setupEditMode();
        setupLayoutOptions();
    }

    private void setupDialog() {
        mPreviewPicDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mCardShowTakenPicturePreviewDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.card_show_taken_picture_preview_dialog, null, false);
        mCardShowTakenPicturePreviewDialogBinding.setHandler(this);
        mPreviewPicDialog.setContentView(mCardShowTakenPicturePreviewDialogBinding.getRoot());
    }

    private void setAdapter() {
        mCardShowTakenPictureViewImagesAdapter = new CardShowTakenPictureViewImagesAdapter(new ArrayList<>(0), this);

        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setNestedScrollingEnabled(true);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setFocusable(false);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setAdapter(mCardShowTakenPictureViewImagesAdapter);
    }

    private void setupLayoutOptions() {
        boolean showNoBorder = mStyledAttributes.getBoolean(R.styleable.CardShowTakenPictureView_showNoBorder, false);

        if (showNoBorder) {
            mCardShowTakenPictureViewBinding.cardShowTakenPictureContainerLinearLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_rectangle_white));
        }
    }

    public void setupEditMode() {
        editModeOnly = mStyledAttributes.getBoolean(R.styleable.CardShowTakenPictureView_editModeOnly, false);;

        if (editModeOnly) {
            mCardShowTakenPictureViewBinding.cardShowTakenPictureSaveEditIconContainerLinearLayout.setVisibility(GONE);
            mCardShowTakenPictureViewBinding.cardShowTakenPictureEditIconContainerLinearLayout.setVisibility(GONE);
            showEditStateViewConfiguration(this);
        }
    }

    public void setStrokeColor(int color) {
        GradientDrawable drawable = (GradientDrawable) mCardShowTakenPictureViewBinding.cardShowTakenPictureContainerLinearLayout.getBackground().mutate();
        mCardShowTakenPictureViewBinding.cardShowTakenPictureHeaderTitleTextView.setTextColor(color);
        drawable.setStroke(3, color);
    }

    public void setBackgroundColor(int color){
        GradientDrawable drawable = (GradientDrawable) mCardShowTakenPictureViewBinding.cardShowTakenPictureContainerLinearLayout.getBackground().mutate();
        drawable.setColor(color);
    }

    public void setIsMultipleGallerySelection(boolean isMultipleGallerySelection){
        mIsMultipleGallerySelection = isMultipleGallerySelection;
    }

    public void updateCurrentAndLimitPhotosQuantityText(Integer currentQuantity) {
        mCardShowTakenPictureViewBinding.setCurrentAndLimitPhotosQuantityText(
                currentQuantity + "/" + mImagesQuantityLimit);
    }

    @BindingAdapter(value = {"pictureByName", "updatedAt"}, requireAll = false)
    public static void setBinding(CardShowTakenPictureView view,
                                  String mPictureByName, Date updatedAt) {

        if (mPictureByName != null)
            view.mCardShowTakenPictureViewBinding.setPictureByName(mPictureByName);

        if (updatedAt != null) {
            String pattern = "MM/dd/yyyy";
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            view.mCardShowTakenPictureViewBinding.setUpdatedAt(format.format(updatedAt));
        }
    }

    @Override
    public void checkIfHasImages() {
        if (editModeOnly || mCardShowTakenPictureViewImagesAdapter.getItemCount() == 0)
            showEditStateViewConfiguration(this);
        else
            showNormalStateViewConfiguration();
    }

    @Override
    public void showPreviewPicDialog(CardShowTakenImage cardShowTakenImage) {
        setImageBitmapToImageView(mCardShowTakenPicturePreviewDialogBinding.previewImage, cardShowTakenImage, 1);

        mPreviewPicDialog.show();
    }

    @Override
    public void closePreviewPicDialog(View View) {
        mPreviewPicDialog.cancel();
    }

    @Override
    public void showEditStateViewConfiguration(View view) {
        mCardShowTakenPictureViewImagesAdapter.saveOriginalList();
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.EDIT);
        blockEditStateViewConfiguration();
        mCardShowTakenPictureViewImagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNormalStateViewConfiguration() {
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();
    }

    @Override
    public void saveImageStateViewConfiguration(View view) {
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();
        mCardShowTakenPictureViewImagesAdapter.saveEditData();

        if (mOnSavedCardListener != null) {
            mOnSavedCardListener.onSaved(mCardShowTakenPictureViewImagesAdapter.getImagesAsAdded(),
                    mCardShowTakenPictureViewImagesAdapter.getImagesAsRemoved());
        }

        List<CardShowTakenImage> imagesAsRemoved = mCardShowTakenPictureViewImagesAdapter.getImagesAsRemoved();

        deleteFromFileImageAsRemoved(imagesAsRemoved);
    }

    private void deleteFromFileImageAsRemoved(List<CardShowTakenImage> imagesAsRemoved) {
        if (imagesAsRemoved.size() > 0){
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
    public void cancelEditImagesStateViewConfiguration(View view) {
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();
        mCardShowTakenPictureViewImagesAdapter.cancelEditData();

        if (mOnSavedCardListener != null) {
            mOnSavedCardListener.onCancel();
        }
    }

    @Override
    public void blockEditStateViewConfiguration() {
        canEditState = false;
        mCardShowTakenPictureViewBinding.setCanEditState(false);
    }

    @Override
    public void unblockEditStateViewConfiguration() {
        canEditState = true;
        mCardShowTakenPictureViewBinding.setCanEditState(true);
    }

    @Override
    public void setCardStateEnum(CardShowTakenPictureStateEnum cardStateEnum) {
        mCardShowTakenPictureViewBinding.setCardStateEnum(cardStateEnum);
    }

    @Override
    public void ifNoImagesShowEditStateViewConfigurationOnInit() {
        checkIfHasImages();
    }

    @Override
    public void setImagesQuantityLimit(Integer limitQuantity, OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit) {
        Integer currentImagesQuantity  = mCardShowTakenPictureViewImagesAdapter.getItemCount();
        mImagesQuantityLimit           = limitQuantity;
        mOnReachedOnTheImageCountLimit = onReachedOnTheImageCountLimit;

        mCardShowTakenPictureViewBinding.cardShowTakenPictureCurrentPhotosQuantityTextView.setVisibility(VISIBLE);

        updateCurrentAndLimitPhotosQuantityText(currentImagesQuantity);
    }

    @Override
    public boolean hasImages() {
        return mCardShowTakenPictureViewImagesAdapter.getItemCount() > 0;
    }

    @Override
    public boolean hasImageByIdentifier(String identifier) {
        List<CardShowTakenImage> cardShowTakenPictures = mCardShowTakenPictureViewImagesAdapter.getData();
        for (CardShowTakenImage cardShowTakenImage : cardShowTakenPictures) {
            if (identifier.equals(cardShowTakenImage.getIdentifier()))
                return true;
        }

        return false;
    }

    @Override
    public void setOnSavedCardListener(OnSavedCardListener onSavedCardListener) {
        mOnSavedCardListener = onSavedCardListener;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
    }

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    public CardShowTakenPictureStateEnum getActualCardState() {
        return mCardShowTakenPictureViewBinding.getCardStateEnum();
    }

    @Override
    public void setCardImages(List<CardShowTakenImage> cardShowTakenImages) {
        if (cardShowTakenImages != null) {
            mCardShowTakenPictureViewImagesAdapter.replaceData(cardShowTakenImages);
        }
    }

    @Override
    public void addCardImages(List<CardShowTakenImage> cardShowTakenImages) {
        if (cardShowTakenImages != null) {
            mCardShowTakenPictureViewImagesAdapter.addPictures(cardShowTakenImages);
        }
    }

    public List<CardShowTakenImage> getCardImages() {
        return mCardShowTakenPictureViewImagesAdapter.getData();
    }

    @Override
    public List<CardShowTakenImage> getCardImagesAsAdded() {
        return mCardShowTakenPictureViewImagesAdapter.getImagesAsAdded();
    }

    @Override
    public List<CardShowTakenImage> getCardImagesAsRemoved() {
        return mCardShowTakenPictureViewImagesAdapter.getImagesAsRemoved();
    }

    public void setExampleImages() {
        List<CardShowTakenImage> images = new ArrayList<>();
        images.add(new CardShowTakenImage(null, "https://www.cityofsydney.nsw.gov.au/__data/assets/image/0009/105948/Noise__construction.jpg", new Date(), new Date()));
        images.add(new CardShowTakenImage(null, "http://facility-egy.com/wp-content/uploads/2016/07/Safety-is-important-to-the-construction-site.png", new Date(), new Date()));

        setCardImages(images);
    }

    @Override
    public void pickPictureToFinishAction(View view) {
        if (notAtTheImageCountLimit()) {
            openPickGalleryIntent();
        } else if (mOnReachedOnTheImageCountLimit != null && !notAtTheImageCountLimit()) {
            mOnReachedOnTheImageCountLimit.onReached();
        }
    }

    private boolean notAtTheImageCountLimit() {
        if (mImagesQuantityLimit != null) {
            return mCardShowTakenPictureViewImagesAdapter.getItemCount() != mImagesQuantityLimit;
        }
        return true;
    }

    private void openPickGalleryIntent() {
        if (!AppPermissions.hasPermissions((mActivity))) {
            AppPermissions.requestPermissions(mActivity);
        } else {
            dispatchTakePictureOrPickGalleryIntent();
        }
    }

    @Override
    public void dispatchTakePictureOrPickGalleryIntent() {
        Intent intent = new Intent(mActivity, CameraActivity.class);
        intent.putExtra(KEY_LIMIT_IMAGES, mImagesQuantityLimit);
        intent.putExtra(KEY_IMAGE_LIST_SIZE, mCardShowTakenPictureViewImagesAdapter.getItemCount());
        intent.putExtra(KEY_IS_MULTIPLE_GALLERY_SELECTION, mIsMultipleGallerySelection);

        if (mFragment != null) {
            mFragment.startActivityForResult(intent, REQUEST_IMAGE_LIST_RESULT);
        } else if (mActivity != null) {
            mActivity.startActivityForResult(intent, REQUEST_IMAGE_LIST_RESULT);
        }
    }

    public void addImageOnActivityResult(int requestCode, int resultCode, Intent data) {
        imageGenerator = new ImageGenerator(getContext());

        if (requestCode == REQUEST_IMAGE_LIST_RESULT && resultCode == Activity.RESULT_OK && data != null) {

            ArrayList<CameraPhoto> cameraPhotos = (ArrayList<CameraPhoto>) data.getSerializableExtra(KEY_IMAGE_CAMERA_LIST);

            for (CameraPhoto cameraPhoto : cameraPhotos) {

                String localImage = cameraPhoto.getLocalImageFilename();

                File mPhotoDirectory = new File(mSdcardTempImagesDirectory.toString() + "/" + localImage);

                imageGenerator.generateCardShowTakenImageFromCamera(mPhotoDirectory,
                        new CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(bitmap, imageFilename, tempImagePath, cameraPhoto.getCreatedAt(), cameraPhoto.getUpdatedAt());

                                mCardShowTakenPictureViewImagesAdapter.addPicture(cardShowTakenImage);
                                mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.smoothScrollToPosition(mCardShowTakenPictureViewImagesAdapter.getItemCount() - 1);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }

            mCardShowTakenPictureViewImagesAdapter.notifyDataSetChanged();

        } else {
            mPhotoTaken = null;
        }

    }

    public boolean hasUpdatedAt() {
        return mCardShowTakenPictureViewBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mCardShowTakenPictureViewBinding.getPictureByName() != null;
    }


}
