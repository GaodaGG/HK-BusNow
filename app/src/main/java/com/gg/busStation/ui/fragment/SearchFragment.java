package com.gg.busStation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.data.layout.SearchViewModel;
import com.gg.busStation.databinding.FragmentSearchBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.ui.adapter.MainAdapter;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.search.SearchBar;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private SearchViewModel mViewModel;
    private SearchBar mSearchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mViewModel.listItemData.isEmpty()) {
            initView(new ArrayList<>());
        } else {
            initView(mViewModel.listItemData);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mSearchBar = requireActivity().getWindow().getDecorView().findViewById(R.id.searchBar);
        if (!mViewModel.outputText.isEmpty()) {
            binding.searchKeyboard.setOutputText(mViewModel.outputText);
            mSearchBar.setText(mViewModel.outputText);
        }
        binding.searchErrorLayout.setVisibility((mViewModel.outputText.isEmpty() && mSearchBar.getText().length() == 0) ? View.VISIBLE : View.GONE);

        binding.searchKeyboard.setOnKeyClickListener(key -> {
            mSearchBar.setText(key);

//            binding.searchBar.setText(key);
            setRouteList(key);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mViewModel.listItemData = ((MainAdapter) binding.busListView.getAdapter()).getCurrentList();
        mViewModel.outputText = mSearchBar.getText().toString();
    }

    private void initView(List<ListItemData> data) {
        FragmentActivity activity = requireActivity();

//        Drawable background = ((ActionBar) ((AppCompatActivity) activity).getSupportActionBar())..getBackground();
        MainAdapter mainAdapter = new MainAdapter(activity, true);
        mainAdapter.getCurrentList();
        mainAdapter.submitList(data);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL);

        manager.setInitialPrefetchItemCount(10);
        divider.setLastItemDecorated(false);

        activity.runOnUiThread(() -> {
            RecyclerView busListView = binding.busListView;
            busListView.setLayoutManager(manager);
            busListView.addItemDecoration(divider);
            busListView.setHasFixedSize(true);
            busListView.setAdapter(mainAdapter);
        });
    }

    private void setRouteList(String newText) {
        new Thread(() -> {
            List<Route> routes = newText.isEmpty() ? new ArrayList<>() : DataBaseManager.getRoutes(newText);
            requireActivity().runOnUiThread(() -> binding.searchErrorLayout.setVisibility(newText.isEmpty() ? View.VISIBLE : View.GONE));

            MainAdapter adapter = (MainAdapter) binding.busListView.getAdapter();
            if (adapter != null) {
                adapter.submitList(BusDataManager.routesToListItemData(routes));
            }
        }).start();
    }
}
