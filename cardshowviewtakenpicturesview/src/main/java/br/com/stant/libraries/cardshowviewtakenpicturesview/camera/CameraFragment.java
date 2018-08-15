package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_CAMERA_LIST;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil.JPEG_FILE_SUFFIX;

public class CameraFragment extends Fragment implements CameraContract {

    private static Integer mPhotosLimit;
    private static Integer mImageListSize;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static final int REQUEST_IMAGE_LIST_GALLERY_RESULT = 1;

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath = PhotoViewFileUtil.getFile();
    private ImageButton mButtonCapture;
    private CameraSetup mCameraSetup;
    private ImageView mButtonReturnPhotos;
    private ImageView mButtonClose;
    private RecyclerView mPhotosRecyclerView;
    private LinearLayout mNavigationCamera;
    private ImageView mButtonOpenGallery;
    private ImageGenerator mImageGenerator;
    private ArrayList<String> imagesEncodedList;
    private String imageEncoded;
    private String[] filePathColumn;

    public static CameraFragment newInstance(Integer limitOfImages, Integer imageListSize) {
        mPhotosLimit   = limitOfImages;
        mImageListSize = imageListSize;

        return new CameraFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoViewFileUtil.createTempDirectory(mPath);

        mImageGenerator = new ImageGenerator(getContext(), this);
        mCameraPhotosAdapter = new CameraPhotosAdapter(getContext(), this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_fragment, container, false);

        mButtonClose        = mCameraFragmentBinding.cameraFragmentClose;
        mButtonCapture      = mCameraFragmentBinding.cameraFragmentCaptureImageButton;
        mButtonReturnPhotos = mCameraFragmentBinding.cameraFragmentSaveImageView;
        mButtonOpenGallery  = mCameraFragmentBinding.cameraFragmentGallery;
        mPhotosRecyclerView = mCameraFragmentBinding.cameraPhotosRecyclerView;
        mNavigationCamera   = mCameraFragmentBinding.cameraFragmentBottomLinearLayout;

        if (hasNavigationBar()) {
            setNavigationCameraControlsPadding();
        }

        mButtonClose.setOnClickListener(view -> closeCamera());
        mButtonCapture.setOnClickListener(view -> takePicture());
        mButtonReturnPhotos.setOnClickListener(view -> returnImagesToCardShowTakenPicturesView());
        mButtonOpenGallery.setOnClickListener(view -> openGallery());

        mPhotosRecyclerView.setNestedScrollingEnabled(true);
        mPhotosRecyclerView.setFocusable(false);
        mPhotosRecyclerView.setAdapter(mCameraPhotosAdapter);

        mCameraSetup = new CameraSetup(getContext(),
                mCameraFragmentBinding.cameraFragmentView,
                mCameraFragmentBinding.cameraFragmentFocusView);

        mCameraSetup.toggleTorchOnSwitch(mCameraFragmentBinding.cameraFragmentSwitchFlash);
        mCameraSetup.zoomSeekBar(mCameraFragmentBinding.cameraFragmentZoomSeekBar);
        mCameraSetup.switchCameraOnClick(
                mCameraFragmentBinding.cameraFragmentSwitchLens,
                mCameraFragmentBinding.cameraFragmentSwitchFlash);

        updateCounters();

        return mCameraFragmentBinding.getRoot();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.gallery_select_pictures)), REQUEST_IMAGE_LIST_GALLERY_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_IMAGE_LIST_GALLERY_RESULT && resultCode == Activity.RESULT_OK && data != null) {

                if(data.getData() != null) {

                    Uri imageUri = data.getData();

                    mImageGenerator.generateCardShowTakenImageFromImageGallery(imageUri, new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
                                @Override
                                public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                    CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                                    mCameraPhotosAdapter.addPicture(cameraPhoto);
                                    mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount() - 1);
                                }

                                @Override
                                public void onError() {

                                }
                            });

                } else if(data.getClipData() != null) {

                    int count = data.getClipData().getItemCount();

                    for(int i = 0; i < count; i++) {

                        Uri imageUri = data.getClipData().getItemAt(i).getUri();

                        mImageGenerator.generateCardShowTakenImageFromImageGallery(imageUri, new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
                                    @Override
                                    public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                        CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                                        mCameraPhotosAdapter.addPicture(cameraPhoto);
                                        mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount() - 1);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });

                    }

                }

            }

        }

    @Override
    public void onResume() {
        super.onResume();
        mCameraSetup.getFotoapparat().start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCameraSetup.getFotoapparat().stop();
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
    }

    @Override
    public void setPhotos(ArrayList<CameraPhoto> photos) {
        if (photos != null) {
            mCameraPhotosAdapter.addAllPhotos(photos);
        }
    }

    @Override
    public int getItemCount() {
        return mCameraPhotosAdapter.getItemCount();
    }

    @Override
    public void takePicture() {

        if (isLimitReached()) {
            PhotoResult photoResult = mCameraSetup.getFotoapparat().takePicture();

            String uuid = UUID.randomUUID().toString();

            File photoPath = new File(mPath.toString() + "/" + uuid + JPEG_FILE_SUFFIX);

            photoResult.saveToFile(photoPath);

            photoResult.toBitmap().whenAvailable(
                    bitmapPhoto -> {
                        mImageGenerator.generateCardShowTakenImageFromCamera(bitmapPhoto.bitmap, new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
                            @Override
                            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                                CameraPhoto cameraPhoto = new CameraPhoto(imageFilename, tempImagePath, new Date(), new Date());

                                mCameraPhotosAdapter.addPicture(cameraPhoto);
                                mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount() - 1);

                                photoPath.delete();

                                updateCounters();
                            }

                            @Override
                            public void onError() {

                            }
                        });

                        return null;
                    }
            );

        } else {
            Toast.makeText(getContext(), getString(R.string.camera_photo_reached_limit), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void returnImagesToCardShowTakenPicturesView() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(KEY_IMAGE_CAMERA_LIST, (Serializable) mCameraPhotosAdapter.getList());
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }

    public boolean isLimitReached() {
        return mPhotosLimit == -1 || (mImageListSize + getItemCount()) < mPhotosLimit;
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

    public void updateCounters(){
        if (mPhotosLimit == -1) {
            mCameraFragmentBinding.cameraFragmentChip.setVisibility(View.GONE);
        } else {
            getActivity().runOnUiThread(() -> {
                mCameraFragmentBinding.cameraFragmentCurrentValue.setText(String.valueOf(mImageListSize + getItemCount()));
                mCameraFragmentBinding.cameraFragmentLimitValue.setText(String.valueOf(mPhotosLimit));
            });
        }
    }

    private int convertDpToPixels(int dpValue) {
        float roundingValue = 0.5f;
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + roundingValue);
    }

}
