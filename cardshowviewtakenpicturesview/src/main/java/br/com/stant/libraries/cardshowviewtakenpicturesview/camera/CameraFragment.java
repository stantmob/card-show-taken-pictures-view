package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraFragment extends Fragment implements CameraContract {

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "/stantOccurrences/temp/");
    private ImageButton mButtonCapture;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Fotoapparat mFotoapparat;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PhotoViewFileUtil.createTempDirectory(mPath);
        mCameraPhotosAdapter = new CameraPhotosAdapter(getContext(), new ArrayList<>());
        setExampleImages();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCameraFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.camera_fragment, container, false);

        mButtonCapture = mCameraFragmentBinding.cameraFragmentCaptureImageButton;
        mButtonCapture.setOnClickListener(view -> takePicture());

        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        mCameraFragmentBinding.cameraPhotosRecyclerView.setLayoutManager(layout);
        mCameraFragmentBinding.cameraPhotosRecyclerView.setNestedScrollingEnabled(true);
        mCameraFragmentBinding.cameraPhotosRecyclerView.setFocusable(false);
        mCameraFragmentBinding.cameraPhotosRecyclerView.setAdapter(mCameraPhotosAdapter);

        mFotoapparat = new Fotoapparat(getContext(), mCameraFragmentBinding.cameraFragmentView);

        return mCameraFragmentBinding.getRoot();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mFotoapparat.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFotoapparat.stop();
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
        PhotoResult photoResult = mFotoapparat.takePicture();

        File photoPath = new File(mPath.toString() + UUID.randomUUID().toString());
        photoResult.saveToFile(photoPath);
    }


    public void setExampleImages() {
        ArrayList<CameraPhoto> images = new ArrayList<>();
        images.add(new CameraPhoto(null, "http://www.cityofsydney.nsw.gov.au/__data/assets/image/0009/105948/Noise__construction.jpg"));

        setPhotos(images);
    }


}
