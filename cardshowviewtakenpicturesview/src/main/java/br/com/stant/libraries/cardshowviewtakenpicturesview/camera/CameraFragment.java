package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract.CardShowTakenCompressedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoPreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants.SaveMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.BitmapFromFileCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.DialogLoader;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.OrientationListener;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VerticalSeekBar;
import io.fotoapparat.result.PhotoResult;

import static android.support.v4.content.ContextCompat.getDrawable;
import static android.view.View.INVISIBLE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_CAMERA_LIST;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup.getLensPosition;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants.SaveMode.SAVE_ONLY_MODE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants.SaveMode.STANT_MODE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator.fromGallery;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_PREFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_SUFFIX;
import static com.annimon.stream.Optional.ofNullable;

public class CameraFragment extends Fragment implements CameraContract {

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotoPreviewDialogBinding mCameraPhotoPreviewDialogBinding;
    private Snackbar mSaveOnlySnackbar;
    private Snackbar mStantSaveModeSnackbar;

    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath;
    private CameraSetup mCameraSetup;
    private ImageGenerator mImageGenerator;
    private DialogLoader mDialogLoader;
    private Dialog mPreviewPicDialog;
    private OrientationListener mOrientationListener;

    private Integer mPhotosLimit;
    private Integer mImageListSize;
    private Boolean mIsMultipleGallerySelection;
    private Integer mImagesQuantityLimit;
    private SaveMode mSaveMode;
    private SaveOnlyMode mSaveOnlyMode;

    private final static String KEY_PHOTOS_LIMIT               = "photos_limit";
    private final static String KEY_IMAGE_LIST_SIZE            = "image_list_size";
    private final static String KEY_MULTIPLE_GALLERY_SELECTION = "multiple_gallery_selection";
    private final static String KEY_SAVE_ONLY_MODE             = "save_only_mode";

    final static Integer REQUEST_IMAGE_LIST_GALLERY_RESULT = 1;
    private final static Integer REQUEST_CAMERA_PERMISSION = 200;

    public static CameraFragment newInstance(Integer limitOfImages,
                                             Integer imageListSize,
                                             Boolean isMultipleGallerySelection,
                                             SaveOnlyMode saveOnlyMode) {
        CameraFragment cameraFragment = new CameraFragment();

        Bundle arguments = new Bundle();

        arguments.putInt(KEY_PHOTOS_LIMIT, limitOfImages);
        arguments.putInt(KEY_IMAGE_LIST_SIZE, imageListSize);
        arguments.putBoolean(KEY_MULTIPLE_GALLERY_SELECTION, isMultipleGallerySelection);
        arguments.putParcelable(KEY_SAVE_ONLY_MODE, saveOnlyMode);

        cameraFragment.setArguments(arguments);

        return cameraFragment;
    }

    public CameraFragment() {
        mPhotosLimit                 = -1;
        mImageListSize               = 0;
        mIsMultipleGallerySelection  = false;
        mImagesQuantityLimit         = 10;
        mSaveMode                    = new SaveMode(STANT_MODE);
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

        ofNullable(getContext()).ifPresent(
                (context) -> {
                    setupDialog(context);

                    mPath                = ImageViewFileUtil.getPrivateTempDirectory(context);
                    mDialogLoader        = new DialogLoader(context);
                    mImageGenerator      = new ImageGenerator(context);
                    mCameraPhotosAdapter = new CameraPhotosAdapter(context, this);
                }
        );
    }

