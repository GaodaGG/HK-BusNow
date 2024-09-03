package com.gg.busStation.ui.layout;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gg.busStation.R;

public class ETAListLayout extends LinearLayout {
    private final int time;
    private final String rmk;

    private TextView mTimeView;
    private TextView mRmkView;

    public ETAListLayout(Context context, int time, String rmk) {
        super(context);
        this.time = time;
        this.rmk = rmk;

        View inflate = inflate(context, R.layout.dialog_eta_item, this);
        mTimeView = inflate.findViewById(R.id.dialog_eta_time);
        mRmkView = inflate.findViewById(R.id.dialog_eta_rmk);

        mTimeView.setText(String.valueOf(this.time));
        mRmkView.setText(this.rmk);

        setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ));
    }
}
