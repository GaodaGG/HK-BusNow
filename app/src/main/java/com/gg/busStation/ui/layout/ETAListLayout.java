package com.gg.busStation.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gg.busStation.R;

@SuppressLint("ViewConstructor")
public class ETAListLayout extends LinearLayout {

    public ETAListLayout(Context context, int time, String rmk, String co) {
        super(context);

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
}
