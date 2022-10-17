package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPicturePreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropTouchHelper;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;
import static com.annimon.stream.Optional.ofNullable;

public class CardShowTakenPictureView extends LinearLayout implements CardShowTakenPictureViewContract {

    public static final String KEY_LIMIT_IMAGES                  = "limit_images";
    public static final String KEY_IMAGE_LIST_SIZE               = "image_list_size";
    public static final String KEY_IMAGE_CAMERA_LIST             = "image_camera_list";
    public static final String KEY_IS_MULTIPLE_GALLERY_SELECTION = "is_multiple_gallery_selection";
    public static final String KEY_SAVE_ONLY_MODE                = "save_only_mode";
    public static final String KEY_DRAG_AND_DROP_MODE            = "drag_and_drop_mode";
    public static final String KEY_IS_CAPTION_ENABLED            = "is_caption_enabled";
    public static final int REQUEST_IMAGE_LIST_RESULT            = 2;

    private boolean canEditState;
    private File mSdcardTempImagesDirectory = getPrivateTempDirectory(getContext());
    private File mImageTaken;
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
    private SaveOnlyMode mSaveOnlyMode;
    private boolean mDragAndDropMode = false;
    private OnCaptionSavedCallback mOnCaptionSavedCallback;
    private Integer mImagePosition;
    private Boolean mIsCaptionEnabled = false;

