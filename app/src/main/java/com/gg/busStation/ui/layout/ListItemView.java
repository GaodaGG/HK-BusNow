package com.gg.busStation.ui.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gg.busStation.R;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.databinding.ItemBusBinding;
import com.gg.busStation.function.Tools;

public class ListItemView extends ConstraintLayout {
    private ItemBusBinding binding;
    private ListItemData mData;

    public ListItemView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public ListItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public ListItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            LayoutInflater.from(context).inflate(R.layout.item_bus, this, true);
            return;
        }

        binding = ItemBusBinding.inflate(LayoutInflater.from(context), this, true);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // 确保宽度为 match_parent
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.height = Tools.dp2px(context, 88);
        setLayoutParams(layoutParams);
        mData = new ListItemData(0, "KMB", "1", "HendLine", "Context", 1, "1", "KMB");
        setData(mData);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListItemView);
            setHeadline(typedArray.getString(R.styleable.ListItemView_headline));
            setStopNumber(typedArray.getString(R.styleable.ListItemView_stopNumber));
            setContext(typedArray.getString(R.styleable.ListItemView_context));
            setTips(typedArray.getString(R.styleable.ListItemView_tips));

            typedArray.recycle();
        }

    }

    public String getStopNumber() {
        return mData.getStopNumber();
    }

    public void setStopNumber(String stopNumber) {
        mData.setStopNumber(stopNumber);
    }

    public void setData(ListItemData data) {
        binding.setData(data);
        mData = data;
    }

    public void setTips(String tips) {
        mData.setTips(tips);
    }

    public void setContext(String context) {
        mData.setContext(context);
    }

    public void setHeadline(String headline) {
        mData.setHeadline(headline);
    }
}
