package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.app.Activity;
import android.app.Dialog;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract.CardShowTakenCompressedCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoPreviewDialogBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.DialogLoader;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.OrientationListener;
import io.fotoapparat.result.PhotoResult;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_CAMERA_LIST;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup.getLensPosition;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator.fromGallery;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_PREFIX;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.JPG_FILE_SUFFIX;
import static com.annimon.stream.Optional.ofNullable;

public class CameraFragment extends Fragment implements CameraContract {

    private static Integer mPhotosLimit;
    private static Integer mImageListSize;
    private static final int REQUEST_CAMERA_PERMISSION        = 200;
    public static final int REQUEST_IMAGE_LIST_GALLERY_RESULT = 1;
    private static Integer CAMERA_IMAGES_QUANTITY_LIMIT       = 10;
    public static final String BUNDLE_PHOTOS                  = "photos";
    private static ArrayList<CameraPhoto> mCameraPhotos;
    private static boolean mIsMultipleGallerySelection = false;

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath;
    private ImageButton mButtonCapture;
    private CameraSetup mCameraSetup;
    private ImageView mButtonReturnPhotos;
    private ImageView mButtonClose;
    private RecyclerView mPhotosRecyclerView;
    private LinearLayout mNavigationCamera;
    private ImageView mButtonOpenGallery;
    private ImageGenerator mImageGenerator;
    private DialogLoader mDialogLoader;
    private CameraPhotoPreviewDialogBinding mCameraPhotoPreviewDialogBinding;
    private Dialog mPreviewPicDialog;
    private OrientationListener mOrientationListener;

    public static CameraFragment newInstance(Integer limitOfImages, Integer imageListSize, Boolean isMultipleGallerySelection, Bundle bundlePhotos) {
        mPhotosLimit   = limitOfImages;
        mImageListSize = imageListSize;
        Integer remainingImages = limitOfImages - imageListSize;
        if( isMultipleGallerySelection != null ) {
            mIsMultipleGallerySelection = isMultipleGallerySelection;
        }

        if (remainingImages < CAMERA_IMAGES_QUANTITY_LIMIT && isHasNotLimitOfImages(limitOfImages)) {
            CAMERA_IMAGES_QUANTITY_LIMIT = remainingImages;
        }

        if (bundlePhotos != null) {
            mCameraPhotos = (ArrayList<CameraPhoto>) bundlePhotos.getSerializable(BUNDLE_PHOTOS);
        }

        return new CameraFragment();
    }

