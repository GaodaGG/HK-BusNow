package com.gg.busStation.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.ui.layout.StopItemView;

public class StopListAdapter extends ListAdapter<StopItemData, StopListAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<StopItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull StopItemData oldItem, @NonNull StopItemData newItem) {
            return oldItem.getStopNumber().equals(newItem.getStopNumber()) &&
                    oldItem.getCo().equals(newItem.getCo()) &&
                    oldItem.getBound().equals(newItem.getBound()) &&
                    oldItem.getService_type().equals(newItem.getService_type());
        }

        @Override
        public boolean areContentsTheSame(@NonNull StopItemData oldItem, @NonNull StopItemData newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final FragmentActivity mActivity;

    public StopListAdapter(FragmentActivity activity) {
        super(DIFF_CALLBACK);
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        StopItemView view = new StopItemView(mActivity);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StopItemData stopItemData = getItem(position);
        ((StopItemView) holder.itemView).setData(stopItemData);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final StopItemView stopItemView;

        public ViewHolder(StopItemView view) {
            super(view);
            this.stopItemView = view;
        }

        public StopItemView getView() {
            return stopItemView;
        }
    }
}
