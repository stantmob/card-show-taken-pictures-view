package br.com.stant.libraries.cameraimagegalleryview.activities;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;

import br.com.stant.libraries.cameraimagegalleryview.adapters.PopUpErrorsAdapter;
import br.com.stant.libraries.cameraimagegalleryview.components.DeleteAlertDialog;
import br.com.stant.libraries.cameraimagegalleryview.enums.ImageStatus;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.FullScreenBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class FullScreenImage extends AppCompatActivity {

    private FullScreenBinding mBinding;
    private PopUpErrorsAdapter mPopUpErrorsAdapter;
    private CardShowTakenImage image;
    private PopupWindow mErrorsPopUp;
    private CardShowTakenImageInjection mCardShowTakenImage;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.full_screen);

        mCardShowTakenImage = CardShowTakenImageInjection.getCardShowTakenPictureInjection();

        setSupportActionBar(mBinding.topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setValues();
        configurePopUpErrorsAdapter();
        configurePopUp();
//        configureCaptionEditText();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mErrorsPopUp.dismiss();
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


    private void configurePopUpErrorsAdapter() {
        mPopUpErrorsAdapter = new PopUpErrorsAdapter(this, image.getErrors());
    }

    private PopupWindow createErrorsPopUp() {
        View view = LayoutInflater.from(this).inflate(R.layout.errors_pop_up, null);
        RecyclerView errorsRecycleView = view.findViewById(R.id.errors_recycler_view);
        errorsRecycleView.setAdapter(mPopUpErrorsAdapter);

        return new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void configurePopUp() {
        mErrorsPopUp = createErrorsPopUp();
        mErrorsPopUp.setOutsideTouchable(true);
        mErrorsPopUp.setFocusable(true);
        mErrorsPopUp.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void configureCaptionEditText() {
        mBinding.captionEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mBinding.captionEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

    private void startEditMode() {
        mMenu.setGroupVisible(R.id.show_mode, false);
        mMenu.setGroupVisible(R.id.edit_mode, true);

        mBinding.captionEditText.setFocusable(true);
        mBinding.captionEditText.setCursorVisible(true);

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

        String caption = mBinding.captionEditText.getText().toString();
        image.setCaption(caption);

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

    private void removeImage(){
        DeleteAlertDialog deleteAlertDialog = new DeleteAlertDialog(this, new DeleteAlertDialog.OnDelete() {
            @Override
            public void delete() {
                mCardShowTakenImage.removeImage(image);
                finish();
            }

            @Override
            public void cancel() {}
        });

        deleteAlertDialog.onCreateDialog(null).show();
    }

}