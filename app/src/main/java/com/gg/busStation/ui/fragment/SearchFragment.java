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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.Feature;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.data.layout.SearchViewModel;
import com.gg.busStation.databinding.FragmentSearchBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.Tools;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.database.dao.FeatureDAOImpl;
import com.gg.busStation.function.database.dao.RouteDAOImpl;
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

        mSearchBar = requireActivity().findViewById(R.id.searchBar);
        mSearchBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!mViewModel.outputText.isEmpty()) {
            binding.searchKeyboard.setOutputText(mViewModel.outputText);
            mSearchBar.setText(mViewModel.outputText);
        }

        if (mSearchBar != null) {
            binding.searchErrorLayout.setVisibility((mViewModel.outputText.isEmpty() && mSearchBar.getText().length() == 0) ? View.VISIBLE : View.GONE);
        }

        binding.searchKeyboard.setOnKeyClickListener(key -> {
            mSearchBar.setText(key);

            setRouteList(key);
        });

        View bottomBar = requireActivity().findViewById(R.id.bottom_navigation);
        bottomBar.post(() -> {
            int bottomHeight = bottomBar.getHeight();
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.searchKeyboard.getLayoutParams();
            layoutParams.bottomMargin = bottomHeight + Tools.dp2px(requireContext(), 8);
            binding.searchKeyboard.setLayoutParams(layoutParams);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainAdapter adapter = (MainAdapter) binding.busListView.getAdapter();
        if (adapter != null) {
            mViewModel.listItemData = adapter.getCurrentList();
        }

        mViewModel.outputText = mSearchBar.getText().toString();
        mSearchBar.setVisibility(View.GONE);
    }

    private void initView(List<ListItemData> data) {
        FragmentActivity activity = requireActivity();

//        Drawable background = ((ActionBar) ((AppCompatActivity) activity).getSupportActionBar())..getBackground();
        MainAdapter mainAdapter = new MainAdapter(activity, true);
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

    private void setRouteList(String newText) {
        new Thread(() -> {
            SQLiteDatabase database = DataBaseHelper.getInstance(requireContext()).getDatabase();
            List<Feature> features = new FeatureDAOImpl(database).fuzzySearchFeature(newText);
            requireActivity().runOnUiThread(() -> binding.searchErrorLayout.setVisibility(newText.isEmpty() ? View.VISIBLE : View.GONE));

            MainAdapter adapter = (MainAdapter) binding.busListView.getAdapter();
            if (adapter != null) {
                // TODO 直接在数据库进行操作
                adapter.submitList(BusDataManager.featuresToListItemData(features, new RouteDAOImpl(database)));
            }
        }).start();
    }
}
