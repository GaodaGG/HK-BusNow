package com.gg.busStation.ui.fragment;

import static com.gg.busStation.function.BusDataManager.findNearestStopIndex;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Arrays;
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

            initData(route, data);

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
                    RecyclerView.ViewHolder holder = dialogList.findViewHolderForAdapterPosition(finalNearestStopIndex);
                    if (holder == null) return;
                    StopItemView view = (StopItemView) holder.itemView;
                    view.post(view::performClick);
                });
            });
        }).start();
    }

    private void initData(Route route, List<StopItemData> data) {
        try {
            mStops = BusDataManager.routeToStops(route);

            //获取车费
            String[] stopFares = new String[mStops.size()];
            String fare = DataBaseManager.findFare(route.getRoute(), route.getBound());
            String[] fares = fare.split(";");
            for (String s : fares) {
                String[] fareData = s.split(",");
                String[] pickStopRange = fareData[0].split("-");
                int start = Integer.parseInt(pickStopRange[0]);
                int end = Integer.parseInt(pickStopRange[1]);
                if (end > mStops.size()) {
                    Arrays.fill(stopFares, "");
                    break;
                }

                for (int i = start; i <= end; i++) {
                    if (i - 1 >= mStops.size()) {
                        stopFares[mStops.size() - 1] = "";
                        break;
                    }
                    stopFares[i - 1] = fareData[1] + " HKD";
                }
            }

            for (int i = 0; i < mStops.size(); i++) {
                Stop stop = mStops.get(i);
                StopItemData stopItemData = new StopItemData(String.valueOf(i + 1), stop.getName("zh_CN"), stopFares[i], route.getBound(), route.getService_type(), route.getCo(), route.getRoute(), stop.getStop());
                data.add(stopItemData);
            }
        } catch (IOException e) {
            Log.e(TAG, "initView: ", e);
        }
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
