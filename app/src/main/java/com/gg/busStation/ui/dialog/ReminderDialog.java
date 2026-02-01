package com.gg.busStation.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.ETA;
import com.gg.busStation.data.layout.StopItemData;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.reminder.ReminderManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 提醒选择对话框
 * 用于让用户选择提前多少分钟提醒
 */
public class ReminderDialog {
    private final Context context;
    private final String routeName;
    private final String stopName;
    private final StopItemData stopData;
    private final List<ETA> etaList;

    private int selectedMinutes = 2; // 默认提前2分钟

    public ReminderDialog(Context context, int reminderType, String routeName,
                          String stopName, StopItemData stopData, List<ETA> etaList) {
        this.context = context;
        this.routeName = routeName;
        this.stopName = stopName;
        this.stopData = stopData;
        this.etaList = etaList;
    }

    public void show() {
        // 检查是否有ETA数据
        if (etaList == null || etaList.isEmpty()) {
            Toast.makeText(context, R.string.dialog_reminder_no_eta, Toast.LENGTH_SHORT).show();
            return;
        }

        ETA firstEta = etaList.get(0);
        if (firstEta.getTime() == null) {
            Toast.makeText(context, R.string.dialog_reminder_no_eta, Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建自定义视图
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reminder, null);

        // 显示ETA信息
        TextView etaInfoText = dialogView.findViewById(R.id.eta_info_text);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        long minutesRemaining = BusDataManager.getMinutesRemaining(firstEta.getTime());
        String etaInfo = String.format(Locale.getDefault(),
                "预计到站时间: %s (约 %d 分钟后)",
                sdf.format(firstEta.getTime()),
                minutesRemaining);
        etaInfoText.setText(etaInfo);

        // 设置选项
        RadioGroup radioGroup = dialogView.findViewById(R.id.reminder_options);
        setupRadioGroup(radioGroup, minutesRemaining);

        // 构建对话框
        String title = context.getString(R.string.stop_menu_boarding);

        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_reminder_cancel, null)
                .setPositiveButton(R.string.dialog_reminder_confirm, (dialog, which) -> setReminder(firstEta))
                .show();
    }

    private void setupRadioGroup(RadioGroup radioGroup, long minutesRemaining) {
        int[] options = {1, 2, 3, 5};
        String[] optionTexts = {
                context.getString(R.string.reminder_option_1_min),
                context.getString(R.string.reminder_option_2_min),
                context.getString(R.string.reminder_option_3_min),
                context.getString(R.string.reminder_option_5_min)
        };

        for (int i = 0; i < options.length; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(View.generateViewId());
            radioButton.setText(optionTexts[i]);
            radioButton.setTag(options[i]);

            // 如果剩余时间不足，禁用该选项
            if (options[i] >= minutesRemaining) {
                radioButton.setEnabled(false);
                radioButton.setText(optionTexts[i] + " (时间不足)");
            }

            radioGroup.addView(radioButton);

            // 默认选中2分钟（如果可用）
            if (options[i] == 2 && options[i] < minutesRemaining) {
                radioButton.setChecked(true);
            }
        }

        // 如果默认选项不可用，选中第一个可用的选项
        if (selectedMinutes >= minutesRemaining) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
                if (rb.isEnabled()) {
                    rb.setChecked(true);
                    selectedMinutes = (int) rb.getTag();
                    break;
                }
            }
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadio = group.findViewById(checkedId);
            if (selectedRadio != null && selectedRadio.getTag() != null) {
                selectedMinutes = (int) selectedRadio.getTag();
            }
        });
    }

    private void setReminder(ETA eta) {
        ReminderManager reminderManager = ReminderManager.getInstance(context);

        boolean success = reminderManager.createBoardingReminder(
                routeName,
                stopName,
                stopData.getCo(),
                stopData.getRouteId(),
                stopData.getBound(),
                stopData.getStopSeq(),
                eta,
                selectedMinutes
        );

        if (success) {
            Toast.makeText(context, R.string.dialog_reminder_set_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.dialog_reminder_set_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
