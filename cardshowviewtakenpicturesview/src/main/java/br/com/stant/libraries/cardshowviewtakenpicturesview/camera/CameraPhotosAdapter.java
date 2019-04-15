package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.camera.callbacks.CameraPhotoItemCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoRecyclerViewItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.BitmapFromFileCallback;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.VibratorUtils;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.DragAndDropHandler;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.listener.ItemTouchHelperViewHolder;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.viewholders.LoadingIconItemViewHolder;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.deleteFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;

public class CameraPhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DragAndDropHandler {

    private static int ITEM_VIEW_TYPE   = 0;
    private static int FOOTER_VIEW_TYPE = 1;
    private CameraFragment mCameraFragment;
    private List<CameraPhoto> mPhotos;
    private final Context mContext;
    private boolean mLoading;
    private ItemTouchHelper mTouchHelper;

    private static final int vibrationDuration = 400;

    public CameraPhotosAdapter(Context context, CameraFragment cameraFragment) {
        mCameraFragment = cameraFragment;
        mPhotos         = new ArrayList<>();
        mContext        = context;
        mLoading        = false;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE) {
            return new ItemViewHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.camera_photo_recycler_view_item,
                    parent, false));
        } else if (viewType == FOOTER_VIEW_TYPE) {
            return new LoadingIconItemViewHolder(DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.card_show_taken_picture_loading_item_view_holder,
                    parent,
                    false));
        } else {
            throw new IllegalStateException("Invalid type, this type ot items " + viewType + " can't be handled");
        }
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

            final CameraPhoto cameraPhoto = mPhotos.get(position);

            itemViewHolder.mCameraPhotosRecyclerViewBinding.setHandler(this);

            configureDefaultConstraintLayoutTouchListener(itemViewHolder);

            itemViewHolder.updateView(cameraPhoto);
        } else if (viewHolder instanceof LoadingIconItemViewHolder) {
            LoadingIconItemViewHolder loadingIconItemViewHolder = (LoadingIconItemViewHolder) viewHolder;
            loadingIconItemViewHolder.showProgressBar();
        }
    }

    private void configureDefaultConstraintLayoutTouchListener(ItemViewHolder itemViewHolder) {
        itemViewHolder.mCameraPhotosRecyclerViewBinding.cameraPhotoViewItemPhotoCircularImageView.setOnLongClickListener(
                (view) -> {
                    mTouchHelper.startDrag(itemViewHolder);

                    return true;
                }
        );
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mPhotos.size() || isNotLoading()) {
            return ITEM_VIEW_TYPE;
        } else {
            return FOOTER_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        if (isNotLoading()) {
            return mPhotos.size();
        } else {
            return mPhotos.size() + 1;
        }
    }

    public void removePhoto(View view, CameraPhoto cameraPhoto) {
        int position = mPhotos.indexOf(cameraPhoto);
        mPhotos.remove(cameraPhoto);

        if (deleteFile(mContext, cameraPhoto.getLocalImageFilename())) {
            mCameraFragment.updateCounters();
        }

        notifyItemRemoved(position);
    }

    public void addAllPhotos(List<CameraPhoto> photos) {
        for (CameraPhoto photo :
                photos) {
            addPicture(photo, this::notifyItemInserted);
        }
    }

    public void addPicture(CameraPhoto cameraPhoto, CameraPhotoItemCallback itemCallback) {
        mPhotos.add(cameraPhoto);
        itemCallback.successAtPositionCallback(mPhotos.size());
    }

    public void showLoader(CameraPhotoItemCallback itemCallback) {
        mLoading = true;
        itemCallback.successAtPositionCallback(mPhotos.size() + 1);
    }

    public void hideLoader() {
        mLoading = false;
    }

    private boolean isNotLoading() {
        return !mLoading;
    }

    public List<CameraPhoto> getList() {
        return mPhotos;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        mTouchHelper = touchHelper;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        if (positionIsOnThePhotosArrayIndexBounds(newPosition) && positionIsOnThePhotosArrayIndexBounds(oldPosition)) {
            CameraPhoto targetCameraPhoto = mPhotos.get(oldPosition);

            mPhotos.remove(oldPosition);
            mPhotos.add(newPosition, targetCameraPhoto);

            notifyItemMoved(oldPosition, newPosition);
        }
    }

    private boolean positionIsOnThePhotosArrayIndexBounds(int position) {
        return position < mPhotos.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private CameraPhotoRecyclerViewItemBinding mCameraPhotosRecyclerViewBinding;

        ItemViewHolder(CameraPhotoRecyclerViewItemBinding cameraPhotosRecyclerViewBinding) {
            super(cameraPhotosRecyclerViewBinding.getRoot());
            mCameraPhotosRecyclerViewBinding = cameraPhotosRecyclerViewBinding;
        }

        void updateView(CameraPhoto cameraPhoto) {
            final Integer sampleSizeForSmallImages = 2;

            getBitmapFromFile(getPrivateTempDirectory(mContext), cameraPhoto.getLocalImageFilename(), sampleSizeForSmallImages,
                    new BitmapFromFileCallback() {
                        @Override
                        public void onBitmapDecoded(Bitmap bitmap) {
                            mCameraPhotosRecyclerViewBinding.cameraPhotoViewItemPhotoCircularImageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void fileNotFound() {

                        }
                    }
            );

            mCameraPhotosRecyclerViewBinding.setPhoto(cameraPhoto);
            mCameraPhotosRecyclerViewBinding.executePendingBindings();
            mCameraPhotosRecyclerViewBinding.cameraPhotoViewItemPhotoCircularImageView.setOnClickListener(
                    view -> mCameraFragment.showPreviewPicDialog(cameraPhoto)
            );
        }

        @Override
        public void onItemSelected() {
            VibratorUtils.vibrate(mContext, vibrationDuration);
            mCameraPhotosRecyclerViewBinding.cameraShowPhotoConstraintLayout.setAlpha(0.75f);
            mCameraPhotosRecyclerViewBinding.cameraPhotoViewItemCloseIconContainer.setVisibility(View.GONE);
        }

        @Override
        public void onItemClear() {
            mCameraPhotosRecyclerViewBinding.cameraShowPhotoConstraintLayout.setAlpha(1);
            mCameraPhotosRecyclerViewBinding.cameraPhotoViewItemCloseIconContainer.setVisibility(View.VISIBLE);
        }


    }


}
