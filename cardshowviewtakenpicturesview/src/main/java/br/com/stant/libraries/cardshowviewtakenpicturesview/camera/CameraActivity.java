package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_DRAG_AND_DROP_MODE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IMAGE_LIST_SIZE;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IS_CAPTION_ENABLED;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_IS_MULTIPLE_GALLERY_SELECTION;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_LIMIT_IMAGES;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.CardShowTakenPictureView.KEY_SAVE_ONLY_MODE;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ActivityUtils;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        Integer limitImages = getIntent().getIntExtra(KEY_LIMIT_IMAGES, -1);
        Integer image_list_size = getIntent().getIntExtra(KEY_IMAGE_LIST_SIZE, 0);
        Boolean isMultipleGallerySelection = getIntent().getBooleanExtra(KEY_IS_MULTIPLE_GALLERY_SELECTION, false);
        SaveOnlyMode saveOnlyMode = getIntent().getParcelableExtra(KEY_SAVE_ONLY_MODE);
        Boolean dragAndDropMode = getIntent().getBooleanExtra(KEY_DRAG_AND_DROP_MODE, false);
        Boolean isCaptionEnabled = getIntent().getBooleanExtra(KEY_IS_CAPTION_ENABLED, false);

        CameraFragment mCameraFragment = CameraFragment.newInstance(
                limitImages,
                image_list_size,
                isMultipleGallerySelection,
                saveOnlyMode,
                dragAndDropMode,
                isCaptionEnabled
        );
        hideBarSystem();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mCameraFragment, R.id.camera_content_frame);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideBarSystem();
    }

    private void hideBarSystem() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
}
