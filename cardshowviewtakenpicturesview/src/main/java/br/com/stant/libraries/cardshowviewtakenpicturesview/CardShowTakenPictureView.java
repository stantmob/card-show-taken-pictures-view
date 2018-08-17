package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil.JPEG_FILE_SUFFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil.getFile;


/**
 * Created by denisvieira on 07/06/17.
 */

public class CardShowTakenPictureView extends LinearLayout implements CardShowTakenPictureViewContract {

    public static String KEY_LIMIT_IMAGES = "limit_images";
    public static String KEY_IMAGE_LIST_SIZE = "image_list_size";
    public static String KEY_IMAGE_CAMERA_LIST = "image_camera_list";
    public static final int REQUEST_IMAGE_LIST_RESULT = 2;
    private static final int REQUEST_CHOOSER_IMAGE = 1;

    public boolean canEditState;
    private File mSdcardTempImagesDirectory = getFile();
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

    public CardShowTakenPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CardShowTakenPictureView);

        mCardShowTakenPictureViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.card_show_taken_picture_view, this, true);
        mCardShowTakenPictureViewBinding.setHandler(this);
        mCardShowTakenPictureViewBinding.setCardStateEnum(CardShowTakenPictureStateEnum.NORMAL);
        unblockEditStateViewConfiguration();

        setOrientation(HORIZONTAL);

        mCardShowTakenPictureViewImagesAdapter = new CardShowTakenPictureViewImagesAdapter(getContext(), new ArrayList<>(0), this);

        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setNestedScrollingEnabled(true);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setFocusable(false);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setAdapter(mCardShowTakenPictureViewImagesAdapter);

        PhotoViewFileUtil.createTempDirectory(mSdcardTempImagesDirectory);

        mPreviewPicDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mCardShowTakenPicturePreviewDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.card_show_taken_picture_preview_dialog, null, false);
        mCardShowTakenPicturePreviewDialogBinding.setHandler(this);
        mPreviewPicDialog.setContentView(mCardShowTakenPicturePreviewDialogBinding.getRoot());

        setupEditMode();
        setupLayoutOptions();
    }

    private void setupLayoutOptions() {
        boolean showNoBorder = mStyledAttributes.getBoolean(R.styleable.CardShowTakenPictureView_showNoBorder, false);

        if (showNoBorder) {
            mCardShowTakenPictureViewBinding.cardShowTakenPictureContainer.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_rectangle_white));
        }
    }

    private void setupEditMode() {
        editModeOnly = mStyledAttributes.getBoolean(R.styleable.CardShowTakenPictureView_editModeOnly, false);

        if (editModeOnly) {
            mCardShowTakenPictureViewBinding.cardShowTakenPictureCancelText.setVisibility(GONE);
            mCardShowTakenPictureViewBinding.cardShowTakenPictureSaveText.setVisibility(GONE);
            showEditStateViewConfiguration(this);
        }
    }

    public void setColor(int color) {
        GradientDrawable drawable = (GradientDrawable) mCardShowTakenPictureViewBinding.cardShowTakenPictureContainer.getBackground().mutate();
        mCardShowTakenPictureViewBinding.cardShowTakenPictureHeaderTitle.setTextColor(ContextCompat.getColor(getContext(), color));
        drawable.setStroke(3, ContextCompat.getColor(getContext(), color));
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
        mCardShowTakenPicturePreviewDialogBinding.setImageUrl(cardShowTakenImage.getTempImagePathToShow());
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

        mOnSavedCardListener.onSaved(mCardShowTakenPictureViewImagesAdapter.getImagesAsAdded(), mCardShowTakenPictureViewImagesAdapter.getImagesAsRemoved());

        List<CardShowTakenImage> imagesAsRemoved = mCardShowTakenPictureViewImagesAdapter.getImagesAsRemoved();

        if (imagesAsRemoved.size() > 0){
            for (CardShowTakenImage cardShowTakenImage :
                    imagesAsRemoved) {
                if (cardShowTakenImage.getLocalImageFilename() != null) {
                    File file = new File(getFile() + "/" + cardShowTakenImage.getLocalImageFilename());
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
        mOnSavedCardListener.onCancel();
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
        mImagesQuantityLimit = limitQuantity;
        mOnReachedOnTheImageCountLimit = onReachedOnTheImageCountLimit;
    }

    @Override
    public boolean hasImages() {
        return mCardShowTakenPictureViewImagesAdapter.getItemCount() > 0;
    }

    @Override
    public boolean hasImageByIdentifier(String identifier) {
        List<CardShowTakenImage> cardShowTakenPictures = mCardShowTakenPictureViewImagesAdapter.getData();
        for (CardShowTakenImage cardShowTakenImage : cardShowTakenPictures) {
            if (identifier == cardShowTakenImage.getIdentifier())
                return true;
        }

        return false;
    }

    @Override
    public void setOnSavedCardListener(OnSavedCardListener onSavedCardListener) {
        mOnSavedCardListener = onSavedCardListener;
    }

    @Override
    public int getItemCount() {
        return mCardShowTakenPictureViewImagesAdapter.getItemCount();
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
        images.add(new CardShowTakenImage(null, "http://www.cityofsydney.nsw.gov.au/__data/assets/image/0009/105948/Noise__construction.jpg", new Date(), new Date()));
        images.add(new CardShowTakenImage(null, "http://facility-egy.com/wp-content/uploads/2016/07/Safety-is-important-to-the-construction-site.png", new Date(), new Date()));

        setCardImages(images);
    }

    @Override
    public void pickPictureToFinishServiceInspectionFormFilled(View view) {
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
        intent.putExtra(KEY_IMAGE_LIST_SIZE, getItemCount());
        mActivity.startActivityForResult(intent, REQUEST_IMAGE_LIST_RESULT);
    }

    public void addImageOnActivityResult(int requestCode, int resultCode, Intent data) {
        imageGenerator = new ImageGenerator(getContext(), this);

        if (requestCode == REQUEST_IMAGE_LIST_RESULT && resultCode == Activity.RESULT_OK && data != null) {

            ArrayList<CameraPhoto> cameraPhotos = (ArrayList<CameraPhoto>) data.getSerializableExtra(KEY_IMAGE_CAMERA_LIST);

            for (CameraPhoto cameraPhoto : cameraPhotos) {

                String localImage = cameraPhoto.getLocalImageFilename();

                File mPhotoDirectory = new File(mSdcardTempImagesDirectory.toString() + "/" + localImage);

                imageGenerator.generateCardShowTakenImageFromCamera(mPhotoDirectory,
                        new CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(bitmap, imageFilename, tempImagePath, new Date(), new Date());

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

    private static Bitmap getImage(String from) throws IOException {
        File file = new File(from);

        if (file.exists()) {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bufferedImage = BitmapFactory.decodeFile(from, op);
            return bufferedImage;
        }
        return null;
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public boolean hasUpdatedAt() {
        return mCardShowTakenPictureViewBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mCardShowTakenPictureViewBinding.getPictureByName() != null;
    }


}
