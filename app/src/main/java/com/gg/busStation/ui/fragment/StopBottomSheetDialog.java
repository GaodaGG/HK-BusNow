package com.gg.busStation.ui.fragment;

import static com.gg.busStation.function.BusDataManager.findNearestStopIndex;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.databinding.DialogBusBinding;
import com.gg.busStation.function.location.LocationHelper;
import com.gg.busStation.ui.adapter.StopListAdapter;
import com.gg.busStation.ui.layout.StopItemView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StopBottomSheetDialog extends BottomSheetDialogFragment {
    public static final String TAG = "BusBottomSheetDialog";
    private DialogBusBinding binding;
    private ListItemData mData;
    private List<Stop> mStops;

    public StopBottomSheetDialog(ListItemData listItemData) {
        mData = listItemData;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        binding = DialogBusBinding.inflate(getLayoutInflater());
        binding.setData(mData);
        dialog.setContentView(binding.getRoot());

        initView();
        return dialog;
    }

    public void initView() {
        new Thread(() -> {
            List<StopItemData> data = new ArrayList<>();
            Route route = DataBaseManager.findRoute(mData.getCo(), mData.getStopNumber(), mData.getBound(), mData.getService_type());

            try {
                mStops = BusDataManager.routeToStops(route);

                for (int i = 0; i < mStops.size(); i++) {
                    Stop stop = mStops.get(i);
                    StopItemData stopItemData = new StopItemData(String.valueOf(i + 1), stop.getName("zh_CN"), "", route.getBound(), route.getService_type(), route.getCo(), route.getRoute(), stop.getStop());
                    data.add(stopItemData);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            int nearestStopIndex = 0;

            if (!isAdded()) return;
            LatLng location = LocationHelper.getLocation(false);
            if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                nearestStopIndex = findNearestStopIndex(mStops, location);
            }

            StopListAdapter stopListAdapter = new StopListAdapter(data);
            LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);

            manager.setInitialPrefetchItemCount(10);
            divider.setLastItemDecorated(false);

            int finalNearestStopIndex = nearestStopIndex;
            requireActivity().runOnUiThread(() -> {
                RecyclerView dialogList = binding.dialogList;
                dialogList.setLayoutManager(manager);
                dialogList.addItemDecoration(divider);
                dialogList.setItemAnimator(null);
                dialogList.setAdapter(stopListAdapter);

                //跳转到最近的巴士站,并隐藏loading
                ((LinearLayoutManager) dialogList.getLayoutManager()).scrollToPositionWithOffset(finalNearestStopIndex, 0);
                dialogList.post(() -> {
                    binding.dialogLoading.setVisibility(View.GONE);
                    StopItemView view = (StopItemView) dialogList.findViewHolderForAdapterPosition(finalNearestStopIndex).itemView;
                    view.post(view::performClick);
                });
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        RecyclerView dialogList = binding.dialogList;
        for (int i = 0; i < dialogList.getChildCount(); i++) {
            StopItemView view = (StopItemView) dialogList.getChildAt(i);
            if (view.isOpen()) view.updateTime(view.getContext(), true);
        }
    }
}
