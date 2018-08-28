package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.content.Context;
import android.provider.Settings;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;

public abstract class OrientationListener extends OrientationEventListener {

    private Context mContext;
    private View[] mViews;
    private final int ROTATION_O   = 1;
    private final int ROTATION_90  = 2;
    private final int ROTATION_180 = 3;
    private final int ROTATION_270 = 4;
    private int prevOrientation    = OrientationEventListener.ORIENTATION_UNKNOWN;
    private int mRotation          = 0;
    private int mRotationState     = 0;

    protected OrientationListener(Context context, View... views) {
        super(context);
        mContext = context;
        mViews = views;
    }


    @Override
    public void onOrientationChanged(int orientation) {

        if (android.provider.Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0) // 0 = Auto Rotate Disabled
            return;
        int currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        if (orientation >= 340 || orientation < 20 && mRotation != ROTATION_O) {
            currentOrientation = Surface.ROTATION_0;
            mRotation = ROTATION_O;

        } else if (orientation >= 70 && orientation < 110 && mRotation != ROTATION_90) {
            currentOrientation = Surface.ROTATION_90;
            mRotation = ROTATION_90;

        } else if (orientation >= 160 && orientation < 200 && mRotation != ROTATION_180) {
            currentOrientation = Surface.ROTATION_180;
            mRotation = ROTATION_180;

        } else if (orientation >= 250 && orientation < 290 && mRotation != ROTATION_270) {
            currentOrientation = Surface.ROTATION_270;
            mRotation = ROTATION_270;
        }

        if (prevOrientation != currentOrientation && orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            prevOrientation = currentOrientation;
            if (currentOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                onSimpleOrientationChanged(mRotation);
            }
        }
    }

    protected void setOrientationView(int orientation){
        switch(orientation){
            case ROTATION_O:
                //rotate as on portrait
                rotateViews(0);
                mRotationState = 90;
                break;
            case ROTATION_90:
                //rotate as left on top
                rotateViews(-90);
                mRotationState = 180;
                break;
            case ROTATION_270:
                //rotate as right on top
                rotateViews(90);
                mRotationState = 0;
                break;
            case ROTATION_180:
                //rotate as upside down
                rotateViews(180);
                mRotationState = 270;
                break;

        }
    }

    private void rotateViews(int rotation){
        for (View mView : mViews) {
            mView.animate().rotation(rotation).setDuration(250).start();
        }
    }

    public int getRotationState(){
        return mRotationState;
    }

    public abstract void onSimpleOrientationChanged(int orientation);

}
