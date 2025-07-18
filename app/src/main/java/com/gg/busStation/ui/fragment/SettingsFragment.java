package com.gg.busStation.ui.fragment;

import android.app.ActivityOptions;
import android.app.UiModeManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.gg.busStation.R;
import com.gg.busStation.databinding.DialogLoadingBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.ui.activity.AboutActivity;
import com.gg.busStation.ui.activity.MainActivity;
import com.gg.busStation.ui.layout.preference.MaterialSwitchPreference;
import com.gg.busStation.ui.layout.preference.MenuPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {
    private AlertDialog loadingDialog;
    private DialogLoadingBinding dialogBinding;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        initPreferences();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), requireActivity().findViewById(R.id.bottom_navigation).getHeight());
    }

    private void initPreferences() {
        // 主题
        themeColorPreference();
        darkThemePreference();
        languagePreference();

        // 数据更新
        updateDataCyclePreference();
        updateDataNowPreference();

        // 一般
        updateAppCyclePreference();
        updateAppNowPreference();
        aboutPreference();
    }

    private void languagePreference() {
        MenuPreference settingsLanguage = findPreference("settings_language");
        if (settingsLanguage == null) {
            return;
        }

        settingsLanguage.setVisible(false);
    }

    private void updateAppCyclePreference() {
        MaterialSwitchPreference settingsCheckUpdate = findPreference("settings_update_app");
        if (settingsCheckUpdate == null) {
            return;
        }

//        settingsCheckUpdate.setOnPreferenceChangeListener((preference, newValue) -> {
//            DataBaseManager.updateSetting("dontUpdate", String.valueOf(!(boolean) newValue));
//            return true;
//        });
    }

    private void updateAppNowPreference() {
        Preference settingsUpdateAppNow = findPreference("settings_update_app_now");
        if (settingsUpdateAppNow == null) {
            return;
        }

        settingsUpdateAppNow.setOnPreferenceClickListener(preference -> {
            new Thread(() -> {
                boolean checked = ((MainActivity) requireActivity()).checkAppUpdate(true);
                if (!checked)
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), R.string.update_app_already, Toast.LENGTH_SHORT).show());
            }).start();
            return true;
        });
    }

    private void updateDataCyclePreference() {
        MenuPreference settingsUpdateCycle = findPreference("settings_update_data_cycle");
        if (settingsUpdateCycle == null) {
            return;
        }

//        settingsUpdateCycle.setOnPreferenceChangeListener((preference, newValue) -> {
//            String updateTime = (String) newValue;
//
//            updateTime = String.valueOf(1000 * 60 * 60 * 24 * Integer.parseInt(updateTime));
//            DataBaseManager.updateSetting("updateTime", updateTime);
//
//            return true;
//        });
    }

    private void themeColorPreference() {
        MaterialSwitchPreference settingsSystemTheme = findPreference("settings_system_theme");
        if (settingsSystemTheme == null) {
            return;
        }

        settingsSystemTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            requireActivity().recreate();
            return true;
        });
    }

    private void darkThemePreference() {
        MenuPreference settingsDarkTheme = findPreference("settings_dark_theme");
        if (settingsDarkTheme == null) {
            return;
        }

        settingsDarkTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            String darkSetting = (String) newValue;
            if (Build.VERSION.SDK_INT >= 31) {
                UiModeManager uiModeManager = requireActivity().getSystemService(UiModeManager.class);
                switch (darkSetting) {
                    case "on":
                        uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
                        break;
                    case "off":
                        uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
                        break;
                    default:
                        uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO);
                        break;
                }
            } else {
                switch (darkSetting) {
                    case "on":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "off":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    default:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
            }
            return true;
        });
    }

    private void updateDataNowPreference() {
        Preference settingsUpdateNow = findPreference("settings_update_data_now");
        if (settingsUpdateNow == null) {
            return;
        }

        BusDataManager.OnDataInitListener onDataInitListener = new BusDataManager.OnDataInitListener() {
            @Override
            public void start() {
                requireActivity().runOnUiThread(loadingDialog::show);
            }

            @Override
            public void progress(int now, int max, String tip){
                requireActivity().runOnUiThread(() -> {
                    dialogBinding.updateIndicator.setMax(max);
                    dialogBinding.updateIndicator.setMin(0);
                    dialogBinding.updateIndicator.setProgress(now, true);
                    dialogBinding.updateProgress.setText(now + " / " + max);
                    dialogBinding.updateTips.setText(tip);
                });
            }

            @Override
            public void finish(boolean status) {
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireActivity(), status ? R.string.update_success : R.string.error_getdata, Toast.LENGTH_SHORT).show();
                });
            }
        };

        settingsUpdateNow.setOnPreferenceClickListener(preference -> {
            dialogBinding = DialogLoadingBinding.inflate(getLayoutInflater());
            loadingDialog = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.dialog_loading)
                    .setView(dialogBinding.getRoot())
                    .setCancelable(false)
                    .create();

            new Thread(() -> BusDataManager.initData(requireContext(), onDataInitListener, true)).start();
            return true;
        });
    }

    private void aboutPreference() {
        Preference settingsAbout = findPreference("settings_about");
        if (settingsAbout == null) {
            return;
        }

        settingsAbout.setOnPreferenceClickListener(preference -> {
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle();
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent, bundle);
            return true;
        });
    }
}
