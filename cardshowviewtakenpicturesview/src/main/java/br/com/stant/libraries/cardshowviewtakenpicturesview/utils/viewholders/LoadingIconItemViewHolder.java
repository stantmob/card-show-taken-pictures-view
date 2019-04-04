package br.com.stant.libraries.cardshowviewtakenpicturesview.utils.viewholders;

import android.support.v7.widget.RecyclerView;

import br.com.stant.libraries.cardshowviewtakenpicturesview.databinding.LoadingIconItemViewHolderBinding;

public class LoadingIconItemViewHolder extends RecyclerView.ViewHolder {

    private LoadingIconItemViewHolderBinding mLoadingIconItemViewHolderBinding;

    public LoadingIconItemViewHolder(LoadingIconItemViewHolderBinding loadingIconItemViewHolderBinding) {
        super(loadingIconItemViewHolderBinding.getRoot());
        mLoadingIconItemViewHolderBinding = loadingIconItemViewHolderBinding;
    }

    public void showProgressBar() {
        mLoadingIconItemViewHolderBinding.progressBar.show();
        mLoadingIconItemViewHolderBinding.executePendingBindings();
    }

    public void hideProgressBar() {
        mLoadingIconItemViewHolderBinding.progressBar.hide();
        mLoadingIconItemViewHolderBinding.executePendingBindings();
    }


}