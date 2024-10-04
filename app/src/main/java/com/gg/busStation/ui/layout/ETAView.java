package com.gg.busStation.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gg.busStation.R;

@SuppressLint("ViewConstructor")
public class ETAView extends LinearLayout {
    private int time;
    private String rmk;
    private String co;

    public ETAView(Context context, int time, String rmk, String co) {
        super(context);
        this.time = time;
        this.rmk = rmk;
        this.co = co;

        if (time > 0) {
            initView(context, time, rmk, co);
            return;
        }

        initView(context, rmk, co);
    }

    private void initView(Context context, int time, String rmk, String co) {
        View inflate = inflate(context, R.layout.dialog_eta_item, this);
        TextView mTimeView = inflate.findViewById(R.id.dialog_eta_time);
        TextView mRmkView = inflate.findViewById(R.id.dialog_eta_rmk);
        TextView mCoView = inflate.findViewById(R.id.dialog_eta_co);

        mTimeView.setText(String.valueOf(time));
        mRmkView.setText(rmk);
        mCoView.setText(co);

        setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ));
    }

    private void initView(Context context, String rmk, String co) {
        TextView textView = new TextView(context);
        String text = "即将到站" + rmk + co;
        textView.setText(text);
        textView.setTextSize(20);
        textView.setTypeface(null, Typeface.BOLD);
        addView(textView);
    }

    public void updateTime() {
        time--;
        if (time == -1) {
            ((ViewGroup)getParent()).removeView(this);
            return;
        }

        if (time > 0) {
            findViewById(R.id.dialog_eta_time).setVisibility(VISIBLE);
            ((TextView) findViewById(R.id.dialog_eta_time)).setText(String.valueOf(time));
            return;
        }

        removeAllViews();
        initView(getContext(), this.rmk, this.co);
    }
}