    private void unWrapArguments(Bundle arguments) {
        Integer limitOfImages               = arguments.getInt(KEY_PHOTOS_LIMIT);
        Integer imageListSize               = arguments.getInt(KEY_IMAGE_LIST_SIZE);
        Boolean isMultipleGallerySelection  = arguments.getBoolean(KEY_MULTIPLE_GALLERY_SELECTION);
        SaveOnlyMode saveOnlyMode           = arguments.getParcelable(KEY_SAVE_ONLY_MODE);

        mPhotosLimit                = limitOfImages;
        mImageListSize              = imageListSize;
        mIsMultipleGallerySelection = isMultipleGallerySelection;
        mSaveOnlyMode               = saveOnlyMode;

        Integer remainingImages = mPhotosLimit - mImageListSize;

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

    private Integer convertDpToPixels(Integer dpValue) {
        float roundingValue = 0.5f;
        float scale         = 1f;

        if (getContext() != null && getContext().getResources() != null) {
            scale = getContext().getResources().getDisplayMetrics().density;
        }

        return (int)(dpValue * scale + roundingValue);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSaveModeSnackbars(mCameraFragmentBinding.getRoot());

        setButtonsClick(mCameraFragmentBinding.cameraFragmentCloseImageView,
                mCameraFragmentBinding.cameraFragmentChangeSavePicturesMode,
                mCameraFragmentBinding.cameraFragmentCaptureImageButton,
                mCameraFragmentBinding.cameraFragmentGalleryImageView,
                mCameraFragmentBinding.cameraFragmentSaveImageView);

        setAdapter(mCameraFragmentBinding.cameraPhotosRecyclerView);

        setCameraSetup(mCameraFragmentBinding.cameraFragmentSwitchFlashImageView,
                mCameraFragmentBinding.cameraFragmentZoomSeekBar,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView);

        configureOrientationListener(mCameraFragmentBinding.cameraFragmentCloseImageView,
                mCameraFragmentBinding.cameraFragmentChangeSavePicturesMode,
                mCameraFragmentBinding.cameraFragmentCaptureImageButton,
                mCameraFragmentBinding.cameraFragmentGalleryImageView,
                mCameraFragmentBinding.cameraFragmentSaveImageView,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView,
                mCameraFragmentBinding.cameraFragmentChipLinearLayout,
                mCameraFragmentBinding.cameraFragmentSwitchFlashImageView);

        updateCounters();
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
                                 ImageButton captureButton,
                                 ImageView openGalleryButton,
                                 ImageView savePhotosButton) {
        closeButton.setOnClickListener((view) -> closeCamera());

        setChangeSavePicturesModeOnClickListener(changeSavePicturesMode, mSaveOnlyMode);

        captureButton.setOnClickListener((view) -> {
            if (cameraImagesQuantityIsNotOnLimit()) {
                showToastWithCameraLimitQuantity();
            } else {
                takePicture();
            }
        });

        openGalleryButton.setOnClickListener((view) -> {
            if (cameraImagesQuantityIsNotOnLimit()) {
                showToastWithCameraLimitQuantity();
            } else {
                openGallery();
            }
        });

        savePhotosButton.setOnClickListener((view) -> {
            if (isOverLimit()) {
                Toast.makeText(getContext(), getString(R.string.camera_photo_reached_limit), Toast.LENGTH_SHORT).show();
            } else {
                returnImagesToCardShowTakenPicturesView();
            }
        });
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

    private boolean cameraImagesQuantityIsNotOnLimit() {
        return getCurrentImagesQuantity() >= mImagesQuantityLimit;
    }

    private Integer getCurrentImagesQuantity() {
        if (mCameraPhotosAdapter != null) {
            return mCameraPhotosAdapter.getItemCount();
        } else {
            return 0;
        }
    }

    private void showToastWithCameraLimitQuantity() {
        Toast.makeText(getContext(),
                String.format(getString(R.string.card_show_taken_picture_view_camera_quantity_limit),
                        mImagesQuantityLimit), Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        if (mIsMultipleGallerySelection) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.gallery_select_pictures)),
                REQUEST_IMAGE_LIST_GALLERY_RESULT);
    }

    private Boolean isOverLimit() {
        if (mPhotosLimit == -1) {
            return false;
        } else {
            return (mImageListSize + getCurrentImagesQuantity()) > mPhotosLimit;
        }
    }

    private void setAdapter(RecyclerView cameraPhotosRecyclerView) {
        cameraPhotosRecyclerView.setNestedScrollingEnabled(true);
        cameraPhotosRecyclerView.setFocusable(false);
        cameraPhotosRecyclerView.setAdapter(mCameraPhotosAdapter);
    }

    private void setCameraSetup(ImageView flashImageView,
                                VerticalSeekBar zoomSeekBar,
                                ImageView switchCameraImageView) {
        mCameraSetup = new CameraSetup(getContext(),
                mCameraFragmentBinding.cameraFragmentView,
                mCameraFragmentBinding.cameraFragmentFocusView);

        mCameraSetup.toggleTorchOnSwitch(flashImageView);
        mCameraSetup.zoomSeekBar(zoomSeekBar);
        mCameraSetup.switchCameraOnClick(switchCameraImageView, flashImageView);
    }

