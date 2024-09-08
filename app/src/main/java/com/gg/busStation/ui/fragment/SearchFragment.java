package com.gg.busStation.ui.fragment;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.databinding.FragmentSearchBinding;
import com.gg.busStation.databinding.ItemBusExpendBinding;
import com.gg.busStation.ui.layout.ETAListLayout;
import com.gg.busStation.ui.layout.StopItemView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.motion.MotionUtils;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
