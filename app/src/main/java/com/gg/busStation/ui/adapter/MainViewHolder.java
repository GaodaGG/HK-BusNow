package com.gg.busStation.ui.adapter;


import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.ui.layout.ListItemView;

public class MainViewHolder extends RecyclerView.ViewHolder{
    private final ListItemView view;

    public MainViewHolder(ListItemView view) {
        super(view);
        this.view = view;
    }

    public ListItemView getView() {
        return view;
    }
}
