package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import static com.annimon.stream.Optional.ofNullable;

public class VibratorUtils {

    public static void vibrate(Context context, int duration) {
        ofNullable((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).ifPresent(
                (vibrator) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(duration);
                    }
                }
        );
    }


}
