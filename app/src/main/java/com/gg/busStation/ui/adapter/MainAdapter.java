package com.gg.busStation.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.R;
import com.gg.busStation.databinding.ItemBusBinding;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.ui.fragment.StopBottomSheetDialog;

import java.util.List;

public class MainAdapter extends ListAdapter<ListItemData, MainAdapter.ViewHolder> {
    private final Activity mActivity;

    public MainAdapter(Activity context){
        super(DIFF_CALLBACK);
        this.mActivity = context;
    }

    private static final DiffUtil.ItemCallback<ListItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItemData oldItem, @NonNull ListItemData newItem) {
            return oldItem.getStopNumber().equals(newItem.getStopNumber());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItemData oldItem, @NonNull ListItemData newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        ItemBusBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_bus, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItemData listItemData = getItem(position);
        holder.getBinding().setData(listItemData);

        holder.itemView.setOnClickListener(view -> {
            new StopBottomSheetDialog(listItemData).show(((AppCompatActivity) mActivity).getSupportFragmentManager(), StopBottomSheetDialog.TAG);
            DataBaseManager.addRoutesHistory(listItemData.getStopNumber(), listItemData.getBound(), listItemData.getService_type());
        });
    }

    @Override
    public void submitList(@Nullable List<ListItemData> list) {
        super.submitList(list);
        if (list == null || list.isEmpty()) {
            mActivity.findViewById(R.id.main_error_layout).setVisibility(View.VISIBLE);
        } else {
            mActivity.findViewById(R.id.main_error_layout).setVisibility(View.GONE);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
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
