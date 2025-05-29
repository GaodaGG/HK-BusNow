package com.gg.busStation.ui.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.databinding.FragmentHomeBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.Tools;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FeatureDAO;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.database.dao.HistoryDAO;
import com.gg.busStation.function.database.dao.HistoryDAOImpl;
import com.gg.busStation.ui.adapter.MainAdapter;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        List<RouteA> routes = DataBaseManager.getRoutesHistory();
//        List<ListItemData> data = BusDataManager.routesToListItemData(routes);
        SQLiteDatabase database = DataBaseHelper.getInstance(requireContext()).getDatabase();
        HistoryDAO historyDAO = new HistoryDAOImpl(database);
        FeatureDAO featureDAO = new FeatureDAOImpl(database);
        List<Route> allHistory = historyDAO.getAllHistory();
        List<ListItemData> data = BusDataManager.routesToListItemData(allHistory, featureDAO);
        initView(data);
    }

    @Override
    public void onStart() {
        super.onStart();
        int bottomHeight = requireActivity().findViewById(R.id.bottom_navigation).getHeight();
        binding.getRoot().setPadding(0, 0, 0, bottomHeight);
    }

    private void initView(List<ListItemData> data) {
        FragmentActivity activity = requireActivity();

        if (data.isEmpty()) {
            binding.mainErrorLayout.setVisibility(View.VISIBLE);
        }

        MainAdapter mainAdapter = new MainAdapter(activity, false);
        mainAdapter.submitList(data);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);

        int inset = Tools.dp2px(requireContext(), 16);
        manager.setInitialPrefetchItemCount(10);
        divider.setLastItemDecorated(false);
        divider.setDividerInsetStart(inset);
        divider.setDividerInsetEnd(inset);

        activity.runOnUiThread(() -> {
            RecyclerView busListView = binding.busListView;
            busListView.setLayoutManager(manager);
            busListView.addItemDecoration(divider);
            busListView.setHasFixedSize(true);
            busListView.setAdapter(mainAdapter);
        });
    }
}
