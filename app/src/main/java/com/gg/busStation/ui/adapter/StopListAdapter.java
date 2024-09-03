package com.gg.busStation.ui.adapter;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.databinding.ItemBusExpendBinding;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.DataManager;
import com.gg.busStation.ui.layout.ETAListLayout;
import com.google.android.material.motion.MotionUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class StopListAdapter extends RecyclerView.Adapter<StopListAdapter.ViewHolder> {
    private final Activity mActivity;
    private final List<StopItemData> mData;

    public StopListAdapter(List<StopItemData> data, Activity context) {
        this.mData = data;
        this.mActivity = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        ItemBusExpendBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_bus_expend, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StopItemData stopItemData = mData.get(position);
        holder.getBinding().setData(stopItemData);

        holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initItemView(holder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void initItemView(ViewHolder holder) {
        View view = holder.itemView;

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        ConstraintLayout itemView = holder.getBinding().listItemLayout;

        boolean isOpen = holder.isOpen();

        if (holder.getCloseHeight() == 0) {
            holder.setOpenHeight(view.getHeight());
            holder.setCloseHeight(itemView.getHeight());
        }

        holder.getAdapterPosition();
        if (!isOpen) {
            layoutParams.height = holder.getCloseHeight();
            holder.itemView.requestLayout();
        }

        holder.getBinding().executePendingBindings();

        view.setOnClickListener(v -> itemClickListener(holder, v, isOpen, layoutParams));
    }

    private void itemClickListener(ViewHolder holder, View view, boolean isOpen, RecyclerView.LayoutParams layoutParams) {
        StopItemData data = holder.binding.getData();
        Route route = DataBaseManager.findRoute(data.getRouteId(), data.getBound(), data.getService_type());
        Stop stop = DataBaseManager.findStop(data.getStopId());

        LinearLayout timeList = view.findViewById(R.id.dialog_time_list);
        timeList.removeAllViews();
        new Thread(() -> {
            try {
                boolean hasBus = false;
                for (ETA eta : DataManager.routeAndStopToETAs(route, stop)) {
                    Date date = eta.getEta();
                    if (date == null) {
                        continue;
                    }
                    hasBus = true;
                    ETAListLayout etaListLayout = new ETAListLayout(mActivity, (int) DataManager.getMinutesRemaining(date), eta.getRmk("zh_CN"));
                    mActivity.runOnUiThread(() -> timeList.addView(etaListLayout));
                }

                if (!hasBus) {
                    TextView textView = new TextView(mActivity);
                    textView.setText("已无预定班次");
                    mActivity.runOnUiThread(() -> timeList.addView(textView));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();


        switchItemHeight(isOpen, holder.getAdapterPosition(), holder.getOpenHeight(), holder.getCloseHeight(), layoutParams);
        holder.setOpen(!isOpen);
    }

    public void switchItemHeight(boolean isOpen, int position, int openHeight, int closeHeight, RecyclerView.LayoutParams layoutParams) {
        ValueAnimator valueAnimator;

        if (isOpen) {
            valueAnimator = ValueAnimator.ofInt(openHeight, closeHeight);
            valueAnimator.setDuration(250);
        } else {
            valueAnimator = ValueAnimator.ofInt(closeHeight, openHeight);
            valueAnimator.setDuration(450);
        }

        TimeInterpolator interpolator = MotionUtils.resolveThemeInterpolator(mActivity, com.google.android.material.R.attr.motionEasingStandardInterpolator, new FastOutSlowInInterpolator());
        valueAnimator.setInterpolator(interpolator);

        valueAnimator.addUpdateListener(vA -> {
            layoutParams.height = (int) vA.getAnimatedValue();
            notifyItemChanged(position);
        });
        valueAnimator.start();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBusExpendBinding binding;
        private boolean isOpen = false;
        private int openHeight;
        private int closeHeight;

        public ViewHolder(ItemBusExpendBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemBusExpendBinding getBinding() {
            return binding;
        }


        public boolean isOpen() {
            return isOpen;
        }

        public void setOpen(boolean open) {
            isOpen = open;
        }

        public int getOpenHeight() {
            return openHeight;
        }

        public void setOpenHeight(int openHeight) {
            this.openHeight = openHeight;
        }

        public int getCloseHeight() {
            return closeHeight;
        }

        public void setCloseHeight(int closeHeight) {
            this.closeHeight = closeHeight;
        }
    }
}
