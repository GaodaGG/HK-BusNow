package com.gg.busStation.ui.activity;

import android.os.Build;
import android.transition.Transition;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewbinding.ViewBinding;

import com.gg.busStation.R;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.transition.platform.MaterialSharedAxis;

public abstract class BaseActivity extends AppCompatActivity {
    abstract ViewBinding getBinding();

    protected void setTransition(boolean isEnter) {
        Transition enter = new MaterialSharedAxis(MaterialSharedAxis.X, isEnter).excludeTarget(R.id.toolBar, true);
        Transition exit = new MaterialSharedAxis(MaterialSharedAxis.X, !isEnter).excludeTarget(R.id.toolBar, true);
        Window window = getWindow();
        window.setEnterTransition(enter);
        window.setExitTransition(exit);
        window.setAllowEnterTransitionOverlap(true);
        window.setAllowReturnTransitionOverlap(true);
    }

    protected void initView() {
        EdgeToEdge.enable(this);
        //动态颜色
        boolean colorSetting = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_system_theme", false);
        if (colorSetting) {
            DynamicColors.applyToActivityIfAvailable(this);
        }

        ViewBinding binding = getBinding();
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }
    }
}
