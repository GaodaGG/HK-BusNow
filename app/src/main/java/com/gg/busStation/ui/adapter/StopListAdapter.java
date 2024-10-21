package com.gg.busStation.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.ui.layout.StopItemView;

import java.util.List;

public class StopListAdapter extends RecyclerView.Adapter<StopViewHolder> {

    private final List<StopItemData> mData;

    public StopListAdapter(List<StopItemData> data) {
        this.mData = data;
    }

    @NonNull
    @Override
    public StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        StopItemView itemView = new StopItemView(parent.getContext());

        return new StopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StopViewHolder holder, int position) {
        StopItemData stopItemData = mData.get(position);
        holder.getStopItemView().bindData(stopItemData);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
