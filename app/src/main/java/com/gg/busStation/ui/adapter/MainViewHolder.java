package com.gg.busStation.ui.adapter;


import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.databinding.ItemBusBinding;

public class MainViewHolder extends RecyclerView.ViewHolder{
    private final ItemBusBinding binding;

    public MainViewHolder(ItemBusBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public ItemBusBinding getBinding() {
        return binding;
    }
}