    private static boolean isHasNotLimitOfImages(Integer limitOfImages) {
        return limitOfImages != -1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPath = ImageViewFileUtil.getPrivateTempDirectory(getContext());

        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setupDialog();

        mDialogLoader        = new DialogLoader(getContext());
        mImageGenerator      = new ImageGenerator(getContext());
        mCameraPhotosAdapter = new CameraPhotosAdapter(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_fragment, container, false);

        setViews();

        if (hasNavigationBar()) {
            setNavigationCameraControlsPadding();
        }

        setClickButtons();
        setAdapter();
        setCameraSetup();
        setOrientation();

        updateCounters();

        return mCameraFragmentBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    private void setViews() {
        mButtonClose        = mCameraFragmentBinding.cameraFragmentCloseImageView;
        mButtonCapture      = mCameraFragmentBinding.cameraFragmentCaptureImageButton;
        mButtonReturnPhotos = mCameraFragmentBinding.cameraFragmentSaveImageView;
        mButtonOpenGallery  = mCameraFragmentBinding.cameraFragmentGalleryImageView;
        mPhotosRecyclerView = mCameraFragmentBinding.cameraPhotosRecyclerView;
        mNavigationCamera   = mCameraFragmentBinding.cameraFragmentBottomLinearLayout;
    }

    private void setOrientation(){
        mOrientationListener = new OrientationListener(getContext(), mButtonClose, mButtonCapture,
                mButtonReturnPhotos, mButtonOpenGallery,
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView,
                mCameraFragmentBinding.cameraFragmentChipLinearLayout,
                mCameraFragmentBinding.cameraFragmentSwitchFlashImageView) {
            @Override
            public void onSimpleOrientationChanged(int orientation) {
                setOrientationView(orientation);
            }
        };
    }

    private void setClickButtons() {
        mButtonClose.setOnClickListener(view -> closeCamera());
        mButtonCapture.setOnClickListener(view -> {
            if(cameraImagesQuantityIsNotOnLimit()) {
                showToastWithCameraLimitQuantity();
            } else {
                takePicture();
            }
        });
        mButtonOpenGallery.setOnClickListener(view -> {
            if (cameraImagesQuantityIsNotOnLimit()) {
                showToastWithCameraLimitQuantity();
            } else {
                openGallery();
            }
        });
        mButtonReturnPhotos.setOnClickListener(view -> {
            if (isOverLimit()) {
                Toast.makeText(getContext(), getString(R.string.camera_photo_reached_limit), Toast.LENGTH_SHORT).show();
            } else {
                returnImagesToCardShowTakenPicturesView();
            }
        });
    }

    private void setAdapter() {
        mPhotosRecyclerView.setNestedScrollingEnabled(true);
        mPhotosRecyclerView.setFocusable(false);
        mPhotosRecyclerView.setAdapter(mCameraPhotosAdapter);

        if (mCameraPhotos != null) {
            mCameraPhotosAdapter.addAllPhotos(mCameraPhotos);
        }
    }

    private void setCameraSetup() {
        mCameraSetup = new CameraSetup(getContext(),
                mCameraFragmentBinding.cameraFragmentView,
                mCameraFragmentBinding.cameraFragmentFocusView);

        mCameraSetup.toggleTorchOnSwitch(mCameraFragmentBinding.cameraFragmentSwitchFlashImageView);
        mCameraSetup.zoomSeekBar(mCameraFragmentBinding.cameraFragmentZoomSeekBar);
        mCameraSetup.switchCameraOnClick(
                mCameraFragmentBinding.cameraFragmentSwitchLensImageView,
                mCameraFragmentBinding.cameraFragmentSwitchFlashImageView);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        if(mIsMultipleGallerySelection) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.gallery_select_pictures)), REQUEST_IMAGE_LIST_GALLERY_RESULT);
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

            Bundle bundle = new Bundle();
            bundle.putSerializable(BUNDLE_PHOTOS, (Serializable) mCameraPhotosAdapter.getList());

            FragmentTransaction tr = getFragmentManager().beginTransaction();
            tr.replace(R.id.camera_content_frame, CameraFragment.newInstance(mPhotosLimit, mImageListSize,
                    mIsMultipleGallerySelection, bundle));
            tr.commit();
        }
    }

    private void generateImageCallback(Uri imageUri) {
        mImageGenerator.generateCardShowTakenImageFromImageGallery(imageUri, fromGallery,
                new CardShowTakenCompressedCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                        CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                        mCameraPhotosAdapter.addPicture(cameraPhoto);
                        mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount() - 1);
                    }

                    @Override
                    public void onError() {}
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getString(R.string.camera_no_permission), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void closeCamera() {
        getActivity().finish();

        mCameraPhotos = null;
    }

    @Override
    public void setPhotos(ArrayList<CameraPhoto> photos) {
        if (photos != null) {
            mCameraPhotosAdapter.addAllPhotos(photos);
        }
    }

    public int getCurrentImagesQuantity() {
        return mCameraPhotosAdapter.getItemCount();
    }

    private void showToastWithCameraLimitQuantity() {
        Toast.makeText(getContext(), String.format(getString(R.string.card_show_taken_picture_view_camera_quantity_limit), CAMERA_IMAGES_QUANTITY_LIMIT), Toast.LENGTH_SHORT).show();
    }

    private boolean cameraImagesQuantityIsNotOnLimit() {
        return getCurrentImagesQuantity() >= CAMERA_IMAGES_QUANTITY_LIMIT;
    }

    @Override
    public void takePicture() {
        PhotoResult photoResult = mCameraSetup.getFotoapparat().takePicture();
        String uuid             = UUID.randomUUID().toString();
        File photoPath          = new File(mPath.toString() + "/" + JPG_FILE_PREFIX + uuid + JPG_FILE_SUFFIX);

        mDialogLoader.showLocalLoader();

        photoResult.saveToFile(photoPath);

        photoResult.toBitmap().whenDone(
                bitmapPhotoResult -> ofNullable(bitmapPhotoResult).ifPresent(
                        (bitmapPhoto) ->
                                mImageGenerator.generateCardShowTakenImageFromCamera(bitmapPhoto.bitmap, getLensPosition(),
                                        bitmapPhoto.rotationDegrees,
                                        new CardShowTakenCompressedCallback() {
                                            @Override
                                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                                CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                                                mCameraPhotosAdapter.addPicture(cameraPhoto);
                                                mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount() - 1);

                                                photoPath.delete();

                                                updateCounters();

                                                mDialogLoader.hideLocalLoader();
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                })
                )
        );

    }

    @Override
    public void returnImagesToCardShowTakenPicturesView() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(KEY_IMAGE_CAMERA_LIST, (Serializable) mCameraPhotosAdapter.getList());
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();

        mCameraPhotos = null;
    }

    @Override
    public void showPreviewPicDialog(CameraPhoto cameraPhoto) {
        getBitmapFromFile(cameraPhoto.getTempImagePathToShow(), 1,
                (bitmap) -> {
                    mCameraPhotoPreviewDialogBinding.previewImageView.setImageBitmap(bitmap);
                    mPreviewPicDialog.show();
                }
        );
    }

    @Override
    public void closePreviewPicDialog(View View) {
        mPreviewPicDialog.cancel();
    }

    private void setupDialog() {
        mPreviewPicDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mCameraPhotoPreviewDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.camera_photo_preview_dialog, null, false);
        mCameraPhotoPreviewDialogBinding.setHandler(this);
        mPreviewPicDialog.setContentView(mCameraPhotoPreviewDialogBinding.getRoot());
    }

    private int getColor(int color) {
        return getResources().getColor(color);
    }

    private boolean isOverLimit() {
        if (mPhotosLimit == -1) {
            return false;
        }

        return (mImageListSize + getCurrentImagesQuantity()) > mPhotosLimit;
    }

    public void setNavigationCameraControlsPadding() {
        mNavigationCamera.setPadding(
                mNavigationCamera.getPaddingLeft(),
                mNavigationCamera.getPaddingTop(),
                mNavigationCamera.getPaddingRight(),
                mNavigationCamera.getBottom() + convertDpToPixels(60));
    }

    public boolean hasNavigationBar() {
        int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && getResources().getBoolean(id);
    }

    public void updateCounters() {
        if (cameraImagesQuantityIsNotOnLimit()) {
            setDesignPhotoLimitIsTrue();
        } else {
            setDesignPhotoLimitIsFalse();
        }

        getActivity().runOnUiThread(() -> {
            mCameraFragmentBinding.cameraFragmentCurrentValue.setText(String.valueOf(getCurrentImagesQuantity()));
            mCameraFragmentBinding.cameraFragmentLimitValue.setText(String.valueOf(CAMERA_IMAGES_QUANTITY_LIMIT));
        });
    }

    private void setDesignPhotoLimitIsTrue() {
        mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(R.color.white));
        mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(R.color.white));
        mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(R.color.white));
        mCameraFragmentBinding.cameraFragmentChipLinearLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_red));
    }

    private void setDesignPhotoLimitIsFalse() {
        mCameraFragmentBinding.cameraFragmentCurrentValue.setTextColor(getColor(R.color.black));
        mCameraFragmentBinding.cameraFragmentLimitValue.setTextColor(getColor(R.color.black));
        mCameraFragmentBinding.cameraFragmentChipDivisorTextView.setTextColor(getColor(R.color.black));
        mCameraFragmentBinding.cameraFragmentChipLinearLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_chip));
    }

    private int convertDpToPixels(int dpValue) {
        float roundingValue = 0.5f;
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + roundingValue);
    }
}