    public CardShowTakenPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext          = context;
        this.mStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CardShowTakenPictureView);

        mCardShowTakenPictureViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.card_show_taken_picture_view, this, true);
        mCardShowTakenPictureViewBinding.setHandler(this);
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);

        unblockEditStateViewConfiguration();

        setOrientation(HORIZONTAL);

        setImageListAdapter(mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView);

        attachDragAndDropTouchHelper(mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView);

        setupDialog();
        setupEditMode();
        setupLayoutOptions();
    }

    public void enableCaption(Boolean useCaption) {
        mIsCaptionEnabled = useCaption;
        setupDialog();
    }

    private void setupDialog() {
        mPreviewPicDialog                         = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mCardShowTakenPicturePreviewDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext),
                R.layout.card_show_taken_picture_preview_dialog, null, false);

        mCardShowTakenPicturePreviewDialogBinding.setHandler(this);

        if (mIsCaptionEnabled) {
            mCardShowTakenPicturePreviewDialogBinding.cameraImagePreviewDialogCaptionContainer.setVisibility(View.VISIBLE);
        } else {
            mCardShowTakenPicturePreviewDialogBinding.cameraImagePreviewDialogCaptionContainer.setVisibility(View.GONE);
        }

        mPreviewPicDialog.setContentView(mCardShowTakenPicturePreviewDialogBinding.getRoot());
    }

    private void setImageListAdapter(RecyclerView cardShowTakenPictureImageListRecyclerView) {
        mCardShowTakenPictureViewImagesAdapter = new CardShowTakenPictureViewImagesAdapter(this);

        cardShowTakenPictureImageListRecyclerView.setNestedScrollingEnabled(true);
        cardShowTakenPictureImageListRecyclerView.setHasFixedSize(true);
        cardShowTakenPictureImageListRecyclerView.setAdapter(mCardShowTakenPictureViewImagesAdapter);
    }

    private void attachDragAndDropTouchHelper(RecyclerView cardShowTakenPictureImageListRecyclerView) {
        ofNullable(mCardShowTakenPictureViewImagesAdapter).ifPresent(
                (adapter) -> {
                    DragAndDropTouchHelper dragAndDropTouchHelper = new DragAndDropTouchHelper(adapter);
                    ItemTouchHelper itemTouchHelper               = new ItemTouchHelper(dragAndDropTouchHelper);

                    adapter.setTouchHelper(itemTouchHelper);
                    itemTouchHelper.attachToRecyclerView(cardShowTakenPictureImageListRecyclerView);
                }
        );
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
        GradientDrawable drawable = (GradientDrawable) mCardShowTakenPictureViewBinding
                .cardShowTakenPictureContainerLinearLayout.getBackground().mutate();
        mCardShowTakenPictureViewBinding.cardShowTakenPictureHeaderTitleTextView.setTextColor(color);
        drawable.setStroke(3, color);
    }

    public void setBackgroundColor(int color){
        GradientDrawable drawable = (GradientDrawable) mCardShowTakenPictureViewBinding
                .cardShowTakenPictureContainerLinearLayout.getBackground().mutate();
        drawable.setColor(color);
    }

    public void setIsMultipleGallerySelection(boolean isMultipleGallerySelection){
        mIsMultipleGallerySelection = isMultipleGallerySelection;
    }

    public void updateCurrentAndLimitImagesQuantityText(Integer currentQuantity) {
        mCardShowTakenPictureViewBinding.setCurrentAndLimitPhotosQuantityText(
                currentQuantity + "/" + mImagesQuantityLimit);
    }

    public void enableSaveOnlyMode(Drawable enabledIcon, String enabledWarning, Drawable disabledIcon, String disabledWarning) {
        mSaveOnlyMode = new SaveOnlyMode(getBitmapFromDrawable(enabledIcon), enabledWarning,
                getBitmapFromDrawable(disabledIcon), disabledWarning);
    }

    public void enableDragAndDrop() {
        mDragAndDropMode = true;
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

    @BindingAdapter(value = {"pictureByName", "updatedAt"}, requireAll = false)
    public static void setBinding(CardShowTakenPictureView view,
                                  String mPictureByName, Date updatedAt) {
        if (mPictureByName != null) {
            view.mCardShowTakenPictureViewBinding.setPictureByName(mPictureByName);
        }

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
    public void showPreviewPicDialog(CardShowTakenImage cardShowTakenImage,
                                     Integer imagePosition,
                                     OnCaptionSavedCallback onCaptionSavedCallback) {
        mImagePosition = imagePosition;
        mOnCaptionSavedCallback = onCaptionSavedCallback;

        setImageBitmapToImageView(mCardShowTakenPicturePreviewDialogBinding.previewImage, cardShowTakenImage, 1);
        mCardShowTakenPicturePreviewDialogBinding.cameraImagePreviewDialogEditCaption.setText(cardShowTakenImage.getCaption());

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
            mOnSavedCardListener.onSaved(mCardShowTakenPictureViewImagesAdapter.getCurrentImages(),
                    mCardShowTakenPictureViewImagesAdapter.getImagesAsAdded(),
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

        updateCurrentAndLimitImagesQuantityText(currentImagesQuantity);
    }

    @Override
    public boolean hasImages() {
        return mCardShowTakenPictureViewImagesAdapter.getItemCount() > 0;
    }

    @Override
    public boolean hasImageByIdentifier(String identifier) {
        List<CardShowTakenImage> cardShowTakenPictures = mCardShowTakenPictureViewImagesAdapter.getCurrentImages();
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
        return mCardShowTakenPictureViewImagesAdapter.getCurrentImages();
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
        if (!AppPermissions.hasPermissionsOn((mActivity))) {
            AppPermissions.requestPermissionsFor(mActivity);
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
        intent.putExtra(KEY_SAVE_ONLY_MODE, mSaveOnlyMode);
        intent.putExtra(KEY_DRAG_AND_DROP_MODE, mDragAndDropMode);
        intent.putExtra(KEY_IS_CAPTION_ENABLED, mIsCaptionEnabled);

        if (mFragment != null) {
            mFragment.startActivityForResult(intent, REQUEST_IMAGE_LIST_RESULT);
        } else if (mActivity != null) {
            mActivity.startActivityForResult(intent, REQUEST_IMAGE_LIST_RESULT);
        }
    }

    public void addImageOnActivityResult(int requestCode, int resultCode, Intent data) {
        imageGenerator = new ImageGenerator(getContext());

        if (requestCode == REQUEST_IMAGE_LIST_RESULT && resultCode == Activity.RESULT_OK && data != null) {

            ArrayList<CameraPhoto> cameraImages = (ArrayList<CameraPhoto>) data.getSerializableExtra(KEY_IMAGE_CAMERA_LIST);

            for (CameraPhoto cameraImage : cameraImages) {

                String localImage = cameraImage.getLocalImageFilename();

                File mImageDirectory = new File(mSdcardTempImagesDirectory.toString() + "/" + localImage);

                imageGenerator.generateCardShowTakenImageFromCamera(mImageDirectory,
                        new CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(bitmap,
                                        imageFilename, tempImagePath, cameraImage.getCreatedAt(),
                                        cameraImage.getUpdatedAt(), cameraImage.getCaption());

                                mCardShowTakenPictureViewImagesAdapter.addPicture(cardShowTakenImage);
                                mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.smoothScrollToPosition(mCardShowTakenPictureViewImagesAdapter.getItemCount() - 1);
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
            }

            mCardShowTakenPictureViewImagesAdapter.notifyDataSetChanged();

        } else {
            mImageTaken = null;
        }

    }

    public boolean hasUpdatedAt() {
        return mCardShowTakenPictureViewBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mCardShowTakenPictureViewBinding.getPictureByName() != null;
    }

    public boolean isCanEditState() {
        return canEditState;
    }

    public boolean isNotCanEditState() {
        return !canEditState;
    }

    public void saveCaption(View view) {
        String captionText = mCardShowTakenPicturePreviewDialogBinding.cameraImagePreviewDialogEditCaption.getText().toString();
        mOnCaptionSavedCallback.onCaptionSaved(captionText, mImagePosition);
        mPreviewPicDialog.dismiss();
    }

    public boolean dragAndDropModeIsEnabled() {
        return mDragAndDropMode;
    }


}
