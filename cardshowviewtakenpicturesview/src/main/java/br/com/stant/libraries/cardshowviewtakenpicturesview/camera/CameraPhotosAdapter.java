package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoRecyclerViewItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.BitmapFromFileCallback;

import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder.getBitmapFromFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.deleteFile;
import static br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil.getPrivateTempDirectory;

public class CameraPhotosAdapter extends RecyclerView.Adapter<CameraPhotosAdapter.ItemViewHolder> {

    private CameraFragment mCameraFragment;
    private List<CameraPhoto> mPhotos;
    private final Context mContext;

    public CameraPhotosAdapter(Context context, CameraFragment cameraFragment) {
        this.mCameraFragment = cameraFragment;
        this.mPhotos         = new ArrayList<>();
        this.mContext        = context;
    }

    @NotNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.camera_photo_recycler_view_item,
                parent, false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NotNull ItemViewHolder holder, int position) {
        final CameraPhoto cameraPhoto = mPhotos.get(position);

        holder.mCameraPhotosRecyclerViewBinding.setHandler(this);

        holder.updateView(cameraPhoto);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public void removePhoto(View view, CameraPhoto cameraPhoto) {
        int position = mPhotos.indexOf(cameraPhoto);
        mPhotos.remove(cameraPhoto);

        if (deleteFile(mContext, cameraPhoto.getLocalImageFilename())) {
            mCameraFragment.updateCounters();
        }

        notifyItemRemoved(position);
    }

    public void addPhoto(CameraPhoto cameraPhoto) {
        mPhotos.add(cameraPhoto);
        notifyItemInserted(mPhotos.size());
    }

    public void addAllPhotos(List<CameraPhoto> photos) {
        for (CameraPhoto photo :
                photos) {
            addPhoto(photo);
        }
    }

    public void addPicture(CameraPhoto cardShowTakenImage) {
        mPhotos.add(cardShowTakenImage);
        notifyItemInserted(mPhotos.size());
    }

    public List<CameraPhoto> getList() {
        return mPhotos;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private CameraPhotoRecyclerViewItemBinding mCameraPhotosRecyclerViewBinding;

        ItemViewHolder(CameraPhotoRecyclerViewItemBinding cameraPhotosRecyclerViewBinding) {
            super(cameraPhotosRecyclerViewBinding.getRoot());
            this.mCameraPhotosRecyclerViewBinding = cameraPhotosRecyclerViewBinding;
        }

        void updateView(CameraPhoto cameraPhoto) {
            final Integer sampleSizeForSmallImages = 2;

            getBitmapFromFile(getPrivateTempDirectory(mContext), cameraPhoto.getLocalImageFilename(), sampleSizeForSmallImages,
                    new BitmapFromFileCallback() {
                        @Override
                        public void onBitmapDecoded(Bitmap bitmap) {
                            mCameraPhotosRecyclerViewBinding.cardShowTakenPictureViewGeneralCircularImageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void fileNotFound() {

                        }
                    }
            );

            this.mCameraPhotosRecyclerViewBinding.setPhoto(cameraPhoto);
            this.mCameraPhotosRecyclerViewBinding.executePendingBindings();
            this.mCameraPhotosRecyclerViewBinding.cardShowTakenPictureViewGeneralCircularImageView.setOnClickListener(
                    view -> mCameraFragment.showPreviewPicDialog(cameraPhoto)
            );
        }


    }


}
