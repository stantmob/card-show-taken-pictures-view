package br.com.stant.libraries.cameraimagegalleryview.activities;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.DataBindingUtil;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

import br.com.stant.libraries.cameraimagegalleryview.components.DeleteAlertDialog;
import br.com.stant.libraries.cameraimagegalleryview.model.ImageStatus;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cameraimagegalleryview.model.Theme;
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
        mBinding.infoBox.setBackgroundColor(Color.parseColor(Theme.ActivityBackground));

        setValues();
    }

    private void configureToolBar() {
        setSupportActionBar(mBinding.topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBinding.topAppBar.setBackgroundColor(Color.parseColor(Theme.ToolBarColor));
        getWindow().setStatusBarColor(Color.parseColor(Theme.StatusBarColor));
        getSupportActionBar().setHomeAsUpIndicator(Theme.BackIcon);
    }

    private void setValues() {
        image = (CardShowTakenImage) getIntent().getSerializableExtra(KEY_IMAGE_FULL_SCREEN);
        setImageBitmapToImageView(mBinding.fullImageView,
                image, 8);

        mBinding.captionEditText.setText(image.getCaption());

        if (image.getStatus() == ImageStatus.Approved) {
            mBinding.statusTextView.setText(R.string.full_screen_image_status_approved);
            mBinding.statusTextView.setTextColor(getResources().getColor(R.color.green));
        } else if (image.getStatus() == ImageStatus.Disapproved) {
            mBinding.statusTextView.setText(R.string.full_screen_image_status_disapproved);
            mBinding.statusTextView.setTextColor(getResources().getColor(R.color.red));
        }

        mBinding.topAppBar.setNavigationOnClickListener((View view) -> {
            onBackPressed();
        });

        if (image.hasError()) {
            mBinding.errorContainer.setVisibility(View.VISIBLE);
            mBinding.errorsList.setText(
                    image.getErrorsAsString()
            );
            mBinding.errorsList.setMovementMethod(new ScrollingMovementMethod());
        }
        onClickIconErrors();

    }

    private void onClickIconErrors() {
        mBinding.errorsIcon.setOnClickListener((view) -> {
            if (showErrorsMode) {
                mBinding.errorsList.setLines(1);
                animateIcon(0);
            } else {
                mBinding.errorsList.setMaxLines(50);
                animateIcon(90f);
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
        hideKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        mMenu = menu;
        try {
            Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.ic_delete_white)).setTint(Color.parseColor(Theme.ColorIcons));
            Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.ic_edit_white)).setTint(Color.parseColor(Theme.ColorIcons));
            Objects.requireNonNull(AppCompatResources.getDrawable(this, R.drawable.ic_done)).setTint(Color.parseColor(Theme.ColorIcons));
        } catch (Exception e) {
            return false;
        }

        inflater.inflate(R.menu.full_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.edit) {
            startEditMode();
            return true;
        } else if (itemId == R.id.delete) {
            removeImage();
            return true;
        } else if (itemId == R.id.save) {
            saveChanges();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startEditMode() {
        mMenu.setGroupVisible(R.id.show_mode, false);
        mMenu.setGroupVisible(R.id.edit_mode, true);

        mBinding.captionEditText.setFocusable(true);
        mBinding.captionEditText.setCursorVisible(true);

        mBinding.statusTextView.setVisibility(View.GONE);

        mBinding.captionEditText.requestFocusFromTouch();
        int index = mBinding.captionEditText.getText().toString().length();

        mBinding.captionEditText.setSelection(index);

        showKeyboard();
    }

    private void saveChanges() {
        mMenu.setGroupVisible(R.id.show_mode, true);
        mMenu.setGroupVisible(R.id.edit_mode, false);

        mBinding.captionEditText.setFocusable(false);
        mBinding.captionEditText.setCursorVisible(false);
        mBinding.statusTextView.setVisibility(View.VISIBLE);

        String caption = mBinding.captionEditText.getText().toString();
        for (CardShowTakenImage cardShowTakenImage : mCardShowTakenImage.getAll()) {
            if (cardShowTakenImage.equals(image)) {
                cardShowTakenImage.setCaption(caption);
                image.setCaption(caption);
                break;
            }
        }

        hideKeyboard();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBinding.captionEditText.getWindowToken(), 0);
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