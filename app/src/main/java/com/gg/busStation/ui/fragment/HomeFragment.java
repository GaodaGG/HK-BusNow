package com.gg.busStation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.material.search.SearchView;

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
                //TODO 将点击过的站点放在列表最前面
                List<ListItemData> data = new ArrayList<>();
                data.add(new ListItemData("107", "土瓜湾站", "九龙湾 -> 华贵", Route.In, "1"));

                try {
                    DataManager.initData();
                    List<Route> routes = DataBaseManager.getRoutes(50);

                    for (int i = 0; i < routes.size(); i++) {
                        Route route = routes.get(i);
                        ListItemData listItemData = new ListItemData(route.getRoute(), route.getBound(), route.getOrig("zh_CN") + " -> " + route.getDest("zh_CN"), route.getBound(), route.getService_type());
                        data.add(listItemData);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                mViewModel.data.set(data);
                initView(data);

            }).start();
        } else {
            initView(mViewModel.data.get());
        }


    }

    private void initView(List<ListItemData> data) {
        MainAdapter mainAdapter = new MainAdapter(data, requireActivity());
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
