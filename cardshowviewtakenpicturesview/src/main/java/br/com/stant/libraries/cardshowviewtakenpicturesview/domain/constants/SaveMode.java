package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.constants;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SaveMode {

    public static final String SAVE_ONLY_MODE = "save_only_mode";
    public static final String STANT_MODE     = "stant_mode";

    private String mode;

    @StringDef({SAVE_ONLY_MODE, STANT_MODE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode{
    }

    public SaveMode(@Mode String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }


}
