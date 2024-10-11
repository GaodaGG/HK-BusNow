package com.gg.busStation.ui.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.R;
import com.gg.busStation.databinding.ItemBusBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.ui.fragment.StopBottomSheetDialog;

import java.util.List;

public class MainAdapter extends ListAdapter<ListItemData, MainViewHolder> {
    private final FragmentActivity mActivity;

    public MainAdapter(FragmentActivity context){
        super(DIFF_CALLBACK);
        this.mActivity = context;
    }

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

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        ItemBusBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_bus, parent, false);

        return new MainViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        ListItemData listItemData = getItem(position);
        holder.getBinding().setData(listItemData);

        holder.itemView.setOnClickListener(view -> {
            new StopBottomSheetDialog(listItemData).show(mActivity.getSupportFragmentManager(), StopBottomSheetDialog.TAG);
            DataBaseManager.addRoutesHistory(listItemData.getCo(), listItemData.getStopNumber(), listItemData.getBound(), listItemData.getService_type());

            //当正在搜索时不再更新列表
            MenuItem item = ((Toolbar) mActivity.findViewById(R.id.toolBar)).getMenu().findItem(R.id.search_toolbar_item);
            SearchView searchView = (SearchView) item.getActionView();
            if (searchView.getQuery().length() > 0) {
                return;
            }

            List<Route> routesHistory = DataBaseManager.getRoutesHistory();
            submitList(BusDataManager.routesToListItemData(routesHistory));
        });
    }

    @Override
    public void submitList(@Nullable List<ListItemData> list) {
        super.submitList(list);

        View errorView = mActivity.findViewById(R.id.main_error_layout);
        if (errorView == null) return;
        if (list == null || list.isEmpty()) {
            errorView.setVisibility(View.VISIBLE);
        } else {
            errorView.setVisibility(View.GONE);
        }
    }
}
