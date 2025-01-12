package com.gg.busStation.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gg.busStation.databinding.DialogEtaItemBinding;

@SuppressLint("ViewConstructor")
public class ETAView extends LinearLayout {
    private DialogEtaItemBinding binding;
    private int time;
    private final String rmk;
    private final String co;

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
        binding = DialogEtaItemBinding.inflate(LayoutInflater.from(context), this);

        binding.dialogEtaTime.setText(String.valueOf(time));
        binding.dialogEtaRmk.setText(rmk);
        binding.dialogEtaCo.setText(co);

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

        setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        ));

        addView(textView);
    }

    public void updateTime() {
        time--;
        if (time == -1) {
            ((ViewGroup)getParent()).removeView(this);
            return;
        }

        if (time > 0) {
            binding.dialogEtaTime.setVisibility(VISIBLE);
            binding.dialogEtaTime.setText(String.valueOf(time));
            return;
        }

        removeAllViews();
        initView(getContext(), this.rmk, this.co);
    }
}
