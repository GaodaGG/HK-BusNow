package com.gg.busStation.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.ui.layout.StopItemView;

public class StopViewHolder extends RecyclerView.ViewHolder {
    private final StopItemView stopItemView;

    public StopViewHolder(@NonNull StopItemView itemView) {
        super(itemView);
        stopItemView = itemView;
    }

    public StopItemView getStopItemView() {
        return stopItemView;
    }
}
