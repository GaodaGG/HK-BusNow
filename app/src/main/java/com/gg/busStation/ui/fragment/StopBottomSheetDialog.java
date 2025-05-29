package com.gg.busStation.ui.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.model.LatLng;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.databinding.DialogBusBinding;
import com.gg.busStation.function.Tools;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FareDAOImpl;
import com.gg.busStation.function.database.dao.RouteDAO;
import com.gg.busStation.function.database.dao.RouteDAOImpl;
import com.gg.busStation.function.database.dao.StopDAO;
import com.gg.busStation.function.database.dao.StopDAOImpl;
import com.gg.busStation.function.feature.CompanyManager;
import com.gg.busStation.function.feature.FareManager;
import com.gg.busStation.function.feature.FeatureManager;
import com.gg.busStation.function.location.LocationHelper;
import com.gg.busStation.ui.adapter.StopListAdapter;
import com.gg.busStation.ui.layout.StopItemView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StopBottomSheetDialog extends BottomSheetDialogFragment {
    public static final String TAG = "BusBottomSheetDialog";
    private DialogBusBinding binding;
    private ListItemData mData;
    private final List<Stop> mStops = new ArrayList<>();

    public StopBottomSheetDialog(ListItemData listItemData) {
        mData = listItemData;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && mData == null) {
            mData = savedInstanceState.getParcelable("data");
        }

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        binding = DialogBusBinding.inflate(getLayoutInflater());
        binding.setData(mData);
        dialog.setContentView(binding.getRoot());
        initView();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = requireDialog().getWindow();

        if (Tools.isMIUI() && window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("data", mData);
    }

    public void initView() {
        new Thread(() -> {
            SQLiteDatabase database = DataBaseHelper.getInstance(requireContext()).getDatabase();
            RouteDAO routeDAO = new RouteDAOImpl(database);
            List<Route> routes = routeDAO.getRoutes(mData.getRouteId(), mData.getRouteSeq());

            List<StopItemData> data = initData(routes, mData.getCo(), database);

            int nearestStopIndex = 0;

            if (!isAdded()) return;
            LatLng location = LocationHelper.getLocation(false);
            if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                nearestStopIndex = FeatureManager.findNearestStopIndex(mStops, location);
            }

            StopListAdapter stopListAdapter = new StopListAdapter(requireActivity());
            stopListAdapter.submitList(data);
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

                binding.dialogHeadline.requestFocus();

                //跳转到最近的巴士站,并隐藏loading
                LinearLayoutManager layoutManager = (LinearLayoutManager) dialogList.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }

                layoutManager.scrollToPositionWithOffset(finalNearestStopIndex, 0);
                dialogList.post(() -> {
                    binding.dialogLoading.setVisibility(View.GONE);
                    RecyclerView.ViewHolder holder = dialogList.findViewHolderForAdapterPosition(finalNearestStopIndex);

                    // 检查是否存在对应的公司代码类
                    try {
                        Class.forName("com.gg.busStation.function.feature.co." + getCompanyCode(mData.getCo()));
                    } catch (ClassNotFoundException e) {
                        return;
                    }

                    if (holder == null) {
                        return;
                    }
                    StopItemView view = (StopItemView) holder.itemView;
                    view.post(view::performClick);
                });
            });
        }).start();
    }

    private String getCompanyCode(String companyCode) {
        if (companyCode.equals(CompanyManager.CompanyEnum.KMB_CTB.getCode())) {
            companyCode = "KMBCTB";
        } else if (companyCode.equals(CompanyManager.CompanyEnum.LWB_CTB.getCode())) {
            companyCode = "LWBCTB";
        } else if (companyCode.equals(CompanyManager.CompanyEnum.KMB_NWFB.getCode())) {
            companyCode = "KMBNWFB";
        }
        return companyCode;
    }

    private List<StopItemData> initData(List<Route> routes, String companyCode, SQLiteDatabase db) {
        List<StopItemData> data = new ArrayList<>();
        StopDAO stopDAO = new StopDAOImpl(db);

        routes.forEach(route -> {
            Stop stop = stopDAO.getStop(route.stopId());
            mStops.add(stop);
        });

        String language = Locale.getDefault().getLanguage();
        Route route = routes.get(0);

        FareManager fareManager = new FareManager(db);
        String[] stopFares = fareManager.getFares(route, new FareDAOImpl(db), mStops.size());

        for (int i = 0; i < mStops.size(); i++) {
            Stop stop = mStops.get(i);
            StopItemData stopItemData = new StopItemData(String.valueOf(i + 1),
                    stop.getName(language),
                    stopFares[i],
                    route.routeSeq(),
                    companyCode,
                    route.id(),
                    i);
            data.add(stopItemData);
        }

        return data;
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
