package com.gg.busStation.ui.fragment;

import android.app.ActivityOptions;
import android.app.UiModeManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.gg.busStation.R;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.ui.activity.AboutActivity;
import com.gg.busStation.ui.activity.MainActivity;
import com.gg.busStation.ui.layout.preference.MaterialSwitchPreference;
import com.gg.busStation.ui.layout.preference.MenuPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {
    private AlertDialog loadingDialog;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
        initPreferences();
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

        settingsCheckUpdate.setOnPreferenceChangeListener((preference, newValue) -> {
            DataBaseManager.updateSetting("dontUpdate", String.valueOf(!(boolean) newValue));
            return true;
        });
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

        settingsUpdateCycle.setOnPreferenceChangeListener((preference, newValue) -> {
            String updateTime = (String) newValue;

            updateTime = String.valueOf(1000 * 60 * 60 * 24 * Integer.parseInt(updateTime));
            DataBaseManager.updateSetting("updateTime", updateTime);

            return true;
        });
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
            public void finish(boolean status) {
                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(requireActivity(), status ? R.string.update_success : R.string.error_getdata, Toast.LENGTH_SHORT).show();
                });
            }
        };

        settingsUpdateNow.setOnPreferenceClickListener(preference -> {
            loadingDialog = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.dialog_loading)
                    .setView(R.layout.dialog_loading)
                    .setCancelable(false)
                    .create();

            new Thread(() -> BusDataManager.initData(onDataInitListener, true)).start();
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
