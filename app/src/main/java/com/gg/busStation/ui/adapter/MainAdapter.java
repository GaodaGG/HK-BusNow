package com.gg.busStation.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.R;
import com.gg.busStation.databinding.ItemBusBinding;
import com.gg.busStation.ui.fragment.StopBottomSheetDialog;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private final Activity mActivity;
    private List<ListItemData> mData;

    public MainAdapter(List<ListItemData> data, Activity context){
        this.mData = data;
        this.mActivity = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        ItemBusBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_bus, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItemData listItemData = mData.get(position);
        holder.getBinding().setData(listItemData);

        holder.itemView.setOnClickListener(view -> new StopBottomSheetDialog(listItemData).show(((AppCompatActivity)mActivity).getSupportFragmentManager(), StopBottomSheetDialog.TAG));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final ItemBusBinding binding;

        public ViewHolder(ItemBusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemBusBinding getBinding() {
            return binding;
        }
    }
}
