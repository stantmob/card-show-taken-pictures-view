package br.com.stant.libraries.cameraimagegalleryview.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.stant.libraries.cardshowviewtakenpicturesview.R;

public class PopUpErrorsAdapter extends RecyclerView.Adapter<ErrorItem> {

    private Context mContext;
    private List<String> mErrors;

    public PopUpErrorsAdapter(Context context, List<String> errors){
        this.mContext = context;
        this.mErrors = errors;
    }

    @NonNull
    @Override
    public ErrorItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.errors_item_recycler_view, parent, false);
        return new ErrorItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorItem holder, int position) {
        holder.setError(mErrors.get(position));
    }

    @Override
    public int getItemCount() {
        return mErrors.size();
    }
}
class ErrorItem extends RecyclerView.ViewHolder{

    private final TextView errorTextView;
    public ErrorItem(@NonNull View itemView) {
        super(itemView);
        errorTextView = itemView.findViewById(R.id.error_item);
    }
    public void setError(String error){
        errorTextView.setText(error);
    }
}