package br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums;

/**
 * Created by denisvieira on 11/06/17.
 */

public enum CardShowTakenPictureStateEnum {
    NORMAL(0), EDIT(1);

    private final int mValue;

    CardShowTakenPictureStateEnum(int value) {
        mValue = value;
    }

    public int getValue(){
        return mValue;
    }
}
