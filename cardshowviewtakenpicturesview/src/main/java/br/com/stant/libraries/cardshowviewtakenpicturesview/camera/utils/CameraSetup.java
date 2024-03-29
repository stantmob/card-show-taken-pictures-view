package br.com.stant.libraries.cardshowviewtakenpicturesview.camera.utils;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator.fromCameraBack;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageGenerator.fromCameraFront;
import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.selector.AspectRatioSelectorsKt.standardRatio;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.on;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.LensPositionSelectorsKt.front;
import static io.fotoapparat.selector.PreviewFpsRangeSelectorsKt.highestFps;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static io.fotoapparat.selector.SensorSensitivitySelectorsKt.highestSensorSensitivity;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.view.CameraView;
import io.fotoapparat.view.FocusView;

public class CameraSetup {

    private final Context mContext;
    private final Fotoapparat mFotoapparat;
    private final CameraConfiguration mCameraConfiguration;

    private boolean mActiveCameraBack;
    public boolean mIsFlashChecked;
    public boolean mIsDataInfoChecked;
    private Integer mLensPosition;

    public CameraSetup(Context context, CameraView cameraView, FocusView focusView) {
        mContext = context;
        mFotoapparat = createCamera(focusView, cameraView);
        mCameraConfiguration = createSettings();
        mLensPosition = fromCameraFront;
    }

    private Fotoapparat createCamera(FocusView focusView, CameraView cameraView) {
        return Fotoapparat
                .with(mContext)
                .into(cameraView)
                .focusView(focusView)
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(back())
                .logger(loggers(
                        logcat(),
                        fileLogger(mContext)
                ))
                .cameraErrorCallback(e -> {
                    Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
                })
                .build();
    }

    private CameraConfiguration createSettings() {
        return CameraConfiguration.builder()
                .photoResolution(standardRatio(
                        highestResolution()
                ))
                .focusMode(firstAvailable(
                        autoFocus(),
                        continuousFocusPicture(),
                        fixed()
                ))
                .flash(firstAvailable(
                        off(),
                        autoRedEye(),
                        autoFlash(),
                        on()
                ))
                .previewFpsRange(highestFps())
                .sensorSensitivity(highestSensorSensitivity())
                .build();
    }

    public Fotoapparat getFotoapparat() {
        return mFotoapparat;
    }

    public void toggleTorchOnSwitch(View view) {
        view.setOnClickListener(v -> {
            boolean isNotChecked = !mIsFlashChecked;

            if (isNotChecked) {
                mFotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(on()).build());
                changeViewImageResource((ImageView) view, R.drawable.ic_flash_yes);
                mIsFlashChecked = true;
            } else {
                mFotoapparat.updateConfiguration(UpdateConfiguration.builder().flash(off()).build());
                changeViewImageResource((ImageView) view, R.drawable.ic_flash_no);
                mIsFlashChecked = false;
            }
        });
    }

    public void toggleDataInfoOnSwitch(View view, LinearLayout linearLayout) {
        view.setOnClickListener(v -> {
            boolean isNotChecked = !mIsDataInfoChecked;

            if (isNotChecked) {
                linearLayout.setVisibility(View.VISIBLE);
                changeViewImageResource((ImageView) view, R.drawable.ic_baseline_data_on);
                mIsDataInfoChecked = true;
            } else {
                linearLayout.setVisibility(View.GONE);
                changeViewImageResource((ImageView) view, R.drawable.ic_baseline_data_off);
                mIsDataInfoChecked = false;
            }
        });
    }

    public void zoomSeekBar(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFotoapparat.setZoom(progress / (float) seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void switchCameraOnClick(View switchCameraView, View flashView) {
        boolean hasFrontCamera = mFotoapparat.isAvailable(front());

        if (hasFrontCamera) {
            switchCameraView.setVisibility(View.VISIBLE);
            switchCameraOnClickVerify(switchCameraView, flashView);
        } else {
            switchCameraView.setVisibility(View.GONE);
        }
    }

    private void switchCameraOnClickVerify(View view, View flashView) {
        view.setOnClickListener(
                (v) -> {
                    mIsFlashChecked = false;
                    changeViewImageResource((ImageView) flashView, R.drawable.ic_flash_no);

                    if (mActiveCameraBack) {
                        mFotoapparat.switchTo(back(), mCameraConfiguration);
                        mLensPosition = fromCameraBack;
                    } else {
                        mFotoapparat.switchTo(front(), mCameraConfiguration);
                        mLensPosition = fromCameraFront;
                    }

                    mActiveCameraBack = !mActiveCameraBack;
                }
        );
    }

    private void changeViewImageResource(final ImageView imageView, @DrawableRes final int resId) {
        imageView.postDelayed(() -> imageView.setImageResource(resId), 120);
    }

    public Integer getLensPosition() {
        return mLensPosition;
    }


}
