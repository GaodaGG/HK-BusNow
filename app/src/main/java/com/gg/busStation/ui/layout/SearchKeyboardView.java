package com.gg.busStation.ui.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.gg.busStation.databinding.SearchKeyboardBinding;
import com.gg.busStation.function.DataBaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.stream.Collectors;

public class SearchKeyboardView extends MaterialCardView {
    private SearchKeyboardBinding binding;
    private OnKeyClickListener onKeyClickListener;
    private String outputText = "";

    public SearchKeyboardView(Context context) {
        super(context, null, com.google.android.material.R.attr.materialCardViewElevatedStyle);
        initView(context);
    }

    public SearchKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs, com.google.android.material.R.attr.materialCardViewElevatedStyle);
        initView(context);
    }

    public SearchKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private final View.OnClickListener onClickListener = v -> {
        MaterialButton button = (MaterialButton) v;
        String text = button.getText().toString();
        if (text.isEmpty()) {
            text = button.getTag().toString();
        }

        switch (text) {
            case "delete":
                outputText = "";
                onKeyClickListener.onKeyClick(outputText);
                break;
            case "backspace":
                if (outputText.isEmpty()) {
                    break;
                }
                outputText = outputText.substring(0, outputText.length() - 1);
                onKeyClickListener.onKeyClick(outputText);
                break;
            default:
                outputText += text;
                onKeyClickListener.onKeyClick(outputText);
                break;
        }

        setButtonStatus(outputText.length() + 1);
    };

    private void initView(Context context) {
        binding = SearchKeyboardBinding.inflate(LayoutInflater.from(context), this);
        setButtonStatus(1);
    }

    private void setButtonStatus(int index) {
        List<String> routeNthCharacters = DataBaseManager.getRouteNthCharacters(outputText, index);

        List<String> rightKeys = routeNthCharacters.stream()
                .filter(s -> s.matches("^[a-zA-Z]+$"))
                .collect(Collectors.toList());

        binding.itemSearchKeyboardScrollview.removeAllViews();
        for (String rightKey : rightKeys) {
            binding.itemSearchKeyboardScrollview.addView(createKeyView(rightKey));
        }

        for (int i = 0; i < binding.itemSearchKeyboardLayout.getChildCount(); i++) {
            View child = binding.itemSearchKeyboardLayout.getChildAt(i);
            if (child instanceof MaterialButton button) {
                button.setOnClickListener(onClickListener);

                button.setEnabled(routeNthCharacters.contains(button.getText().toString()) || (button.getText().toString().isEmpty() && !outputText.isEmpty()));
            }
        }
    }

    private MaterialButton createKeyView(String key) {
        MaterialButton button = new MaterialButton(getContext());
        button.setText(key);
        button.setTextSize(22);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setOnClickListener(onClickListener);
        return button;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
        setButtonStatus(outputText.length() + 1);
    }

    public void setOnKeyClickListener(OnKeyClickListener onKeyClickListener) {
        this.onKeyClickListener = onKeyClickListener;
    }

    public interface OnKeyClickListener {
        void onKeyClick(String key);
    }
}
