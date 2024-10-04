package com.gg.busStation.ui.layout;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.BusDataManager;
import com.google.android.material.motion.MotionUtils;

import java.io.IOException;
import java.util.List;

public class StopItemView extends LinearLayout {
    private final BroadcastReceiver updateTimeReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!Intent.ACTION_TIME_TICK.equals(action)) {
                return;
            }

            LinearLayout timeList = findViewById(R.id.dialog_time_list);
            for (int i = 0; i < timeList.getChildCount(); i++) {
                ETAView etaView = (ETAView) timeList.getChildAt(i);
                etaView.updateTime();
            }
        }
    };

    private ItemBusExpendBinding binding;
    private boolean isOpen = false;
    private int openHeight;
    private int closeHeight;

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
        this.setOrientation(VERTICAL);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // 确保宽度为 match_parent
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(layoutParams);

        // 设置点击监听器来处理展开/收起
        setOnClickListener(view -> {
            getETA(context, view);
            toggle();
        });
    }

    public void bindData(StopItemData data) {
        binding.setData(data);
        binding.executePendingBindings();
        this.isOpen = data.isOpen.get();  // 绑定初始状态

//        if (!etaViews.isEmpty()) {
//            LinearLayout timeList = binding.dialogTimeList;
//            timeList.removeAllViews();
//            for (View etaView : etaViews) {
//                timeList.addView(etaView);
//            }
//        }

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
    }

    private void getETA(Context context, View view) {
        if (isOpen) return;

        Handler mainHandler = new Handler(Looper.getMainLooper());

        StopItemData data = binding.getData();
        Route route = DataBaseManager.findRoute(data.getRouteId(), data.getBound(), data.getService_type());
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

            for (ETA eta : etas) {
                long time = BusDataManager.getMinutesRemaining(eta.getEta());
                View etaView = new ETAView(context, (int) time, eta.getRmk("zh_CN"), Route.coKMB.equals(eta.getCo()) ? "九巴" : "城巴");
                mainHandler.post(() -> timeList.addView(etaView));
            }

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
}
