package br.com.stant.libraries.cardshowviewtakenpicturesview.utils.viewholders;

import androidx.recyclerview.widget.RecyclerView;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.CardShowTakenPictureLoadingItemViewHolderBinding;

public class LoadingIconItemViewHolder extends RecyclerView.ViewHolder {

    private CardShowTakenPictureLoadingItemViewHolderBinding mBinding;

    public LoadingIconItemViewHolder(CardShowTakenPictureLoadingItemViewHolderBinding cardShowTakenPictureLoadingItemViewHolderBinding) {
        super(cardShowTakenPictureLoadingItemViewHolderBinding.getRoot());
        mBinding = cardShowTakenPictureLoadingItemViewHolderBinding;
    }

    public void showProgressBar() {
        mBinding.progressBar.show();
        mBinding.executePendingBindings();
    }

    public void hideProgressBar() {
        mBinding.progressBar.hide();
        mBinding.executePendingBindings();
    }


}