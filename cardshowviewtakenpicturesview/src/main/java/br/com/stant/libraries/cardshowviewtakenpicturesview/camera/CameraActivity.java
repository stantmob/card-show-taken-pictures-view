package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ActivityUtils;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_LIST_SIZE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_LIMIT_IMAGES;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        Integer limitImages = getIntent().getIntExtra(KEY_LIMIT_IMAGES, -1);
        Integer image_list_size = getIntent().getIntExtra(KEY_IMAGE_LIST_SIZE, 0);

        CameraFragment mCameraFragment = CameraFragment.newInstance(limitImages, image_list_size);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mCameraFragment, R.id.camera_content_frame);
    }


}
