package com.gg.busStation.ui.adapter;

import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
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
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FeatureDAO;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.database.dao.HistoryDAO;
import com.gg.busStation.function.database.dao.HistoryDAOImpl;
import com.gg.busStation.ui.fragment.StopBottomSheetDialog;
import com.gg.busStation.ui.layout.ListItemView;

import java.util.List;

public class MainAdapter extends ListAdapter<ListItemData, MainAdapter.ViewHolder> {
    private static final DiffUtil.ItemCallback<ListItemData> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListItemData oldItem, @NonNull ListItemData newItem) {
            return oldItem.getStopNumber().equals(newItem.getStopNumber()) &&
                    oldItem.getCo().equals(newItem.getCo()) &&
                    oldItem.getRouteSeq() == newItem.getRouteSeq() &&
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
//            DataBaseManager.addRoutesHistory(listItemData.getCo(), listItemData.getStopNumber(), listItemData.getBound(), listItemData.getService_type());
            DataBaseHelper dbHelper = DataBaseHelper.getInstance(mActivity);
            HistoryDAO historyDAO = new HistoryDAOImpl(dbHelper.getDatabase());
            historyDAO.insert(listItemData.getRouteId(), listItemData.getRouteSeq(), false);
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

    private void showMenu(View moreButtion, ListItemData listItemData) {
        PopupMenu popupMenu = new PopupMenu(mActivity, moreButtion);
        popupMenu.inflate(R.menu.bus_item_menu);

        int routeId = listItemData.getRouteId();
        int routeSeq = listItemData.getRouteSeq();

        SQLiteDatabase database = DataBaseHelper.getInstance(mActivity).getDatabase();
        HistoryDAO historyDAO = new HistoryDAOImpl(database);

        if (historyDAO.getPinnedIndex(routeId, routeSeq) != 0) {
            popupMenu.getMenu().findItem(R.id.bus_menu_pin).setVisible(false);
            popupMenu.getMenu().findItem(R.id.bus_menu_unpin).setVisible(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bus_menu_pin) {
                historyDAO.pinHistory(routeId, routeSeq);

                refreshList();
                return true;
            } else if (itemId == R.id.bus_menu_unpin) {
                historyDAO.unpinHistory(routeId, routeSeq);

                refreshList();
                return true;
            } else if (itemId == R.id.bus_menu_delete) {
                historyDAO.delete(routeId, routeSeq);

                refreshList();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    public void refreshList() {
        SQLiteDatabase database = DataBaseHelper.getInstance(mActivity).getDatabase();
        HistoryDAO historyDAO = new HistoryDAOImpl(database);
        FeatureDAO featureDAO = new FeatureDAOImpl(database);
        List<Route> allHistory = historyDAO.getAllHistory();
        List<ListItemData> data = BusDataManager.routesToListItemData(allHistory, featureDAO);
        submitList(data);

        if (data.isEmpty()) {
            mActivity.findViewById(R.id.main_error_layout).setVisibility(View.VISIBLE);
        } else {
            mActivity.findViewById(R.id.main_error_layout).setVisibility(View.GONE);
        }
    }

    @lombok.Getter
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemView view;

        public ViewHolder(ListItemView view) {
            super(view);
            this.view = view;
        }

    }
}
