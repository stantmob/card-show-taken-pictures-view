package br.com.stant.libraries.cameraimagegalleryview.activities;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Objects;

import br.com.stant.libraries.cameraimagegalleryview.components.DeleteAlertDialog;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cameraimagegalleryview.model.Proprieties;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.FullScreenBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class FullScreenImage extends AppCompatActivity {

    private FullScreenBinding mBinding;
    private CardShowTakenImage image;
    private CardShowTakenImageInjection mCardShowTakenImage;
    private Menu mMenu;

    public boolean showErrorsMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.full_screen);

        mCardShowTakenImage = CardShowTakenImageInjection.getCardShowTakenPictureInjection();

        configureToolBar();
        mBinding.infoBox.setBackgroundColor(Color.parseColor(Proprieties.InfoBoxColor));

        setValues();
    }

    private void configureToolBar() {
        setSupportActionBar(mBinding.topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBinding.topAppBar.setBackgroundColor(Color.parseColor(Proprieties.ToolBarColor));
        getWindow().setStatusBarColor(Color.parseColor(Proprieties.StatusBarColor));
        getSupportActionBar().setHomeAsUpIndicator(Proprieties.BackIcon);
    }

    private void setValues() {
        image = (CardShowTakenImage) getIntent().getSerializableExtra(KEY_IMAGE_FULL_SCREEN);
        setImageBitmapToImageView(mBinding.fullImageView,
                image, 1);

        mBinding.captionEditText.setText(image.getCaption());


        mBinding.topAppBar.setNavigationOnClickListener((View view) -> {
            onBackPressed();
        });

        if(image.hasError()){
            mBinding.errorContainer.setVisibility(View.VISIBLE);
            mBinding.errorsList.setText(image.getErrorsAsString());
            mBinding.setHasError(true);

            if(image.getErrors().size() == 1) {
                mBinding.errorsIcon.setVisibility(View.GONE);
            } else {
                mBinding.errorsList.setMovementMethod(new ScrollingMovementMethod());
                mBinding.errorsList.setLines(2);
                onClickIconErrors();
            }
        }

        if(Proprieties.readyModeOn){
            mBinding.captionEditText.setFocusable(false);
            mBinding.saveTextView.setVisibility(View.GONE);
            if(image.getCaption() == null || image.getCaption().isEmpty()){
                mBinding.captionEditText.setHint(R.string.fullscreen_without_caption_in_read_mode);
            }
        }

        mBinding.saveTextView.setOnClickListener(this::saveChanges);
    }

    private void onClickIconErrors() {
        mBinding.errorsIcon.setOnClickListener((view) -> {
            if (showErrorsMode) {
                mBinding.errorsList.setLines(2);
                animateIcon(0f);
            } else {
                mBinding.errorsList.setMaxLines(50);
                animateIcon(180f);
            }
            showErrorsMode = !showErrorsMode;
        });
    }

    private void animateIcon(float goTo) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mBinding.errorsIcon, "rotation", goTo);
        anim.setDuration(500);
        anim.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(Proprieties.readyModeOn) return false;
        MenuInflater inflater = getMenuInflater();
        mMenu = menu;

        inflater.inflate(R.menu.full_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.delete) {
            removeImage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveChanges(View view) {
        String caption = mBinding.captionEditText.getText().toString();
        image.setCaption(caption);
        mBinding.captionEditText.clearFocus();
        Toast.makeText(this, getResources().getString(R.string.full_screen_caption_updated), Toast.LENGTH_LONG).show();
        mCardShowTakenImage.updateImage(image);
    }

    private void removeImage() {
        DeleteAlertDialog deleteAlertDialog = new DeleteAlertDialog(this, new DeleteAlertDialog.OnDelete() {
            @Override
            public void delete() {
                mCardShowTakenImage.removeImage(image);
                finish();
            }

            @Override
            public void cancel() {
            }
        });

        deleteAlertDialog.onCreateDialog(null).show();
    }

}