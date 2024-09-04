package com.gg.busStation.ui.layout;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.gg.busStation.function.DataManager;
import com.google.android.material.motion.MotionUtils;

import java.io.IOException;
import java.util.Date;

public class StopItemView extends LinearLayout {
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

    private void getETA(Context context, View view) {
        if (isOpen) return;

        Handler mainHandler = new Handler(Looper.getMainLooper());

        StopItemData data = binding.getData();
        Route route = DataBaseManager.findRoute(data.getRouteId(), data.getBound(), data.getService_type());
        Stop stop = DataBaseManager.findStop(data.getStopId());

        LinearLayout timeList = view.findViewById(R.id.dialog_time_list);
        timeList.removeAllViews();
        new Thread(() -> {
            try {
                boolean hasBus = false;

                for (ETA eta : DataManager.routeAndStopToETAs(route, stop, Integer.parseInt((String) binding.listItemNumber.getText()))) {
                    Date date = eta.getEta();
                    hasBus = true;
                    long time = DataManager.getMinutesRemaining(date);
                    View etaView;
                    if (time > 0) {
                        etaView = new ETAListLayout(context, (int) time, eta.getRmk("zh_CN"), Route.coKMB.equals(eta.getCo()) ? "九巴" : "城巴");
                    } else {
                        etaView = new TextView(context);
                        String text = "即将到站" + eta.getRmk("zh_CN") + (Route.coKMB.equals(eta.getCo()) ? "九巴" : "城巴");
                        ((TextView) etaView).setText(text);
                        ((TextView) etaView).setTextSize(20);
                        ((TextView) etaView).setTypeface(null, Typeface.BOLD);
                    }
                    mainHandler.post(() -> timeList.addView(etaView));
                }

                if (!hasBus) {
                    TextView textView = new TextView(context);
                    textView.setText("已无预定班次");
                    mainHandler.post(() -> timeList.addView(textView));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

    }

    public void bindData(StopItemData data) {
        binding.setData(data);
        binding.executePendingBindings();
        this.isOpen = data.isOpen.get();  // 绑定初始状态

        // 计算初始展开和收起的高度
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (closeHeight == 0) {
                    closeHeight = binding.listItemLayout.getHeight();
                }
                if (openHeight == 0) {
                    openHeight = StopItemView.this.getHeight();
                }
                ViewGroup.LayoutParams layoutParams = StopItemView.this.getLayoutParams();
                layoutParams.height = closeHeight;

                StopItemView.this.setLayoutParams(layoutParams);
                StopItemView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void toggle() {
        isOpen = !isOpen;
        binding.getData().isOpen.set(isOpen);
        adjustViewHeight(isOpen, getLayoutParams());
    }

    private void adjustViewHeight(boolean isOpen, ViewGroup.LayoutParams layoutParams) {
        if (layoutParams instanceof RecyclerView.LayoutParams recyclerViewLayoutParams) {
            switchItemHeight(isOpen, recyclerViewLayoutParams);
        } else {
            switchItemHeight(isOpen, layoutParams);
        }
    }

    private void switchItemHeight(boolean isOpen, ViewGroup.LayoutParams layoutParams) {
        int startHeight = isOpen ? closeHeight : openHeight;
        int endHeight = isOpen ? openHeight : closeHeight;

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
