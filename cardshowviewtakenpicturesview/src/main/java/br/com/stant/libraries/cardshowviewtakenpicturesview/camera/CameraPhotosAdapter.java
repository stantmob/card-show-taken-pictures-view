package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoRecyclerViewItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageDecoder;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.ImageViewFileUtil;

public class CameraPhotosAdapter extends RecyclerView.Adapter<CameraPhotosAdapter.ItemViewHolder> {

    private CameraFragment mCameraFragment;
    private ItemViewHolder mViewHolder;
    private Context mContext;
    private List<CameraPhoto> mPhotos;

    public CameraPhotosAdapter(Context context, CameraFragment cameraFragment) {
        this.mContext        = context;
        this.mCameraFragment = cameraFragment;
        this.mPhotos         = new ArrayList<>();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.camera_photo_recycler_view_item,
                parent, false));
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        mViewHolder = holder;
        final CameraPhoto cameraPhoto = mPhotos.get(position);

        mViewHolder.mCameraPhotosRecyclerViewBinding.setHandler(this);

        holder.updateView(cameraPhoto);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public void removePhoto(View view, CameraPhoto cameraPhoto){
        int position = mPhotos.indexOf(cameraPhoto);
        mPhotos.remove(cameraPhoto);
        File file = new File(ImageViewFileUtil.getFile().toString() + "/" + cameraPhoto.getLocalImageFilename());
        if(file.delete()){
            mCameraFragment.updateCounters();
        }
        notifyItemRemoved(position);
    }

    public void addPhoto(CameraPhoto cameraPhoto){
        mPhotos.add(cameraPhoto);
        notifyItemInserted(mPhotos.size());
    }

    public void addAllPhotos(List<CameraPhoto> photos) {
        for (CameraPhoto photo:
             photos) {
            addPhoto(photo);
        }
    }

    private Bitmap getLocalImage(CameraPhoto cameraPhoto) {
        if (hasLocalImage(cameraPhoto)) {
            return ImageDecoder.getBitmapFromFile(ImageViewFileUtil.getFile(), cameraPhoto.getLocalImageFilename(),2);
        }

        return null;
    }

    private boolean hasLocalImage(CameraPhoto cameraPhoto) {
        return cameraPhoto.getLocalImageFilename() != null;
    }

    public void addPicture(CameraPhoto cardShowTakenImage){
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
            this.mCameraPhotosRecyclerViewBinding.cardShowTakenPictureViewGeneralCircularImageView.setImageBitmap(getLocalImage(cameraPhoto));
            this.mCameraPhotosRecyclerViewBinding.setPhoto(cameraPhoto);
            this.mCameraPhotosRecyclerViewBinding.executePendingBindings();
            this.mCameraPhotosRecyclerViewBinding.
                    cardShowTakenPictureViewGeneralCircularImageView.setOnClickListener(
                    view -> mCameraFragment.showPreviewPicDialog(cameraPhoto));
        }
    }
}