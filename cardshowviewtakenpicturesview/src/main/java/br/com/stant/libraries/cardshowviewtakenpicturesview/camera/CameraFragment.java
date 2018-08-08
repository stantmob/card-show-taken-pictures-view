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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureViewContract;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraFragmentBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.reactivex.Observable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraFragment extends Fragment implements CameraContract {

    private CameraFragmentBinding mCameraFragmentBinding;
    private CameraPhotosAdapter mCameraPhotosAdapter;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "/<br.com.stant>/temp");
    private ImageButton mButtonCapture;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Fotoapparat mFotoapparat;
    private ImageGenerator mImageGenerator;

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

        String uuid = UUID.randomUUID().toString();

        File photoPath = new File(mPath.toString() + "/" + uuid + ".jpg");
        photoResult.saveToFile(photoPath);

        photoResult.toBitmap().whenDone(bitmapPhoto -> {
            File fileName = new File(uuid);

            assert bitmapPhoto != null;
            CameraPhoto cameraPhoto = new CameraPhoto(bitmapPhoto.bitmap, fileName.toString(), photoPath.toString());

            mCameraPhotosAdapter.addPicture(cameraPhoto);
            mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(getItemCount() - 1);

        });

    }

    private void sendImagesToAdapter(File file){
        mImageGenerator = new ImageGenerator(getContext(), file, this);

        mImageGenerator.generateCardShowTakenImageFromCamera(file, getActivity(), new CardShowTakenPictureViewContract.CardShowTakenCompressedCallback() {
            @Override
            public void onSuccess(Bitmap bitmap, String imageFilename, String tempImagePath) {
                CameraPhoto cameraPhoto = new CameraPhoto(bitmap, imageFilename, tempImagePath);

                mCameraPhotosAdapter.addPicture(cameraPhoto);
                mCameraFragmentBinding.cameraPhotosRecyclerView.smoothScrollToPosition(mCameraPhotosAdapter.getItemCount() - 1);
                mCameraPhotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError() {
                Toast.makeText(getContext(), "Erro to add photo", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void setExampleImages() {
        ArrayList<CameraPhoto> images = new ArrayList<>();
        images.add(new CameraPhoto(null, "http://www.cityofsydney.nsw.gov.au/__data/assets/image/0009/105948/Noise__construction.jpg"));

        setPhotos(images);
    }


}
