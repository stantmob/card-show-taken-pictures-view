package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import static android.view.View.INVISIBLE;
import static androidx.core.content.ContextCompat.getDrawable;
import static com.annimon.stream.Optional.ofNullable;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_CAMERA_LIST;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IS_CAPTION_ENABLED;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants.SaveMode.SAVE_ONLY_MODE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants.SaveMode.STANT_MODE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator.fromGallery;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_PREFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_SUFFIX;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract.CardShowTakenCompressedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.OnCaptionSavedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoPreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants.SaveMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.AppPermissions;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.BitmapFromFileCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.OrientationListener;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VerticalSeekBar;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropTouchHelper;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.adapter.rxjava2.SingleAdapter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements CameraContract {

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotoPreviewDialogBinding mCameraPhotoPreviewDialogBinding;
    private Snackbar mSaveOnlySnackbar;
    private Snackbar mStantSaveModeSnackbar;
    private ImageButton mCaptureImageButton;

    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath;
    private CameraSetup mCameraSetup;
    private Fotoapparat mFotoapparat;
    private ImageGenerator mImageGenerator;
    private Dialog mPreviewPicDialog;
    private OrientationListener mOrientationListener;

    private Integer mPhotosLimit;
    private Integer mImageListSize;
    private Boolean mIsMultipleGallerySelection;
    private Integer mImagesQuantityLimit;
    private SaveMode mSaveMode;
    private SaveOnlyMode mSaveOnlyMode;
    private Boolean mDragAndDropMode;

    private Context mContext;

    private Toast mCameraLimitQuantityToast;
    private Toast mDisabledCameraButtonToast;
    private Toast mSaveInProgressHasBeenCanceledToast;

    private final static String KEY_PHOTOS_LIMIT = "photos_limit";
    private final static String KEY_IMAGE_LIST_SIZE = "image_list_size";
    private final static String KEY_MULTIPLE_GALLERY_SELECTION = "multiple_gallery_selection";
    private final static String KEY_SAVE_ONLY_MODE = "save_only_mode";
    private final static String KEY_DRAG_AND_DROP_MODE = "drag_and_drop_mode";

    final static Integer REQUEST_IMAGE_LIST_GALLERY_RESULT = 1;
    private final static Integer REQUEST_CAMERA_PERMISSION = 200;

    private OnCaptionSavedCallback mOnCaptionSavedCallback;
    private Integer mPhotoPosition;
    private Boolean mIsCaptionEnabled = false;

    private String currentTagText = "";

    public static CameraFragment newInstance(Integer limitOfImages,
                                             Integer imageListSize,
                                             Boolean isMultipleGallerySelection,
                                             SaveOnlyMode saveOnlyMode,
                                             Boolean dragAndDropMode,
                                             Boolean isCaptionEnabled) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle arguments = new Bundle();

        arguments.putInt(KEY_PHOTOS_LIMIT, limitOfImages);
        arguments.putInt(KEY_IMAGE_LIST_SIZE, imageListSize);
        arguments.putBoolean(KEY_MULTIPLE_GALLERY_SELECTION, isMultipleGallerySelection);
        arguments.putParcelable(KEY_SAVE_ONLY_MODE, saveOnlyMode);
        arguments.putBoolean(KEY_DRAG_AND_DROP_MODE, dragAndDropMode);
        arguments.putBoolean(KEY_IS_CAPTION_ENABLED, isCaptionEnabled);

        cameraFragment.setArguments(arguments);

        return cameraFragment;
    }

    public CameraFragment() {
        mPhotosLimit = -1;
        mImageListSize = 0;
        mIsMultipleGallerySelection = false;
        mImagesQuantityLimit = 10;
        mSaveMode = new SaveMode(STANT_MODE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ofNullable(getActivity()).ifPresent((activity) ->
                ofNullable(activity.getWindow()).ifPresent((window) ->
                        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
                )
        );

        ofNullable(getArguments()).ifPresent(this::unWrapArguments);

        ofNullable(getActivity()).ifPresentOrElse(
                (activity) -> mContext = activity.getApplicationContext(),
                () -> mContext = getContext()
        );

        ofNullable(mContext).ifPresent(
                (context) -> {
                    setupDialog(getContext());

                    mPath = ImageViewFileUtil.getPrivateTempDirectory(context);
                    mImageGenerator = new ImageGenerator(context);
                    mCameraPhotosAdapter = new CameraPhotosAdapter(context, this);
                }
        );
    }

    private void unWrapArguments(Bundle arguments) {
        Integer limitOfImages = arguments.getInt(KEY_PHOTOS_LIMIT);
        int imageListSize = arguments.getInt(KEY_IMAGE_LIST_SIZE);
        boolean isMultipleGallerySelection = arguments.getBoolean(KEY_MULTIPLE_GALLERY_SELECTION);
        SaveOnlyMode saveOnlyMode = arguments.getParcelable(KEY_SAVE_ONLY_MODE);
        boolean dragAndDropMode = arguments.getBoolean(KEY_DRAG_AND_DROP_MODE);
        boolean isCaptionEnabled = arguments.getBoolean(KEY_IS_CAPTION_ENABLED);

        mPhotosLimit = limitOfImages;
        mImageListSize = imageListSize;
        mIsMultipleGallerySelection = isMultipleGallerySelection;
        mSaveOnlyMode = saveOnlyMode;
        mDragAndDropMode = dragAndDropMode;
        mIsCaptionEnabled = isCaptionEnabled;

        int remainingImages = mPhotosLimit - mImageListSize;

        if (remainingImages < mImagesQuantityLimit && isHasNotLimitOfImages(limitOfImages)) {
            mImagesQuantityLimit = remainingImages;
        }
    }

    private boolean isHasNotLimitOfImages(Integer limitOfImages) {
        return limitOfImages != -1;
    }

    private void setupDialog(Context context) {
        mPreviewPicDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        mCameraPhotoPreviewDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.camera_photo_preview_dialog, null, false);

        if (mIsCaptionEnabled) {
            mCameraPhotoPreviewDialogBinding.cameraPhotoPreviewDialogCaptionContainer.setVisibility(View.VISIBLE);
        } else {
            mCameraPhotoPreviewDialogBinding.cameraPhotoPreviewDialogCaptionContainer.setVisibility(View.GONE);
        }

        mCameraPhotoPreviewDialogBinding.setHandler(this);
        mPreviewPicDialog.setContentView(mCameraPhotoPreviewDialogBinding.getRoot());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_fragment, container, false);

        if (hasNavigationBar()) {
            setNavigationCameraControlsPadding();
        }

        return mCameraFragmentBinding.getRoot();
    }

    private Boolean hasNavigationBar() {
        int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && getResources().getBoolean(id);
    }

    private void setNavigationCameraControlsPadding() {
        ofNullable(mCameraFragmentBinding.cameraFragmentBottomLinearLayout).executeIfPresent((navigationCamera) ->
                navigationCamera.setPadding(
                        navigationCamera.getPaddingLeft(),
                        navigationCamera.getPaddingTop(),
                        navigationCamera.getPaddingRight(),
                        navigationCamera.getPaddingBottom() + convertDpToPixels(48))
        );
    }

    @SuppressWarnings("SameParameterValue")
    private Integer convertDpToPixels(Integer dpValue) {
        float roundingValue = 0.5f;
        float scale = 1f;

        if (mContext != null && mContext.getResources() != null) {
            scale = mContext.getResources().getDisplayMetrics().density;
        }

        return (int) (dpValue * scale + roundingValue);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSaveModeSnackbars(mCameraFragmentBinding.getRoot());

        mCaptureImageButton = mCameraFragmentBinding.cameraFragmentCaptureImageButton;

        setButtonsClick(mCameraFragmentBinding.cameraFragmentCloseImageView,
                mCameraFragmentBinding.cameraFragmentChangeSavePicturesMode,
                mCameraFragmentBinding.cameraFragmentGalleryImageView,
                mCameraFragmentBinding.cameraFragmentSaveImageView);

        final RecyclerView cameraPhotosRecyclerView = mCameraFragmentBinding.cameraPhotosRecyclerView;

        configureRecyclerView(cameraPhotosRecyclerView);

        cameraPhotosRecyclerView.setAdapter(mCameraPhotosAdapter);

        attachDragAndDropTouchHelper(cameraPhotosRecyclerView);

        setCameraSetup(mCameraFragmentBinding.cameraFragmentSwitchFlashImageView,
                mCameraFragmentBinding.cameraFragmentZoomSeekBar,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView,
                mCameraFragmentBinding.cameraFragmentImageDataView);

        configureOrientationListener(mCameraFragmentBinding.cameraFragmentCloseImageView,
                mCameraFragmentBinding.cameraFragmentChangeSavePicturesMode,
                mCaptureImageButton,
                mCameraFragmentBinding.cameraFragmentGalleryImageView,
                mCameraFragmentBinding.cameraFragmentSaveImageView,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView,
                mCameraFragmentBinding.cameraFragmentChipLinearLayout,
                mCameraFragmentBinding.cameraFragmentSwitchFlashImageView);

        updateCounters();

        onGeolocationStarted();
    }

    private void setupSaveModeSnackbars(View root) {
        ofNullable(mSaveOnlyMode).executeIfPresent(
                (saveOnlyMode) -> {
                    mSaveOnlySnackbar = Snackbar.make(root,
                            saveOnlyMode.getEnabledWarning(),
                            Snackbar.LENGTH_SHORT);
                    mStantSaveModeSnackbar = Snackbar.make(root,
                            saveOnlyMode.getDisabledWarning(),
                            Snackbar.LENGTH_SHORT);
                }
        );
    }

    private void setButtonsClick(ImageView closeButton,
                                 ImageView changeSavePicturesMode,
                                 ImageView openGalleryButton,
                                 ImageView savePhotosButton) {
        closeButton.setOnClickListener((view) -> closeCamera());

        setChangeSavePicturesModeOnClickListener(changeSavePicturesMode, mSaveOnlyMode);

        enableCaptureButton();

        openGalleryButton.setOnClickListener((view) -> {
            if (cameraImagesQuantityIsOverLimit()) {
                showCameraLimitQuantityToast();
            } else {
                openGallery();
            }
        });

        savePhotosButton.setOnClickListener((view) -> {
            if (isOverLimit()) {
                Toast.makeText(mContext, mContext.getString(R.string.camera_photo_reached_limit), Toast.LENGTH_SHORT).show();
            } else {
                returnImagesToCardShowTakenPicturesView();
            }
        });
    }

    private void enableCaptureButton() {
        mCaptureImageButton.setOnClickListener((view) -> {
            if (cameraImagesQuantityIsOverLimit()) {
                showCameraLimitQuantityToast();
            } else {
                takePicture();
            }
        });
    }

    private void disableCaptureButton() {
        mCaptureImageButton.setOnClickListener(
                (view) -> showDisabledCameraButtonToast()
        );
    }

    private void showCameraLimitQuantityToast() {
        final String cameraQuantityLimitMessage = getCameraQuantityLimitMessage(mImagesQuantityLimit);

        ofNullable(mCameraLimitQuantityToast).ifPresent(Toast::cancel);

        mCameraLimitQuantityToast = createAndShowErrorToast(cameraQuantityLimitMessage);
    }

    private String getCameraQuantityLimitMessage(Integer imagesQuantityLimit) {
        return String.format(mContext.getString(R.string.card_show_taken_picture_view_camera_quantity_limit), imagesQuantityLimit);
    }

    private void showDisabledCameraButtonToast() {
        final String waitUntilThePhotoSaveProcessFinishesMessage = getWaitUntilThePhotoSaveProcessFinishesMessage();

        ofNullable(mDisabledCameraButtonToast).ifPresent(Toast::cancel);

        mDisabledCameraButtonToast = createAndShowErrorToast(waitUntilThePhotoSaveProcessFinishesMessage);
    }

    private String getWaitUntilThePhotoSaveProcessFinishesMessage() {
        return mContext.getString(R.string.camera_fragment_wait_until_the_photo_save_process_finishes);
    }

    private void setChangeSavePicturesModeOnClickListener(ImageView changeSavePicturesMode, SaveOnlyMode saveOnlyMode) {
        if (saveOnlyMode != null) {
            changeSavePicturesMode.setImageBitmap(mSaveOnlyMode.getDisabledIcon());
            changeSavePicturesMode.setOnClickListener(
                    (view) -> {
                        if (mSaveMode.getMode().equalsIgnoreCase(STANT_MODE)) {
                            changeSavePicturesMode.setImageBitmap(mSaveOnlyMode.getEnabledIcon());
                            showSaveOnlySnackBar();
                            mSaveMode = new SaveMode(SAVE_ONLY_MODE);
                        } else {
                            changeSavePicturesMode.setImageBitmap(mSaveOnlyMode.getDisabledIcon());
                            showStantSaveModeSnackBar();
                            mSaveMode = new SaveMode(STANT_MODE);
                        }
                    }
            );
        } else {
            changeSavePicturesMode.setVisibility(INVISIBLE);
            changeSavePicturesMode.setOnClickListener(null);
        }
    }

    private void showSaveOnlySnackBar() {
        mSaveOnlySnackbar.show();
        mStantSaveModeSnackbar.dismiss();
    }

    private void showStantSaveModeSnackBar() {
        mSaveOnlySnackbar.dismiss();
        mStantSaveModeSnackbar.show();
    }

    private boolean cameraImagesQuantityIsOverLimit() {
        return getCurrentImagesQuantity() >= mImagesQuantityLimit;
    }

    private Integer getCurrentImagesQuantity() {
        if (mCameraPhotosAdapter != null) {
            return mCameraPhotosAdapter.getItemCount();
        } else {
            return 0;
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        if (mIsMultipleGallerySelection) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                        mContext.getString(R.string.gallery_select_pictures)),
                REQUEST_IMAGE_LIST_GALLERY_RESULT);
    }

    private Boolean isOverLimit() {
        if (mPhotosLimit == -1) {
            return false;
        } else {
            return (mImageListSize + getCurrentImagesQuantity()) > mPhotosLimit;
        }
    }

    private void configureRecyclerView(RecyclerView cameraPhotosRecyclerView) {
        cameraPhotosRecyclerView.setNestedScrollingEnabled(true);
        cameraPhotosRecyclerView.setFocusable(false);
        cameraPhotosRecyclerView.setHasFixedSize(true);
    }

    private void attachDragAndDropTouchHelper(RecyclerView cameraPhotosRecyclerView) {
        DragAndDropTouchHelper dragAndDropTouchHelper = new DragAndDropTouchHelper(mCameraPhotosAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragAndDropTouchHelper);

        mCameraPhotosAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(cameraPhotosRecyclerView);
    }

    private void setCameraSetup(ImageView flashImageView,
                                VerticalSeekBar zoomSeekBar,
                                ImageView switchCameraImageView,
                                ImageView dataInfoImageView) {
        Activity activity = getActivity();

        if (activity != null) {
            mCameraSetup = newCameraSetup(activity.getApplicationContext(), mCameraFragmentBinding);
        } else {
            mCameraSetup = newCameraSetup(mContext, mCameraFragmentBinding);
        }

        configureCameraSetup(mCameraSetup, flashImageView, zoomSeekBar, switchCameraImageView, dataInfoImageView);
        mFotoapparat = mCameraSetup.getFotoapparat();
        mCameraFragmentBinding.imageDateTextView.setFormat24Hour(mContext.getString(R.string.camera_fragment_date_time_format));
    }

    private CameraSetup newCameraSetup(Context context, CameraFragmentBinding cameraFragmentBinding) {
        return new CameraSetup(context,
                cameraFragmentBinding.cameraFragmentView,
                cameraFragmentBinding.cameraFragmentFocusView);
    }

    private void configureCameraSetup(CameraSetup cameraSetup,
                                      ImageView flashImageView,
                                      VerticalSeekBar zoomSeekBar,
                                      ImageView switchCameraImageView,
                                      ImageView dataInfoImageView) {
        cameraSetup.toggleTorchOnSwitch(flashImageView);
        cameraSetup.toggleDataInfoOnSwitch(dataInfoImageView,
                mCameraFragmentBinding.cameraFragmentDataInfoLayout);
        cameraSetup.zoomSeekBar(zoomSeekBar);
        cameraSetup.switchCameraOnClick(switchCameraImageView, flashImageView);
    }

    private void configureOrientationListener(ImageView closeButton, ImageView changeSavePicturesReason,
                                              ImageButton captureButton, ImageView openGalleryButton,
                                              ImageView savePhotosButton, ImageView switchCameraImageView,
                                              LinearLayout chipLinearLayout, ImageView flashImageView) {
        mOrientationListener = new OrientationListener(mContext, closeButton, changeSavePicturesReason, captureButton,
                savePhotosButton, openGalleryButton, switchCameraImageView, chipLinearLayout, flashImageView) {
            @Override
            public void onSimpleOrientationChanged(int orientation) {
                setOrientationView(orientation);
            }
        };
    }

    public void updateImageDataTextView(String value) {
        this.currentTagText = mCameraFragmentBinding.imageDateTextView.getText() + "\n" + value;
        mCameraFragmentBinding.imageLocalTextView.setText(value);
    }

    public void updateCounters() {
        if (cameraImagesQuantityIsOverLimit()) {
            setDesignPhotoLimitIsTrue();
        } else {
            setDesignPhotoLimitIsFalse();
        }

        ofNullable(getActivity()).ifPresent(
                (activity) -> activity.runOnUiThread(() -> {
                    mCameraFragmentBinding.cameraFragmentCurrentValue.setText(convertIntegerToString(getCurrentImagesQuantity()));
                    mCameraFragmentBinding.cameraFragmentLimitValue.setText(convertIntegerToString(mImagesQuantityLimit));
                })
        );
    }

    private String convertIntegerToString(Integer integer) {
        String integerStringFormat = "%d";
        return String.format(Locale.getDefault(), integerStringFormat, integer);
    }

    private void setDesignPhotoLimitIsTrue() {
        ofNullable(mContext).executeIfPresent((context) -> {
            mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(context, R.color.white));
            mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(context, R.color.white));
            mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(context, R.color.white));
            mCameraFragmentBinding.cameraFragmentChipLinearLayout.setBackground(getDrawable(context, R.drawable.shape_rectangle_red));
        });
    }

    private void setDesignPhotoLimitIsFalse() {
        ofNullable(mContext).executeIfPresent((context) -> {
            mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(context, R.color.black));
            mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(context, R.color.black));
            mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(context, R.color.black));
            mCameraFragmentBinding.cameraFragmentChipLinearLayout.setBackground(getDrawable(context, R.drawable.shape_rectangle_chip));
        });
    }

    private Integer getColor(Context context, Integer colorId) {
        if (context != null) {
            return ContextCompat.getColor(context, colorId);
        } else {
            return 0;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_LIST_GALLERY_RESULT && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    if (imageUri.getLastPathSegment() != null) {
                        generateImageCallback(imageUri);
                    }
                } else if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();

                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();

                        generateImageCallback(imageUri);
                    }
                }
            }

            updateCounters();
        }
    }

    private void generateImageCallback(Uri imageUri) {
        mImageGenerator.generateCardShowTakenImageFromImageGallery(imageUri, fromGallery,
                new CardShowTakenCompressedCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                        CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                        mCameraPhotosAdapter.addPicture(cameraPhoto, (position) -> mCameraPhotosAdapter.notifyItemInserted(position));
                        mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount());
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onResume() {
        super.onResume();
        ofNullable(getActivity()).ifPresent((activity) ->
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        mOrientationListener.enable();
    }

    @Override
    public void onPause() {
        super.onPause();
        mOrientationListener.disable();
    }

    @Override
    public void onStart() {
        super.onStart();
        startFotoapparat();
    }

    private void startFotoapparat() {
        Handler handler = new Handler();
        handler.postDelayed(() -> mFotoapparat.start(), 60);
    }

    @Override
    public void onStop() {
        super.onStop();
        mFotoapparat.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOrientationListener.disable();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ofNullable(mContext).ifPresent((context) ->
                        Toast.makeText(context, context.getString(R.string.camera_no_permission), Toast.LENGTH_SHORT).show()
                );
                ofNullable(getActivity()).ifPresent(Activity::finish);
            }
        }
    }

    @Override
    public void closeCamera() {
        ofNullable(getActivity()).ifPresent(Activity::finish);
    }

    @Override
    public void setPhotos(ArrayList<CameraPhoto> photos) {
        if (photos != null) {
            mCameraPhotosAdapter.addAllPhotos(photos);
        }
    }

    @Override
    public void takePicture() {
        PhotoResult photoResult = mFotoapparat.takePicture();
        String uuid = UUID.randomUUID().toString();
        File photoPath = new File(mPath.toString() + "/" + JPG_FILE_PREFIX + uuid + JPG_FILE_SUFFIX);

        disableCaptureButton();

        if (saveModeOnlyNotSelected()) {
            showSaveImageLoader();
        }

        unwrapPhotoResultAndSaveBitmap(photoResult, photoPath);
    }

    private boolean saveModeOnlyNotSelected() {
        return !mSaveMode.getMode().equalsIgnoreCase(SAVE_ONLY_MODE);
    }

    private void showSaveImageLoader() {
        mCameraPhotosAdapter.showLoader(position -> mCameraPhotosAdapter.notifyItemInserted(position));
        mCameraFragmentBinding.cameraPhotosRecyclerView
                .smoothScrollToPosition(mCameraPhotosAdapter.getItemCount());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    private void unwrapPhotoResultAndSaveBitmap(PhotoResult photoResult, File photoPath) {
        photoResult.toBitmap()
                .adapt(SingleAdapter.toSingle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (bitmapPhoto) -> {
                            Bitmap bitmap = bitmapPhoto.bitmap;
                            Integer rotationDegrees = bitmapPhoto.rotationDegrees;

                            if (saveModeOnlySelected()) {
                                String tagText = mCameraSetup.mIsDataInfoChecked ? this.currentTagText : "";
                                mImageGenerator.scaleAndSaveInPictures(
                                        bitmap,
                                        rotationDegrees,
                                        UUID.randomUUID().toString(),
                                        tagText);
                                enableCaptureButton();
                            } else {
                                generateCardShowTakenImageFromCamera(bitmap, rotationDegrees, photoPath);
                            }
                        }, (throwable) -> {
                            showSaveInProgressHasBeenCanceledToast();
                            hideSaveImageLoaderOnFailed();
                            enableCaptureButton();
                        }
                );
    }

    private boolean saveModeOnlySelected() {
        return mSaveMode.getMode().equalsIgnoreCase(SAVE_ONLY_MODE);
    }

    private void generateCardShowTakenImageFromCamera(Bitmap bitmap, Integer rotationDegrees, File photoPath) {
        String tagText = mCameraSetup.mIsDataInfoChecked ? this.currentTagText : "";
        mImageGenerator.generateCardShowTakenImageFromCamera(
                bitmap,
                mCameraSetup.getLensPosition(),
                rotationDegrees,
                tagText,
                new CardShowTakenCompressedCallback() {

                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    @Override
                    public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                        CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                        hideSaveImageLoaderOnSuccess(cameraPhoto);

                        photoPath.delete();

                        updateCounters();
                        enableCaptureButton();
                    }

                    @Override
                    public void onError(String message) {
                        enableCaptureButton();
                        hideSaveImageLoaderOnFailed();
                    }
                }
        );
    }

    private void hideSaveImageLoaderOnSuccess(CameraPhoto cameraPhoto) {
        mCameraPhotosAdapter.hideLoader();
        mCameraPhotosAdapter.addPicture(cameraPhoto,
                (position) -> mCameraPhotosAdapter.notifyItemChanged(position - 1)
        );
        mCameraFragmentBinding.cameraPhotosRecyclerView
                .smoothScrollToPosition(mCameraPhotosAdapter.getItemCount());
    }

    private void showSaveInProgressHasBeenCanceledToast() {
        final String waitUntilThePhotoSaveProcessFinishesMessage = getSaveInProgressHasBeenCanceledMessage();

        ofNullable(mSaveInProgressHasBeenCanceledToast).ifPresent(Toast::cancel);

        mSaveInProgressHasBeenCanceledToast = createAndShowErrorToast(waitUntilThePhotoSaveProcessFinishesMessage);
    }

    private Toast createAndShowErrorToast(String message) {
        Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.show();

        return toast;
    }

    private String getSaveInProgressHasBeenCanceledMessage() {
        return mContext.getString(R.string.camera_fragment_save_in_progress_has_been_canceled);
    }

    private void hideSaveImageLoaderOnFailed() {
        mCameraPhotosAdapter.hideLoader();
        mCameraPhotosAdapter.notifyItemRemoved(mCameraPhotosAdapter.getItemCount());
    }

    @Override
    public void returnImagesToCardShowTakenPicturesView() {
        Intent returnIntent = new Intent();

        returnIntent.putExtra(KEY_IMAGE_CAMERA_LIST, (Serializable) mCameraPhotosAdapter.getList());

        ofNullable(getActivity()).ifPresent((activity) -> {
            activity.setResult(Activity.RESULT_OK, returnIntent);
            activity.finish();
        });
    }

    @Override
    public void showPreviewPicDialog(CameraPhoto cameraPhoto, Integer photoPositionOnAdapter, OnCaptionSavedCallback onCaptionSavedCallback) {
        final int sampleSizeForPreviewImages = 1;

        mPhotoPosition = photoPositionOnAdapter;
        mOnCaptionSavedCallback = onCaptionSavedCallback;

        getBitmapFromFile(cameraPhoto.getTempImagePathToShow(), sampleSizeForPreviewImages, new BitmapFromFileCallback() {
            @Override
            public void onBitmapDecoded(Bitmap bitmap) {
                mCameraPhotoPreviewDialogBinding.cameraPhotoPreviewDialogMainImageView.setImageBitmap(bitmap);
                mCameraPhotoPreviewDialogBinding.cameraPhotoPreviewDialogEditCaption.setText(cameraPhoto.getCaption());
                mPreviewPicDialog.show();
            }

            @Override
            public void fileNotFound() {

            }
        });
    }

    @Override
    public void closePreviewPicDialog(View View) {
        mPreviewPicDialog.cancel();
    }

    @Override
    public void saveCaption(View view) {
        String captionText = mCameraPhotoPreviewDialogBinding.cameraPhotoPreviewDialogEditCaption.getText().toString();
        mOnCaptionSavedCallback.onCaptionSaved(captionText, mPhotoPosition);
        mPreviewPicDialog.dismiss();
    }

    public Boolean dragAndDropModeIsEnabled() {
        return mDragAndDropMode;
    }

    /**
     * @suppress MissingPermission is used because the permissions are first checked before use
     */
    @SuppressLint("MissingPermission")
    private void onGeolocationStarted() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (gpsEnabled) {
            if (AppPermissions.hasLocationPermissionsOn(mContext)) {
                Task<Location> locationResult = LocationServices.getFusedLocationProviderClient(requireActivity())
                        .getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null);
                locationResult.addOnSuccessListener(location -> {
                    if (location != null) {
                        setLocation(location);
                    } else {
                        setLocation(forceLocation(locationManager));
                    }
                });
            } else {
                AppPermissions.requestPermissionsFor(requireActivity());
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.geolocation_location_not_enabled_alert_text), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setLocation(Location location) {
        if (location == null) {
            this.updateImageDataTextView(mContext.getString(R.string.geolocation_location_not_available_text));
            return;
        }

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String thoroughfare = address.getThoroughfare() + " " + address.getSubThoroughfare();
                String district = address.getSubLocality();
                String city = address.getSubAdminArea();
                String state = address.getAdminArea();
                this.updateImageDataTextView(
                        currentLatitude
                                + ", "
                                + currentLongitude
                                + "\n"
                                + thoroughfare
                                + "\n"
                                + district
                                + "\n"
                                + city
                                + "\n"
                                + state
                );
            } else {
                this.updateImageDataTextView(mContext.getString(R.string.geolocation_location_not_available_text));
            }
        } catch (IOException e) {
            this.updateImageDataTextView(
                    "Lat. " + location.getLatitude()
                            + "\n"
                            + "Lon. " + location.getLongitude()
            );
        }
    }

    private Location forceLocation(LocationManager locationManager) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(7 * 1000);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, mContext.getString(R.string.permission_information_dialog_message), Toast.LENGTH_LONG).show();
        }
        String bestProvider = locationManager.getBestProvider(new Criteria(), true);
        locationManager.requestLocationUpdates(bestProvider, 0L, 0F, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                setLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                LocationListener.super.onStatusChanged(provider, status, extras);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                LocationListener.super.onProviderEnabled(provider);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                LocationListener.super.onProviderDisabled(provider);
            }
        });
        return locationManager.getLastKnownLocation(bestProvider);
    }
}
