package br.com.stant.libraries.cardshowviewtakenpicturesview.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;
import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CameraPhotoRecyclerViewItemBinding;
import br.com.stant.libraries.cardshowviewtakenpicturesview.domain.model.CameraPhoto;
import br.com.stant.libraries.cardshowviewtakenpicturesview.utils.PhotoViewFileUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CameraPhotosAdapter extends RecyclerView.Adapter<CameraPhotosAdapter.ItemViewHolder> {

    private ItemViewHolder mViewHolder;
    private Context mContext;
    private List<CameraPhoto> mPhotos;
    private List<CameraPhoto> mPhotosAsAdded;

    public CameraPhotosAdapter(Context context) {
        this.mContext = context;
        this.mPhotos  = new ArrayList<>();
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

        Observable.fromCallable(
                () -> {
                    cameraPhoto.setTempImagePathToShow(getImage(cameraPhoto));
                    return cameraPhoto;
                }
        ).observeOn(Schedulers.newThread())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(
                (photo) -> mViewHolder.mCameraPhotosRecyclerViewBinding.setPhoto(photo)
        );

//        mViewHolder.mCameraPhotosRecyclerViewBinding.cardShowTakenPictureViewGeneralCircularImageView.setImageBitmap(cameraPhoto.getBitmapImageFromIntentPath());
        mViewHolder.mCameraPhotosRecyclerViewBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public void saveEditData(){
        notifyDataSetChanged();
    }

    public void replaceData(List<CameraPhoto> photos) {
        mPhotos = photos;
        notifyDataSetChanged();
    }

    public List<CameraPhoto> getPhotosAsAdded(){
        return mPhotosAsAdded;
    }

    private String getImage(CameraPhoto cameraPhoto) {
        if (hasLocalImage(cameraPhoto)) {
            return getTempImageFileToShowFromLocalImageFilename(cameraPhoto.getLocalImageFilename());
        }

        return null;
    }

    private boolean hasLocalImage(CameraPhoto cameraPhoto) {
        return cameraPhoto.getLocalImageFilename() != null;
    }

    private String getTempImageFileToShowFromLocalImageFilename(String localImageFilename){
        String tempImageFilePath;
        Bitmap bitmap = PhotoViewFileUtil.getBitMapFromFile(localImageFilename, PhotoViewFileUtil.getFile());

        tempImageFilePath = MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                bitmap, "temp_image_stant", null);

        return tempImageFilePath;
    }

    public void addPicture(CameraPhoto cardShowTakenImage){
        mPhotos.add(cardShowTakenImage);
        notifyItemInserted(mPhotos.size());
    }

    public void addPictures(ArrayList<CameraPhoto> photos){
        mPhotos.addAll(photos);
        mPhotosAsAdded.addAll(photos);
        replaceData(mPhotos);
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
    }
}