package br.com.stant.libraries.cameraimagegalleryview.activities;

import static br.com.stant.libraries.cameraimagegalleryview.CardImageGalleryViewContract.KEY_IMAGE_FULL_SCREEN;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.setImageBitmapToImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.Arrays;

import br.com.stant.libraries.cameraimagegalleryview.adapters.PopUpErrorsAdapter;
import br.com.stant.libraries.cameraimagegalleryview.enums.ImageStatus;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.FullScreenBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;

public class FullScreenImage extends AppCompatActivity {

    private FullScreenBinding mBinding;
    private PopUpErrorsAdapter mPopUpErrorsAdapter;
    private CardShowTakenImage image;

    private PopupWindow mErrorsPopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.full_screen);


        setSupportActionBar(mBinding.topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setValues();
        configurePopUpErrorsAdapter();
        configurePopUp();
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
        setImageBitmapToImageView(mBinding.imageView,
                image, 8);

        mBinding.captionTextView.setText(image.getCaption());

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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!image.hasError()) return false;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.erros_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.collapsible) {
            mErrorsPopUp.showAsDropDown(mBinding.topAppBar);
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
}