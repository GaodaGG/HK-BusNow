package com.gg.busStation.ui.layout;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.bus.Stop;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.databinding.ItemBusExpendBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.DataBaseManager;
import com.google.android.material.motion.MotionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StopItemView extends LinearLayout {
    private ItemBusExpendBinding binding;
    private boolean isOpen = false;
    private int openHeight;
    private int closeHeight;

    private int updateCounter = 0;
    //记录上次更新时间 防止多次更新 比如从后台切回前台
    private int lastUpdateTime = Calendar.getInstance().get(java.util.Calendar.MINUTE);

    private final BroadcastReceiver updateTimeReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int updateTime = Calendar.getInstance().get(java.util.Calendar.MINUTE);
            if (Intent.ACTION_TIME_TICK.equals(action) && updateTime - lastUpdateTime >= 1) {
                updateTime(context, false);
                lastUpdateTime = updateTime;
            }
        }
    };

    public StopItemView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public StopItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StopItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        binding = ItemBusExpendBinding.inflate(LayoutInflater.from(context), this, true);
        setOrientation(VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // 确保宽度为 match_parent
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // 设置点击监听器来处理展开/收起
        setOnClickListener(view -> {
            if (!isOpen) getETA(context, view);
            toggle();
        });
    }

    public void bindData(StopItemData data) {
        binding.setData(data);
        binding.executePendingBindings();
        this.isOpen = data.isOpen.get();  // 绑定初始状态

        // 计算初始展开和收起的高度
        this.post(() -> {
            if (closeHeight == 0) {
                closeHeight = binding.listItemLayout.getHeight();
            }

            if (openHeight == 0) {
                openHeight = StopItemView.this.getHeight();
            }

            ViewGroup.LayoutParams layoutParams = StopItemView.this.getLayoutParams();
            switchItemHeight(isOpen, false, layoutParams);
        });

        if (isOpen) {
            ETAView[] etas = data.getEtas();
            if (etas == null) {
                TextView textView = new TextView(binding.getRoot().getContext());
                textView.setText("已无预定班次");
                binding.dialogTimeList.removeAllViews();
                binding.dialogTimeList.addView(textView);
                return;
            }

            for (ETAView eta : etas) {
                ((ViewGroup) eta.getParent()).removeView(eta);
                binding.dialogTimeList.addView(eta);

                //注册广播更新时间
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_TIME_TICK);
                getContext().registerReceiver(updateTimeReciver, filter);
            }

            getLayoutParams().height = openHeight;
        }
    }

    public void updateTime(Context context, boolean ignoreCount) {
        // 每隔5分钟重新获取一次时间
        if (updateCounter == 5 || ignoreCount) {
            getETA(context, this);
            updateCounter = 0;
            return;
        }

        updateCounter++;

        LinearLayout timeList = findViewById(R.id.dialog_time_list);
        for (int i = 0; i < timeList.getChildCount(); i++) {
            ETAView etaView = (ETAView) timeList.getChildAt(i);
            etaView.updateTime();
        }
    }

    public void getETA(Context context, View view) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        StopItemData data = binding.getData();
        Route route = DataBaseManager.findRoute(data.getCo(), data.getRouteId(), data.getBound(), data.getService_type());
        Stop stop = DataBaseManager.findStop(data.getStopId());

        LinearLayout timeList = view.findViewById(R.id.dialog_time_list);

        new Thread(() -> {
            List<ETA> etas;
            try {
                etas = BusDataManager.routeAndStopToETAs(route, stop, Integer.parseInt((String) binding.listItemNumber.getText()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            mainHandler.post(timeList::removeAllViews);

            if (etas.isEmpty()) {
                TextView textView = new TextView(context);
                textView.setText("已无预定班次");
                mainHandler.post(() -> timeList.addView(textView));
                return;
            }

            List<ETAView> etaViews = new ArrayList<>();
            for (ETA eta : etas) {
                long time = BusDataManager.getMinutesRemaining(eta.getTime());
                ETAView etaView = new ETAView(context, (int) time, eta.getRmk("zh_CN"), Route.coKMB.equals(eta.getCo()) ? "九巴" : "城巴");
                etaViews.add(etaView);
                mainHandler.post(() -> timeList.addView(etaView));
            }

            data.setEtas(etaViews.toArray(new ETAView[0]));

            //注册广播更新时间
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            getContext().registerReceiver(updateTimeReciver, filter);
        }).start();
    }

    public void toggle() {
        isOpen = !isOpen;
        binding.getData().isOpen.set(isOpen);
        adjustViewHeight(isOpen, getLayoutParams());
    }

    private void adjustViewHeight(boolean isOpen, ViewGroup.LayoutParams layoutParams) {
        if (layoutParams instanceof RecyclerView.LayoutParams recyclerViewLayoutParams) {
            switchItemHeight(isOpen, true, recyclerViewLayoutParams);
        } else {
            switchItemHeight(isOpen, true, layoutParams);
        }
    }

    private void switchItemHeight(boolean isOpen, boolean hasAnimator, ViewGroup.LayoutParams layoutParams) {
        int startHeight = isOpen ? closeHeight : openHeight;
        int endHeight = isOpen ? openHeight : closeHeight;

        if (!hasAnimator) {
            layoutParams.height = endHeight;
            this.setLayoutParams(layoutParams);
            return;
        }

        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        valueAnimator.setDuration(isOpen ? 250 : 450);

        TimeInterpolator interpolator = MotionUtils.resolveThemeInterpolator(getContext(), com.google.android.material.R.attr.motionEasingStandardInterpolator, new FastOutSlowInInterpolator());
        valueAnimator.setInterpolator(interpolator);

        valueAnimator.addUpdateListener(animation -> {
            layoutParams.height = (int) animation.getAnimatedValue();
            this.setLayoutParams(layoutParams);
        });
        valueAnimator.start();
    }

    public boolean isOpen() {
        return isOpen;
    }




}