    private void configureOrientationListener(ImageView closeButton, ImageView changeSavePicturesReason,
                                              ImageButton captureButton, ImageView openGalleryButton,
                                              ImageView savePhotosButton, ImageView switchCameraImageView,
                                              LinearLayout chipLinearLayout, ImageView flashImageView) {
        mOrientationListener = new OrientationListener(getContext(), closeButton, changeSavePicturesReason, captureButton,
                savePhotosButton, openGalleryButton, switchCameraImageView, chipLinearLayout, flashImageView) {
            @Override
            public void onSimpleOrientationChanged(int orientation) {
                setOrientationView(orientation);
            }
        };
    }

    public void updateCounters() {
        if (cameraImagesQuantityIsNotOnLimit()) {
            setDesignPhotoLimitIsTrue();
        } else {
            setDesignPhotoLimitIsFalse();
        }

        ofNullable(getActivity()).ifPresent(
                (activity) -> activity.runOnUiThread(() -> {
                    mCameraFragmentBinding.cameraFragmentCurrentValue.setText(
                            String.format(Locale.getDefault(),"%d", getCurrentImagesQuantity()));
                    mCameraFragmentBinding.cameraFragmentLimitValue.setText(
                            String.format(Locale.getDefault(),"%d", mImagesQuantityLimit));
                })
        );
    }

    private void setDesignPhotoLimitIsTrue() {
        ofNullable(getContext()).executeIfPresent((context) -> {
            mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(context, R.color.white));
            mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(context, R.color.white));
            mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(context, R.color.white));
            mCameraFragmentBinding.cameraFragmentChipLinearLayout.setBackground(getDrawable(context, R.drawable.shape_rectangle_red));
        });
    }

    private void setDesignPhotoLimitIsFalse() {
        ofNullable(getContext()).executeIfPresent((context) -> {
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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_LIST_GALLERY_RESULT && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getData() != null) {
                Uri imageUri = data.getData();

                generateImageCallback(imageUri);
            } else if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();

                    generateImageCallback(imageUri);
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

                        mCameraPhotosAdapter.addPicture(cameraPhoto);
                        mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount());
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

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
        mCameraSetup.getFotoapparat().start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCameraSetup.getFotoapparat().stop();
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
                ofNullable(getContext()).ifPresent((context) ->
                        Toast.makeText(context, getString(R.string.camera_no_permission), Toast.LENGTH_SHORT)
                                .show()
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
        PhotoResult photoResult = mCameraSetup.getFotoapparat().takePicture();
        String uuid             = UUID.randomUUID().toString();
        File photoPath          = new File(mPath.toString() + "/" + JPG_FILE_PREFIX + uuid + JPG_FILE_SUFFIX);

        mDialogLoader.showLocalLoader();

        photoResult.saveToFile(photoPath);

        photoResult.toBitmap().whenDone(
                (bitmapPhoto) -> {
                    Bitmap bitmap = bitmapPhoto.bitmap;
                    Integer rotationDegrees = bitmapPhoto.rotationDegrees;
                    if (mSaveMode.getMode().equalsIgnoreCase(SAVE_ONLY_MODE)) {
                        mImageGenerator.saveInPictures(bitmap, rotationDegrees, UUID.randomUUID().toString());
                        mDialogLoader.hideLocalLoader();
                    } else {
                        mImageGenerator.generateCardShowTakenImageFromCamera(bitmap, getLensPosition(), rotationDegrees,
                                new CardShowTakenCompressedCallback() {

                                    @Override
                                    public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                        CameraPhoto cameraPhoto = new CameraPhoto(imageFilename,
                                                tempImagePath, new Date(), new Date());

                                        mCameraPhotosAdapter.addPicture(cameraPhoto);
                                        mCameraFragmentBinding.cameraPhotosRecyclerView
                                                .smoothScrollToPosition(mCameraPhotosAdapter.getItemCount());

                                        photoPath.delete();

                                        updateCounters();

                                        mDialogLoader.hideLocalLoader();
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }
        }
        );
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
    public void showPreviewPicDialog(CameraPhoto cameraPhoto) {
        getBitmapFromFile(cameraPhoto.getTempImagePathToShow(), 1, new BitmapFromFileCallback() {
            @Override
            public void onBitmapDecoded(Bitmap bitmap) {
                mCameraPhotoPreviewDialogBinding.previewImageView.setImageBitmap(bitmap);
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


}
