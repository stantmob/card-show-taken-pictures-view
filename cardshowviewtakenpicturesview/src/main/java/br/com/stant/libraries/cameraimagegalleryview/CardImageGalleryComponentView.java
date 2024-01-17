package br.com.stant.libraries.cameraimagegalleryview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import br.com.stant.libraries.cameraimagegalleryview.activities.CardImageGalleryView;
import br.com.stant.libraries.cameraimagegalleryview.components.Camera;
import br.com.stant.libraries.cameraimagegalleryview.components.CardImageGalleryAvatar;
import br.com.stant.libraries.cameraimagegalleryview.injections.CardShowTakenImageInjection;
import br.com.stant.libraries.cameraimagegalleryview.model.Theme;
import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.CameraActivity;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardImageGalleryComponentViewBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.enums.CardShowTakenPictureStateEnum;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CardShowTakenImage;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.SaveOnlyMode;

public class CardImageGalleryComponentView extends LinearLayout implements CardImageGalleryViewContract {

    public final static String KEY_APP_BAR_NAME = "app_bar_name";

    private AppCompatActivity mActivity;
    private Fragment mFragment;
    private CardShowTakenImageInjection mCardShowTakenImages;
    private Context mContext;
    private CardImageGalleryComponentViewBinding mBinding;
    private TypedArray mStyledAttributes;
    private Integer mImagesQuantityLimit;
    private OnReachedOnTheImageCountLimit mOnReachedOnTheImageCountLimit;
    private SaveOnlyMode mSaveOnlyMode;
    private Camera mCamera;
    private String galleryAppName = "";


