package br.com.stant.libraries.cameraimagegalleryview;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.adapters.CardImageGalleryComponentViewAdapter;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView;
import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraActivity;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryComponentViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPicturePreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;

public class CardImageGalleryComponentView extends CardShowTakenPictureView {

    private File mSdCardTempImagesDirectory = getPrivateTempDirectory(getContext());
    private File mImageTaken;
    private CardImageGalleryComponentViewBinding mCardImageGalleryComponentViewBinding;
    private CardImageGalleryComponentViewAdapter mCardImageGalleryComponentViewAdapter;
    private CardShowTakenPicturePreviewDialogBinding mCardShowTakenPicturePreviewDialogBinding;
    private OnCaptionSavedCallback mOnCaptionSavedCallback;
    private Context mContext;
    private TypedArray mStyledAttributes;
    private Integer mImagesQuantityLimit;
    private Integer mImagePosition;
    private CardShowTakenPictureViewContract.OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit;
    private ImageGenerator imageGenerator;
    private boolean mIsMultipleGallerySelection = false;
    private SaveOnlyMode mSaveOnlyMode;
    private boolean mDragAndDropMode = false;
    private boolean mIsCaptionEnabled = false;
    private Activity mActivity;
    private Fragment mFragment;
    private Dialog mPreviewPicDialog;

