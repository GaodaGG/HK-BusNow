package com.gg.busStation.ui.layout;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
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
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.databinding.ItemBusExpendBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.Tools;
import com.gg.busStation.function.database.DataBaseHelper;
import com.gg.busStation.function.feature.CompanyManager;
import com.gg.busStation.function.feature.co.Company;
import com.google.android.material.motion.MotionUtils;

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
        initView(context);
    }

    public StopItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public StopItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = ItemBusExpendBinding.inflate(LayoutInflater.from(context), this, true);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackgroundBorderless});
        setForeground(typedArray.getDrawable(0));
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

    public void setData(StopItemData data) {
        binding.setData(data);
        binding.listItemLayout.setData(new ListItemData(0, data.getCo(), data.getStopNumber(), data.getHeadline(), data.getContext(), 1, "1", ""));
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
                if (eta.getParent() != null) ((ViewGroup) eta.getParent()).removeView(eta);
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

        LinearLayout timeList = view.findViewById(R.id.dialog_time_list);

        new Thread(() -> {
            Company company = CompanyManager.getCompanyInstance(data.getCo());
            List<ETA> etas = company.getETA(data.getRouteId(), data.getBound(), data.getStopSeq(), DataBaseHelper.getInstance(context).getDatabase());
            Log.d("StopItemView", company.getClass().getName());
            Log.d("StopItemView", "getETA: " + etas.toString());

            mainHandler.post(timeList::removeAllViews);

            if (etas.isEmpty()) {
                TextView textView = new TextView(context);
                textView.setText("已无预定班次");
                mainHandler.post(() -> timeList.addView(textView));
                return;
            }

            ArrayList<ETAView> etaViews = new ArrayList<>();
            int size = Math.min(etas.size(), 3);
            for (int i = 0; i < size; i++) {
                ETA eta = etas.get(i);
                long time = BusDataManager.getMinutesRemaining(eta.getTime());
//                ETAView etaView = new ETAView(context, (int) time, eta.getRmk("zh_CN"), RouteA.coKMB.equals(eta.getCo()) ? "九巴" : "城巴");
                ETAView etaView = new ETAView(context, (int) time, eta.getRmk("zh_CN"), CompanyManager.getCompanyByCode(eta.getCo()).getName("zh_CN"));
                etaViews.add(etaView);

                LayoutParams layoutParams = (LayoutParams) etaView.getLayoutParams();
                layoutParams.bottomMargin = Tools.dp2px(context, 4);
                etaView.setLayoutParams(layoutParams);
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


    public void setContext(String context) {
        binding.getData().setContext(context);
    }

    public void setStopNumber(String stopNumber) {
        binding.getData().setStopNumber(stopNumber);
    }

    public void setHeadline(String headline) {
        binding.getData().setHeadline(headline);
    }
}
