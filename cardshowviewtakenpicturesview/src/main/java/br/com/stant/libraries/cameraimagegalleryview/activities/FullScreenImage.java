package br.com.stant.libraries.cameraimagegalleryview.activities;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;

import br.com.stant.libraries.cameraimagegalleryview.enums.ImageStatus;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.FullScreenBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class FullScreenImage extends AppCompatActivity {

    private FullScreenBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.full_screen);
        setValues();
    }

    private void setValues() {
        CardShowTakenImage image = (CardShowTakenImage) getIntent().getSerializableExtra(KEY_IMAGE_FULL_SCREEN);

        setImageBitmapToImageView(mBinding.fullImageView,
                image, 8);

        mBinding.captionTextView.setText(image.getCaption());
        mBinding.errorTextView.setText(image.getErrorsAsString());
        mBinding.errorTextView.setMovementMethod(new ScrollingMovementMethod());

        if (image.getStatus() == ImageStatus.Approved) {
            mBinding.statusTextView.setText(R.string.full_screen_image_status_approved);
            mBinding.statusTextView.setTextColor(getResources().getColor(R.color.green));
        } else if (image.getStatus() == ImageStatus.Disapproved){
            mBinding.statusTextView.setText(R.string.full_screen_image_status_disapproved);
            mBinding.statusTextView.setTextColor(getResources().getColor(R.color.red));
        }

        mBinding.backButton.setOnClickListener((View view) -> {
            onBackPressed();
        });

    }

}