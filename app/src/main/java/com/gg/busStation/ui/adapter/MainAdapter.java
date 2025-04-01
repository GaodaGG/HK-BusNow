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
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.ui.fragment.StopBottomSheetDialog;
import com.gg.busStation.ui.layout.ListItemView;

import java.util.List;

public class MainAdapter extends ListAdapter<ListItemData, MainAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<ListItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItemData oldItem, @NonNull ListItemData newItem) {
            return oldItem.getStopNumber().equals(newItem.getStopNumber()) &&
                    oldItem.getCo().equals(newItem.getCo()) &&
                    oldItem.getBound().equals(newItem.getBound()) &&
                    oldItem.getService_type().equals(newItem.getService_type());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItemData oldItem, @NonNull ListItemData newItem) {
            return oldItem.equals(newItem);
        }
    };
    private final FragmentActivity mActivity;
    private final boolean isSearch;

    public MainAdapter(FragmentActivity context, boolean isSearch) {
        super(DIFF_CALLBACK);
        this.mActivity = context;
        this.isSearch = isSearch;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TypedArray typedArray = mActivity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
        Drawable itemForeground = typedArray.getDrawable(0);
        typedArray.recycle();

        ListItemView listItemView = new ListItemView(mActivity);
        listItemView.setForeground(itemForeground);
        return new ViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListItemData listItemData = getItem(position);
        holder.getView().setData(listItemData);

        holder.itemView.setOnClickListener(view -> {
            new StopBottomSheetDialog(listItemData).show(mActivity.getSupportFragmentManager(), StopBottomSheetDialog.TAG);
            DataBaseManager.addRoutesHistory(listItemData.getCo(), listItemData.getStopNumber(), listItemData.getBound(), listItemData.getService_type());
        });

        if (!isSearch) {
            holder.itemView.findViewById(R.id.more_button).setOnClickListener(view -> showMenu(view, listItemData));
            //android:foreground="?attr/selectableItemBackgroundBorderless"
            TypedArray typedArray = mActivity.getTheme().obtainStyledAttributes(R.style.Theme_BusStation, new int[]{com.google.android.material.R.attr.selectableItemBackgroundBorderless});
            holder.itemView.findViewById(R.id.more_button).setForeground(AppCompatResources.getDrawable(mActivity, typedArray.getResourceId(0, 0)));
            typedArray.recycle();
        }
    }


    @Override
    public void onCurrentListChanged(@NonNull List<ListItemData> previousList, @NonNull List<ListItemData> currentList) {
        super.onCurrentListChanged(previousList, currentList);
        if (previousList.isEmpty() || previousList.equals(currentList)) return;
        ((RecyclerView) mActivity.findViewById(R.id.bus_list_view)).scrollToPosition(0);
    }

    //TODO 删除最后一个item1后通知fragment显示提示
    private void showMenu(View moreButtion, ListItemData listItemData) {
        PopupMenu popupMenu = new PopupMenu(mActivity, moreButtion);
        popupMenu.inflate(R.menu.bus_item_menu);

        String co = listItemData.getCo();
        String stopNumber = listItemData.getStopNumber();
        String bound = listItemData.getBound();
        String serviceType = listItemData.getService_type();
        if (DataBaseManager.isPinRoutesHistory(co, stopNumber, bound, serviceType)) {
            popupMenu.getMenu().findItem(R.id.bus_menu_pin).setVisible(false);
            popupMenu.getMenu().findItem(R.id.bus_menu_unpin).setVisible(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bus_menu_pin) {
                DataBaseManager.pinRoutesHistory(co, stopNumber, bound, serviceType);

                refreshList();
                return true;
            } else if (itemId == R.id.bus_menu_unpin) {
                DataBaseManager.unpinRoutesHistory(co, stopNumber, bound, serviceType);

                refreshList();
                return true;
            } else if (itemId == R.id.bus_menu_delete) {
                DataBaseManager.deleteRoutesHistory(co, stopNumber, bound, serviceType);

                refreshList();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    public void refreshList() {
        List<Route> routes = DataBaseManager.getRoutesHistory();
        List<ListItemData> data = BusDataManager.routesToListItemData(routes);
        submitList(data);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemView view;

        public ViewHolder(ListItemView view) {
            super(view);
            this.view = view;
        }

        public ListItemView getView() {
            return view;
        }
    }
}
