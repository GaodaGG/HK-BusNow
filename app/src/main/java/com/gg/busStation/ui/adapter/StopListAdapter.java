package com.gg.busStation.ui.adapter;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.data.reminder.ReminderData;
import com.gg.busStation.function.NotificationHelper;
import com.gg.busStation.ui.dialog.ReminderDialog;
import com.gg.busStation.ui.layout.StopItemView;

import java.util.List;
import java.util.Objects;

public class StopListAdapter extends ListAdapter<StopItemData, StopListAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<StopItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull StopItemData oldItem, @NonNull StopItemData newItem) {
            return oldItem.getStopNumber().equals(newItem.getStopNumber()) &&
                    oldItem.getCo().equals(newItem.getCo()) &&
                    Objects.equals(oldItem.getBound(), newItem.getBound());
        }

        @Override
        public boolean areContentsTheSame(@NonNull StopItemData oldItem, @NonNull StopItemData newItem) {
            return oldItem.equals(newItem);
        }
    };

    private final FragmentActivity mActivity;
    private String mRouteName = "";
    private final Drawable moreButtonForeground;

    public StopListAdapter(FragmentActivity activity) {
        super(DIFF_CALLBACK);
        this.mActivity = activity;

        TypedArray typedArray = mActivity.getTheme().obtainStyledAttributes(R.style.Theme_BusStation, new int[]{android.R.attr.selectableItemBackgroundBorderless});
        moreButtonForeground = AppCompatResources.getDrawable(mActivity, typedArray.getResourceId(0, 0));
        typedArray.recycle();
    }

    public void setRouteName(String routeName) {
        this.mRouteName = routeName;
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

        holder.stopItemView.findViewById(R.id.more_button).setOnClickListener(v ->
                showMenu(v, holder.stopItemView));
        holder.stopItemView.findViewById(R.id.more_button).setForeground(moreButtonForeground);
    }

    private void showMenu(View moreButton, StopItemView stopItemView) {
        PopupMenu popupMenu = new PopupMenu(mActivity, moreButton);
        popupMenu.inflate(R.menu.stop_item_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // 获取站点数据和ETA数据
            StopItemData stopData = stopItemView.getStopData();
            List<ETA> etaList = stopItemView.getLastEtaList();
            String routeName = mRouteName;           // 路线号
            String stopName = stopData.getHeadline(); // 站点名称

            if (itemId == R.id.stop_menu_boarding) {
                // 检查通知权限
                if (!NotificationHelper.checkPermission(mActivity)) {
                    NotificationHelper.registerPermission(mActivity);
                    return true;
                }

                // 显示上车提醒对话框
                ReminderDialog dialog = new ReminderDialog(
                        mActivity,
                        ReminderData.TYPE_BOARDING,
                        routeName,
                        stopName,
                        stopData,
                        etaList
                );
                dialog.show();
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
