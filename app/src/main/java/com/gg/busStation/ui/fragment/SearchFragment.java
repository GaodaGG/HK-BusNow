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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.motion.MotionUtils;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private ItemBusExpendBinding itemBusExpendBinding;
    private boolean isOpen = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        itemBusExpendBinding = ItemBusExpendBinding.inflate(requireActivity().getLayoutInflater());
        itemBusExpendBinding.setData(new StopItemData("1", "摩士公园", "车费:$6.4", Route.Out, "1","1", "8S3JWN1034UTB"));
        binding.testTransCard.addView(itemBusExpendBinding.getRoot());

        for (int i = 0; i < 3; i++) {
            ((LinearLayout) itemBusExpendBinding.getRoot().findViewById(R.id.dialog_time_list)).addView(new ETAListLayout(requireActivity(), i + 10, "延迟班次", "九巴"));
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                SearchFragment.this.addTransform(binding.testTransCard);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void addTransform(MaterialCardView view) {
        if (view == null) {
            return;
        }

        ViewGroup.LayoutParams layoutParams = itemBusExpendBinding.dialogItem.getLayoutParams();
        ConstraintLayout itemView = itemBusExpendBinding.listItemLayout;
        int openHeight = view.getHeight();
        int closeHeight = itemView.getHeight();

        layoutParams.height = closeHeight;
        itemBusExpendBinding.dialogItem.setLayoutParams(layoutParams);


        view.setOnClickListener(v -> {
            ValueAnimator valueAnimator;
            valueAnimator = isOpen ? ValueAnimator.ofInt(openHeight, closeHeight) : ValueAnimator.ofInt(closeHeight, openHeight);

            TimeInterpolator interpolator = MotionUtils.resolveThemeInterpolator(requireContext(), com.google.android.material.R.attr.motionEasingStandardInterpolator, new FastOutSlowInInterpolator());

            valueAnimator.setInterpolator(interpolator);
            valueAnimator.setDuration(isOpen ? 250 : 450);

            valueAnimator.addUpdateListener(vA -> {
                layoutParams.height = (int) vA.getAnimatedValue();
                binding.testTransCard.requestLayout();
            });
            valueAnimator.start();

            isOpen = !isOpen;
        });
    }
}
