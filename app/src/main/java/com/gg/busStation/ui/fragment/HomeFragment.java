package com.gg.busStation.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.HomeViewModel;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.function.location.LocationHelper;
import com.gg.busStation.ui.adapter.MainAdapter;
import com.gg.busStation.databinding.FragmentHomeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel mViewModel;
    AlertDialog loadingDialog;

    private int page = 1;
    private final int pageSize = 15;
    private boolean isLoading = false;
    private List<ListItemData> mData;

    MenuProvider menuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.top_bar_menu, menu);
            MenuItem item = menu.findItem(R.id.search_toolbar_item);
            SearchView searchView = (SearchView) item.getActionView();
            if (searchView == null) {
                return;
            }

            searchView.setQueryHint(getString(R.string.search_hint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    setRouteList(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    setRouteList(newText);
                    return true;
                }
            });
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
            return false;
        }
    };

    // 权限申请回调
    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (Boolean.FALSE.equals(result)) {
            Toast.makeText(requireContext(), "权限授权失败，请手动给予", Toast.LENGTH_SHORT).show();
        } else {
            LocationHelper.getLocation(true);
        }
    });

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
                    .setCancelable(false)
                    .show();

            new Thread(() -> {
                try {
                    BusDataManager.initData(requireContext());
                } catch (IOException e) {
                    Toast.makeText(requireContext(), R.string.error_getdata, Toast.LENGTH_SHORT).show();
                }

                List<Route> routes = DataBaseManager.getRoutesHistory();
                List<ListItemData> data = BusDataManager.routesToListItemData(routes);

                initView(data);
            }).start();
        } else {
            initView(mViewModel.data.get());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        List<Route> routes = DataBaseManager.getRoutesHistory();
        List<ListItemData> data = BusDataManager.routesToListItemData(routes);
        mViewModel.data.set(data);

        requireActivity().removeMenuProvider(menuProvider);
    }

    private void initView(List<ListItemData> data) {
        FragmentActivity activity = requireActivity();

        MainAdapter mainAdapter = new MainAdapter(activity);
        mainAdapter.submitList(data);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);

        manager.setInitialPrefetchItemCount(10);
        divider.setLastItemDecorated(false);

        binding.busScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            mViewModel.scrollOffset = scrollY;

            if (isLoading || scrollY + v.getMeasuredHeight() + 200 < v.getChildAt(0).getMeasuredHeight()) {
                return;
            }
            isLoading = true;

            Executors.newFixedThreadPool(1).execute(() -> {
                int startIndex = pageSize * (page - 1);
                int endIndex = Math.min(pageSize * page, mData.size());
                if (startIndex >= mData.size()) {
                    isLoading = false;
                    return;
                }

                List<ListItemData> newData = mData.subList(startIndex, endIndex);
                List<ListItemData> listItemData = new ArrayList<>(mainAdapter.getCurrentList());
                listItemData.addAll(newData);
                mainAdapter.submitList(listItemData);
                page++;
                isLoading = false;
            });
        });

        activity.runOnUiThread(() -> {
            requireActivity().addMenuProvider(menuProvider);

            RecyclerView busListView = binding.busListView;
            busListView.setLayoutManager(manager);
            busListView.addItemDecoration(divider);
            busListView.setHasFixedSize(true);

            busListView.setAdapter(mainAdapter);
            binding.busScrollView.scrollTo(0, mViewModel.scrollOffset);

            loadingDialog.dismiss();

            checkPermissions();
        });
    }

    private void setRouteList(String newText) {
        new Thread(() -> {
            List<Route> routes;
            if (newText.isEmpty()) {
                routes = DataBaseManager.getRoutesHistory();
            } else {
                routes = DataBaseManager.getRoutes(newText);
            }

            RecyclerView recyclerView = binding.busListView;
            MainAdapter adapter = (MainAdapter) recyclerView.getAdapter();

            mData = BusDataManager.routesToListItemData(routes);
            page = 1;

            if (adapter != null) {
                adapter.submitList(mData.subList(0, Math.min(pageSize, mData.size())));
            }
        }).start();
    }

    private void checkPermissions() {
        if ("true".equals(DataBaseManager.getSettings().get("isInit"))) {
            return;
        }

        int selfPermission = requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (selfPermission == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_permission_title)
                .setMessage(R.string.dialog_permission_message)
                .setNegativeButton(R.string.dialog_permission_decline, (dialog, which) -> {})
                .setPositiveButton(R.string.dialog_permission_accept, (dialog, which) -> requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                .show();

        DataBaseManager.setInitStatus(true);
    }
}
