package com.gg.busStation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gg.busStation.R;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.DataManager;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.HomeViewModel;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.ui.adapter.MainAdapter;
import com.gg.busStation.databinding.FragmentHomeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel mViewModel;
    AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

            loadingDialog = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.dialog_loading)
                    .setView(R.layout.dialog_loading)
                    .show();

            new Thread(() -> {
                List<ListItemData> data = new ArrayList<>();

                try {
                    DataManager.initData(requireContext());
                    List<Route> routes = DataBaseManager.getRoutesHistory();

                    for (Route route : routes) {
                        String tips = route.getCo().equals(Route.coCTB) ? "(城巴路线)" : "";
                        ListItemData listItemData = new ListItemData(route.getRoute(),
                                route.getBound(),
                                route.getOrig("zh_CN") + " -> " + route.getDest("zh_CN"),
                                route.getBound(),
                                route.getService_type(),
                                tips);
                        data.add(listItemData);
                    }
                } catch (IOException e) {
                    Toast.makeText(requireContext(), "获取数据失败", Toast.LENGTH_SHORT).show();
                }

                mViewModel.data.set(data);
                initView(data);

            }).start();
        } else {
            initView(mViewModel.data.get());
        }


    }

    private void initView(List<ListItemData> data) {
        MainAdapter mainAdapter = new MainAdapter(requireActivity());
        mainAdapter.submitList(data);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);

        manager.setInitialPrefetchItemCount(10);
        divider.setLastItemDecorated(false);

        requireActivity().runOnUiThread(() -> {
            binding.busListView.setLayoutManager(manager);
            binding.busListView.addItemDecoration(divider);
            binding.busListView.setHasFixedSize(true);

            binding.busListView.setAdapter(mainAdapter);
            binding.busScrollView.scrollTo(0, mViewModel.scrollOffset);
            binding.busScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> mViewModel.scrollOffset = scrollY);

            loadingDialog.hide();
        });
    }
}