    public CardImageGalleryComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CardShowTakenPictureView);

        mCardImageGalleryComponentViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.card_image_gallery_component_view, this, true);
        mCardImageGalleryComponentViewBinding.setHandler(this);

        setOrientation(HORIZONTAL);

        setImageListAdapter(mCardImageGalleryComponentViewBinding.cardImageGalleryComponentListRecyclerView);

        setupLayoutOptions();

    }

    @Override
    public void pickPictureToFinishAction(View view) {
        if (notAtTheImageCountLimit()) {
            openPickGalleryIntent();
        } else if (mOnReachedOnTheImageCountLimit != null && !notAtTheImageCountLimit()){
            mOnReachedOnTheImageCountLimit.onReached();
        }
    }

    private void openPickGalleryIntent() {
        if (!AppPermissions.hasPermissionsOn((mActivity))) {
            AppPermissions.requestPermissionsFor(mActivity);
        } else {
            dispatchTakePictureOrPickGalleryIntent();
        }
    }

    private boolean notAtTheImageCountLimit() {
        if (mImagesQuantityLimit != null) {
            return mCardImageGalleryComponentViewAdapter.getItemCount() != mImagesQuantityLimit;
        }
        return true;
    }

    @Override
    public void dispatchTakePictureOrPickGalleryIntent() {
        Intent intent = new Intent(mActivity, CameraActivity.class);

        intent.putExtra(KEY_LIMIT_IMAGES, mImagesQuantityLimit);
        intent.putExtra(KEY_IMAGE_LIST_SIZE, mCardImageGalleryComponentViewAdapter.getItemCount());
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

    @Override
    public void setImagesQuantityLimit(Integer limitQuantity, CardShowTakenPictureViewContract.OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit) {
        Integer currentImagesQuantity = mCardImageGalleryComponentViewAdapter.getItemCount();
        mImagesQuantityLimit = limitQuantity;
        mOnReachedOnTheImageCountLimit = onReachedOnTheImageCountLimit;

        mCardImageGalleryComponentViewBinding.cardImageGalleryComponentPhotosQuantityTextView.setVisibility(VISIBLE);

        updateCurrentAndLimitImagesQuantityText(currentImagesQuantity);
    }

    @Override
    public void addImageOnActivityResult(int requestCode, int resultCode, Intent data) {
        imageGenerator = new ImageGenerator(getContext());

        if (requestCode == REQUEST_IMAGE_LIST_RESULT && resultCode == Activity.RESULT_OK && data != null) {

            ArrayList<CameraPhoto> cameraImages = (ArrayList<CameraPhoto>) data.getSerializableExtra(KEY_IMAGE_CAMERA_LIST);

            for (CameraPhoto cameraImage : cameraImages) {
                String localImage = cameraImage.getLocalImageFilename();
                File mImageDirectory = new File(mSdCardTempImagesDirectory.toString() + "/" + localImage);

                imageGenerator.generateCardShowTakenImageFromCamera(mImageDirectory,
                        new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(bitmap,
                                        imageFilename, tempImagePath, cameraImage.getCreatedAt(),
                                        cameraImage.getUpdatedAt(), cameraImage.getCaption()); //passar para a imagem em tela cheia.

                                mCardImageGalleryComponentViewAdapter.addPicture(cardShowTakenImage);
                                mCardImageGalleryComponentViewBinding.cardImageGalleryComponentListRecyclerView
                                        .smoothScrollToPosition(mCardImageGalleryComponentViewAdapter.getItemCount() - 1);
                            }

                            @Override
                            public void onError(String message) {
                                //tratamento de erro para implementar
                            }
                        });
            }

            mCardImageGalleryComponentViewAdapter.notifyDataSetChanged(); //Adapter não está atualizando corretamente.

        } else {
            mImageTaken = null;
        }
    }

    public void updateCurrentAndLimitImagesQuantityText(Integer currentQuantity) {
        mCardImageGalleryComponentViewBinding.setCurrentAndLimitPhotosQuantityText(
          currentQuantity + "/" + mImagesQuantityLimit
        );
    }

    @BindingAdapter(value = {"pictureByName", "updateAt"}, requireAll = false)
    public static void setBinding(CardImageGalleryComponentView view, String mPictureByName, Date updateAt) {
        if (mPictureByName != null) {
            view.mCardImageGalleryComponentViewBinding.setPictureByName(mPictureByName);
        }

        if (updateAt != null) {
            String pattern = "MM/dd/yyyy";
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            view.mCardImageGalleryComponentViewBinding.setUpdatedAt(format.format(updateAt));
        }
    }

    private void setupLayoutOptions() {
        boolean showNoBorder = mStyledAttributes.getBoolean(R.styleable.CardShowTakenPictureView_showNoBorder, false);

        if (showNoBorder) {
            mCardImageGalleryComponentViewBinding.cardImageGalleryComponentContainerLinearLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_rectangle_white));
        }
    }

    private void setImageListAdapter(RecyclerView cardImageGalleryComponentViewRecyclerView) {
        mCardImageGalleryComponentViewAdapter = new CardImageGalleryComponentViewAdapter(this);
        cardImageGalleryComponentViewRecyclerView.setNestedScrollingEnabled(true);
        cardImageGalleryComponentViewRecyclerView.setHasFixedSize(true);
        cardImageGalleryComponentViewRecyclerView.setAdapter(mCardImageGalleryComponentViewAdapter);
    }

    public boolean hasUpdatedAt() {
        return mCardImageGalleryComponentViewBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mCardImageGalleryComponentViewBinding.getPictureByName() != null;
    }

    public void saveCaption(View view) {
        String captionText = mCardShowTakenPicturePreviewDialogBinding.cameraImagePreviewDialogEditCaption.getText().toString();
        mOnCaptionSavedCallback.onCaptionSaved(captionText, mImagePosition);
        mPreviewPicDialog.dismiss();
    }

    public void goToGallery(View view) {
        List<CardShowTakenImage> imageList = mCardImageGalleryComponentViewAdapter.getCurrentImages();

        if (imageList != null && !imageList.isEmpty()) {
            Intent intent = new Intent(mContext, CardImageGalleryView.class);
            intent.putExtra("imageList", new ArrayList<>(imageList));
            mContext.startActivity(intent);
        } else {
            Toast.makeText(mContext, "Não há imagens disponíveis para a galeria.", Toast.LENGTH_LONG).show();
        }
    }
}