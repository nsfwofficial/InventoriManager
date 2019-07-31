package id.web.dmalvian.invman.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import id.web.dmalvian.invman.R;
import id.web.dmalvian.invman.model.Tool;

public class GridAdapter extends FirestoreRecyclerAdapter<Tool, GridAdapter.GridViewHolder> {
    private Context mContext;
    private GridAdapter.OnItemClickListener listener;

    public GridAdapter(@NonNull FirestoreRecyclerOptions<Tool> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull GridViewHolder holder, int position, @NonNull Tool model) {
        holder.tvTitle.setText(model.getTitle());
        holder.tvStockCode.setText(model.getStockCode());
        holder.tvCategory.setText(model.getCategory());
        String key = (String) model.getImages().keySet().toArray()[0];
        Glide.with(mContext)
                .load(model.getImages().get(key))
                .into(holder.imgInv);
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_tool, parent, false);
        this.mContext = parent.getContext();
        return new GridAdapter.GridViewHolder(v);
    }

    public class GridViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvStockCode;
        TextView tvCategory;
        ImageView imgInv;

        public GridViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvStockCode = itemView.findViewById(R.id.tv_stock_code);
            tvCategory = itemView.findViewById(R.id.tv_category);
            imgInv = itemView.findViewById(R.id.img_inv_list);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int postition);
    }

    public void setOnItemClickListener(GridAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
