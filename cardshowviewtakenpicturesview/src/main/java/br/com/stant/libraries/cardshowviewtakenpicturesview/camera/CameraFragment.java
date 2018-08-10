package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils.CameraSetup;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil;
import io.fotoapparat.result.PhotoResult;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_CAMERA_LIST;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil.JPEG_FILE_SUFFIX;

public class CameraFragment extends Fragment implements CameraContract {

    private static Integer mPhotosLimit;
    private static Integer mImageListSize;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath = PhotoViewFileUtil.getFile();
    private ImageButton mButtonCapture;
    private CameraSetup mCameraSetup;
    private ImageView mButtonReturnPhotos;
    private ImageView mButtonClose;
    private RecyclerView mPhotosRecyclerView;
    private LinearLayout mNavigationCamera;

    public static CameraFragment newInstance(Integer limitOfImages, Integer imageListSize) {
        mPhotosLimit = limitOfImages;
        mImageListSize = imageListSize;

        return new CameraFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoViewFileUtil.createTempDirectory(mPath);

        mCameraPhotosAdapter = new CameraPhotosAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_fragment, container, false);

        mButtonClose = mCameraFragmentBinding.cameraFragmentClose;
        mButtonCapture = mCameraFragmentBinding.cameraFragmentCaptureImageButton;
        mButtonReturnPhotos = mCameraFragmentBinding.cameraFragmentSaveImageView;
        mPhotosRecyclerView = mCameraFragmentBinding.cameraPhotosRecyclerView;
        mNavigationCamera = mCameraFragmentBinding.cameraFragmentBottomLinearLayout;

        if (hasNavigationBar()) {
            setNavigationCameraControlsPadding();
        }

        mButtonClose.setOnClickListener(view -> closeCamera());
        mButtonCapture.setOnClickListener(view -> takePicture());
        mButtonReturnPhotos.setOnClickListener(view -> returnImagesToCardShowTakenPicturesView());

        mPhotosRecyclerView.setNestedScrollingEnabled(true);
        mPhotosRecyclerView.setFocusable(false);
        mPhotosRecyclerView.setAdapter(mCameraPhotosAdapter);

        mCameraSetup = new CameraSetup(getContext(),
                mCameraFragmentBinding.cameraFragmentView,
                mCameraFragmentBinding.cameraFragmentFocusView);

        mCameraSetup.toggleTorchOnSwitch(mCameraFragmentBinding.cameraFragmentSwitchFlash);
        mCameraSetup.zoomSeekBar(mCameraFragmentBinding.cameraFragmentZoomSeekBar);
        mCameraSetup.switchCameraOnClick(mCameraFragmentBinding.cameraFragmentSwitchLens);

        return mCameraFragmentBinding.getRoot();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), getResources().getString(R.string.camera_no_permission), Toast.LENGTH_SHORT).show();
                getActivity().finish();
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
    public void closeCamera() {
        getActivity().finish();
    }

    @Override
    public void setPhotos(ArrayList<CameraPhoto> photos) {
        if (photos != null) {
            mCameraPhotosAdapter.replaceData(photos);
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

            photoResult.toBitmap().whenDone(bitmapPhoto -> {
                File fileName = new File(uuid);

                assert bitmapPhoto != null;
                CameraPhoto cameraPhoto = new CameraPhoto(
                        fileName.toString(),
                        photoPath.toString(),
                        new Date(),
                        new Date());

                mCameraPhotosAdapter.addPicture(cameraPhoto);
                mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(getItemCount() - 1);
            });

        } else {
            Toast.makeText(getContext(), getResources().getString(R.string.camera_photo_reached_limit), Toast.LENGTH_SHORT).show();
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

    private int convertDpToPixels(int dpValue) {
        float roundingValue = 0.5f;
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + roundingValue);
    }

}
