package com.gg.busStation.ui.adapter;

import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.function.NotificationHelper;
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

        holder.stopItemView.findViewById(R.id.more_button).setOnClickListener(this::showMenu);
        TypedArray typedArray = mActivity.getTheme().obtainStyledAttributes(R.style.Theme_BusStation, new int[]{com.google.android.material.R.attr.selectableItemBackgroundBorderless});
        holder.stopItemView.findViewById(R.id.more_button).setForeground(AppCompatResources.getDrawable(mActivity, typedArray.getResourceId(0, 0)));
        typedArray.recycle();
    }

    private void showMenu(View moreButtion) {
        PopupMenu popupMenu = new PopupMenu(mActivity, moreButtion);
        popupMenu.inflate(R.menu.stop_item_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.stop_menu_arrival) {
                NotificationHelper.postNotification(mActivity, 0, NotificationHelper.getChannelIDs()[0], "Just test", "just test", R.drawable.ic_launcher_foreground);
                return true;
            }

            return false;
        });

        popupMenu.show();
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
