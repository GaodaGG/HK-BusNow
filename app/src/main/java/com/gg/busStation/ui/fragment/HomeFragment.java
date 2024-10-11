package com.gg.busStation.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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
import androidx.appcompat.widget.Toolbar;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.io.IOException;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel mViewModel;
    AlertDialog loadingDialog;

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
    }

    private void initView(List<ListItemData> data) {
        FragmentActivity activity = requireActivity();
        Menu menu = ((MaterialToolbar) activity.findViewById(R.id.toolBar)).getMenu();
        initMenu(menu);

        MainAdapter mainAdapter = new MainAdapter(activity);
        mainAdapter.submitList(data);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);

        manager.setInitialPrefetchItemCount(10);
        divider.setLastItemDecorated(false);

        activity.runOnUiThread(() -> {
            binding.busListView.setLayoutManager(manager);
            binding.busListView.addItemDecoration(divider);
            binding.busListView.setHasFixedSize(true);

            binding.busListView.setAdapter(mainAdapter);
            binding.busScrollView.scrollTo(0, mViewModel.scrollOffset);
            binding.busScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> mViewModel.scrollOffset = scrollY);

            loadingDialog.dismiss();

            checkPermissions();
        });
    }

    private void initMenu(Menu menu) {
        SearchView searchView = (SearchView) menu.findItem(R.id.search_toolbar_item).getActionView();
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

    //TODO 分页加载
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
            List<ListItemData> data = BusDataManager.routesToListItemData(routes);

            if (adapter != null) {
                requireActivity().runOnUiThread(() -> adapter.submitList(data));
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
