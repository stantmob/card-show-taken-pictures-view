package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ActivityUtils;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        CameraFragment mCameraFragment = CameraFragment.newInstance();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mCameraFragment, R.id.camera_content_frame);
    }

}
