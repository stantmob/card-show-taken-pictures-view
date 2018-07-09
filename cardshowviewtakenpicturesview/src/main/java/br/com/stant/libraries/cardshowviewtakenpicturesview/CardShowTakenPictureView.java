package br.com.stant.libraries.cardshowviewtakenpicturesview;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPicturePreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.FileUtil;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by denisvieira on 07/06/17.
 */

public class CardShowTakenPictureView extends LinearLayout implements CardShowTakenPictureViewContract {

    private static final String TEMP_IMAGE_BASE_NAME = "card_show_taken_picture_temp_image";

    private static final int REQUEST_CHOOSER_IMAGE = 1;

    private CardShowTakenPictureViewBinding mCardShowTakenPictureViewBinding;
    private CardShowTakenPicturePreviewDialogBinding mCardShowTakenPicturePreviewDialogBinding;
    private Context mContext;
    private Activity mActivity;
    private CardShowTakenPictureViewImagesAdapter mCardShowTakenPictureViewImagesAdapter;
    private Fragment mFragment;
    private Dialog mPreviewPicDialog;

    private File mPhotoTaken;
    File compressedImage = null;

    File sdcardTempImagesDir = FileUtil.getFile();
    public boolean canEditState;
    private boolean editModeOnly;
    private OnSavedCardListener mOnSavedCardListener;
    private TypedArray mStyledAttributes;
    private Integer mImagesQuantityLimit;
    private OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit;

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

        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setLayoutManager(layout);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setNestedScrollingEnabled(true);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setFocusable(false);
        mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.setAdapter(mCardShowTakenPictureViewImagesAdapter);

        FileUtil.createTempDirectory(sdcardTempImagesDir);

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
        images.add(new CardShowTakenImage(null, "http://www.cityofsydney.nsw.gov.au/__data/assets/image/0009/105948/Noise__construction.jpg"));
        images.add(new CardShowTakenImage(null, "http://facility-egy.com/wp-content/uploads/2016/07/Safety-is-important-to-the-construction-site.png"));

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
        Intent galleryPickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String pickTitle = getResources().getString(R.string.card_show_taken_picture_view_request_chooser_msg);
        Intent chooserIntent = Intent.createChooser(galleryPickIntent, pickTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

        if (chooserIntent.resolveActivity(getContext().getPackageManager()) != null) {
            mPhotoTaken = FileUtil.prepareFile(takePhotoIntent);
            if (mFragment != null)
                mFragment.startActivityForResult(chooserIntent, REQUEST_CHOOSER_IMAGE);
            else if (mActivity != null)
                mActivity.startActivityForResult(chooserIntent, REQUEST_CHOOSER_IMAGE);
        }
    }

    public void addImageOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSER_IMAGE && resultCode == Activity.RESULT_OK && (data == null || data.getData() == null)) {

            generateCardShowTakenImageFromCamera(mPhotoTaken, mActivity, new CardShowTakenCompressedCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                    CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(bitmap, imageFilename, tempImagePath);

                    mCardShowTakenPictureViewImagesAdapter.addPicture(cardShowTakenImage);
                    mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.smoothScrollToPosition(mCardShowTakenPictureViewImagesAdapter.getItemCount() - 1);
                }

                @Override
                public void onError() {

                }
            });

        } else if (requestCode == REQUEST_CHOOSER_IMAGE && resultCode == Activity.RESULT_OK && data.getData() != null) {
            generateCardShowTakenImageFromImageGallery(mPhotoTaken, data, mActivity, new CardShowTakenCompressedCallback() {
                @Override
                public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                    CardShowTakenImage cardShowTakenImage = new CardShowTakenImage(bitmap, imageFilename, tempImagePath);

                    mCardShowTakenPictureViewImagesAdapter.addPicture(cardShowTakenImage);
                    mCardShowTakenPictureViewBinding.cardShowTakenPictureImageListRecyclerView.smoothScrollToPosition(mCardShowTakenPictureViewImagesAdapter.getItemCount() - 1);
                }

                @Override
                public void onError() {

                }
            });

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

    private void generateCardShowTakenImageFromCamera(File photoTaken, Activity activity, CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        if (photoTaken == null) {
            return;
        }

        new Compressor(mContext)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .compressToFileAsFlowable(photoTaken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        compressedImage = file;

                        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                        String tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath, activity);

                        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, photoTaken.getName(), tempImagePathToShow);
                    }
                }, Throwable::printStackTrace);


    }


    private void generateCardShowTakenImageFromImageGallery(File photoTaken, Intent data, Activity activity, CardShowTakenCompressedCallback cardShowTakenCompressedCallback) {
        try {
            mPhotoTaken = FileUtil.from(mContext, data.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Compressor(mContext)
                .setDestinationDirectoryPath(FileUtil.getFile().getAbsolutePath())
                .compressToFileAsFlowable(mPhotoTaken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        compressedImage = file;

                        new Compressor(mContext)
                                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                                .compressToFileAsFlowable(compressedImage)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())

                                .subscribe(new Consumer<File>() {
                                    @Override
                                    public void accept(File file) {
                                        compressedImage = file;

                                        Bitmap bitmapImageFromIntentPath = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                                        String tempImagePathToShow = createTempImageFileToShow(bitmapImageFromIntentPath, activity);

                                        cardShowTakenCompressedCallback.onSuccess(bitmapImageFromIntentPath, mPhotoTaken.getName(), tempImagePathToShow);
                                    }
                                }, Throwable::printStackTrace);

                    }
                }, Throwable::printStackTrace);

    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String createTempImageFileToShow(Bitmap bitmap, Activity activity) {
        String indexTempImage = mCardShowTakenPictureViewImagesAdapter.getItemCount() + 1 + "";

        return MediaStore.Images.Media.insertImage(activity.getContentResolver(),
                bitmap, TEMP_IMAGE_BASE_NAME + indexTempImage, null);
    }

    public boolean hasUpdatedAt() {
        return mCardShowTakenPictureViewBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mCardShowTakenPictureViewBinding.getPictureByName() != null;
    }

}