    public CardImageGalleryComponentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CardShowTakenPictureView);
        mCardShowTakenImages = CardShowTakenImageInjection.getCardShowTakenPictureInjection();

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.card_image_gallery_component_view, this, true);
        mBinding.setHandler(this);

        setOrientation(HORIZONTAL);

        mCardShowTakenImages.addListener(() -> {
            invalidateIcon();
            if (!mCardShowTakenImages.hasImagesWithErrors()) {
                removeStrokeError();
            }
            loadImagesComponent();
            updateCurrentAndLimitImagesQuantityText(mCardShowTakenImages.getAll().size());
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCardShowTakenImages != null) {
            mCardShowTakenImages.clear();
        }
    }

    public void setActivityAndRegisterForCamera(Activity activity) {
        mActivity = (AppCompatActivity) activity;
        registerActivityForCamera();
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
        setActivityAndRegisterForCamera(fragment.getActivity());
    }

    // Begin Component
    @Override
    public void setImagesQuantityLimit(Integer limitQuantity, OnReachedOnTheImageCountLimit onReachedOnTheImageCountLimit) {
        Integer currentImagesQuantity = mCardShowTakenImages.getAll().size();
        mImagesQuantityLimit = limitQuantity;
        mOnReachedOnTheImageCountLimit = onReachedOnTheImageCountLimit;

        mBinding.cardImageGalleryComponentPhotosQuantityTextView.setVisibility(VISIBLE);

        Camera.setmOnReachedOnTheImageCountLimit(onReachedOnTheImageCountLimit);
        Camera.setmImagesQuantityLimit(limitQuantity);

        updateCurrentAndLimitImagesQuantityText(currentImagesQuantity);
    }

    @Override
    public void updateCurrentAndLimitImagesQuantityText(Integer currentQuantity) {
        mBinding.setCurrentAndLimitPhotosQuantityText(
                currentQuantity + "/" + mImagesQuantityLimit
        );
    }

    public boolean hasUpdatedAt() {
        return mBinding.getUpdatedAt() != null;
    }

    public boolean hasPictureByName() {
        return mBinding.getPictureByName() != null;
    }

    @Override
    public boolean hasImages() {
        return mCardShowTakenImages.hasImages();
    }

    @Override
    public boolean hasImageByIdentifier(String identifier) {
        return mCardShowTakenImages.hasImageByIdentifier(identifier);
    }

    @Override
    public void setCardImages(List<CardShowTakenImage> cardShowTakenImages) {
        mCardShowTakenImages.setImages(cardShowTakenImages);
        if(mCardShowTakenImages.hasImagesWithErrors()){
            showStrokeError();
        }
    }

    private void loadImagesComponent() {
        int count = mBinding.cardImageGalleryComponentContainerLinearLayout.getChildCount();
        for(int i = count - 1; i < mCardShowTakenImages.getAll().size() && i < 4; i++){
            new CardImageGalleryAvatar(mContext,
                    mBinding.cardImageGalleryComponentContainerLinearLayout,
                    mCardShowTakenImages.getAll().get(i));
        }
    }

    @Override
    public List<CardShowTakenImage> getCardImages() {
        return mCardShowTakenImages.getAll();
    }

    @Override
    public List<CardShowTakenImage> getCardImagesAsAdded() {
        return mCardShowTakenImages.getAllAdded();
    }

    @Override
    public List<CardShowTakenImage> getCardImagesAsRemoved() {
        return mCardShowTakenImages.getAllRemoved();
    }

    public void showStrokeError() {
        GradientDrawable drawable = (GradientDrawable) mBinding
                .cardImageGalleryComponentContainerLinearLayout.getBackground().mutate();
        drawable.setStroke(8, getResources().getColor(R.color.dark_red));
    }

    private void removeStrokeError() {
        GradientDrawable drawable = (GradientDrawable) mBinding
                .cardImageGalleryComponentContainerLinearLayout.getBackground().mutate();
        drawable.setStroke(0, getResources().getColor(R.color.white));
    }

    public void setExampleImages() {
        List<CardShowTakenImage> images = new ArrayList<>();

        images.add(new CardShowTakenImage("1", "https://upload.wikimedia.org/wikipedia/commons/b/b6/Image_created_with_a_mobile_phone.png", "", "", new Date(),
                new Date(), "", Arrays.asList("Error 1Error 1Error 1Error 1Error 1Error 1Error d", "Error 1Error 1Error 1Error 1Error 1Error 1Error d", "Error 1Error 1Error 1Error 1Error 1Error 1Error d")));
        images.add(new CardShowTakenImage("2", "https://www.freecodecamp.org/news/content/images/size/w2000/2022/09/jonatan-pie-3l3RwQdHRHg-unsplash.jpg", "", "", new Date(),
                new Date(), "CAPTION", Arrays.asList("Error 1Error 1Error 1Error 1Error 1Erro 1Error 1Erro")));
        images.add(new CardShowTakenImage("3", "https://purepng.com/public/uploads/thumbnail//nature-ekq.png", "", "", new Date(),
                new Date(), "CAPTION", Arrays.asList()));
        images.add(new CardShowTakenImage("4", "https://freepngimg.com/download/environment/2-2-environment-png-image.png", "", "", new Date(),
                new Date(), "CAPTION", Arrays.asList()));

        setCardImages(images);
    }

    public void onClickIcon(View view) {
        if (hasImages()) {
            this.goToGallery();
        } else {
            mCamera.pickPictureToFinishAction();
        }
    }

    public void goToGallery() {
        Intent intent = new Intent(mContext, CardImageGalleryView.class);
        intent.putExtra(Camera.KEY_LIMIT_IMAGES, mImagesQuantityLimit);
        intent.putExtra(KEY_APP_BAR_NAME, galleryAppName);
        mContext.startActivity(intent);
    }

    private void invalidateIcon() {
        if (hasImages()) {
            mBinding.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_imagem_multiple_eye));
        } else {
            mBinding.icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_image_plus));
        }

    }

    public String getGalleryAppName() {
        return galleryAppName;
    }

    public void setGalleryAppName(String galleryAppName) {
        this.galleryAppName = galleryAppName;
    }

    public String getToolBarColor() {
        return Theme.ToolBarColor;
    }

    public void setToolBarColor(String toolBarColor) {
        Theme.ToolBarColor = toolBarColor;
    }

    public String getTitleToolBarColor() {
        return Theme.TitleToolBarColor;
    }

    public void setTitleToolBarColor(String titleToolBarColor) {
        Theme.TitleToolBarColor = titleToolBarColor;
    }

    public String getStatusBarColor() {
        return Theme.StatusBarColor;
    }

    public void setStatusBarColor(String statusBarColor) {
        Theme.StatusBarColor = statusBarColor;
    }

    public String getColorIcons() {
        return Theme.ColorIcons;
    }

    public void setColorIcons(String colorIcons) {
        Theme.ColorIcons = colorIcons;
    }

    public Drawable getBackIcon() {
        return Theme.BackIcon;
    }

    public void setBackIcon(Drawable backIcon) {
        Theme.BackIcon = backIcon;
    }

    public String getActivityBackgroundColor() {
        return Theme.ActivityBackground;
    }

    public void setActivityBackgroundColor(String activityBackground) {
        Theme.ActivityBackground = activityBackground;
    }
    public String getBottomBarColor() {
        return Theme.BottomBarColor;
    }

    public void setBottomBarColor(String bottomBarColor) {
        Theme.BottomBarColor = bottomBarColor;
    }

    public String getInfoBoxColor() {
        return Theme.InfoBoxColor;
    }

    public void setInfoBoxColor(String infoBoxColor) {
        Theme.InfoBoxColor = infoBoxColor;
    }

    // End Component

    //Begin EditState

    @Override
    public void setCardStateEnum(CardShowTakenPictureStateEnum cardStateEnum) {
        mBinding.setCardStateEnum(cardStateEnum);
    }

    // End EditState

    // Begin Camera
    @Override
    public void registerActivityForCamera() {
        Intent intent = new Intent(mActivity, CameraActivity.class);
        mCamera = new Camera(getContext(), mActivity);
        ActivityResultLauncher<Intent> openCamera;

        if (mFragment != null) {
            openCamera = mFragment.registerForActivityResult
                    (new ActivityResultContracts.StartActivityForResult(), mCamera::addImageOnActivityResult);
        } else {
            openCamera = mActivity.registerForActivityResult
                    (new ActivityResultContracts.StartActivityForResult(), mCamera::addImageOnActivityResult);
        }

        mCamera.setCameraIntent(intent);
        mCamera.setOpenCamera(openCamera);
    }

    public void enableSaveOnlyMode(Drawable enabledIcon, String enabledWarning, Drawable disabledIcon, String disabledWarning) {
        mSaveOnlyMode = new SaveOnlyMode(getBitmapFromDrawable(enabledIcon), enabledWarning,
                getBitmapFromDrawable(disabledIcon), disabledWarning);
        Camera.setmSaveOnlyMode(mSaveOnlyMode);
    }

    public Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
                return getBitmapFromVectorDrawable(drawable);
            } else {
                throw new IllegalArgumentException("Unsupported drawable type");
            }
        } else if (drawable instanceof VectorDrawableCompat) {
            return getBitmapFromVectorDrawable(drawable);
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }
    }

    private Bitmap getBitmapFromVectorDrawable(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    // End camera

    @BindingAdapter(value = {"pictureByName", "updatedAt"}, requireAll = false)
    public static void setBinding(CardImageGalleryComponentView view,
                                  String mPictureByName, Date updatedAt) {
        if (mPictureByName != null) {
            view.mBinding.setPictureByName(mPictureByName);
        }

        if (updatedAt != null) {
            String pattern = "MM/dd/yyyy";
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            view.mBinding.setUpdatedAt(format.format(updatedAt));
        }
    }
}