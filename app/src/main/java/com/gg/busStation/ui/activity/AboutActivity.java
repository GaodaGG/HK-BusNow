package com.gg.busStation.ui.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.gg.busStation.R;
import com.gg.busStation.databinding.ActivityAboutBinding;
import com.gg.busStation.databinding.ItemAboutReferencesBinding;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.transition.platform.MaterialSharedAxis;

public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;

    private static final String[] referencesName = new String[]{"Google/Gson", "square/OKHttp3", "Google/Material Design", "Baidu/Baidu Location"};
    private static final String[] referencesLink = new String[]{"https://github.com/google/gson/", "https://github.com/square/okhttp/", "https://material.io/", "https://lbsyun.baidu.com/location/"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTransition();
        initView();
    }

    private void setTransition() {
        Transition enter = new MaterialSharedAxis(MaterialSharedAxis.X, true).excludeTarget(R.id.toolBar, true);
        Transition exit = new MaterialSharedAxis(MaterialSharedAxis.X, false).excludeTarget(R.id.toolBar, true);
        Window window = getWindow();
        window.setEnterTransition(enter);
        window.setExitTransition(exit);
        window.setAllowEnterTransitionOverlap(true);
        window.setAllowReturnTransitionOverlap(true);
    }

    private void initView() {
        EdgeToEdge.enable(this);
        //动态颜色
        boolean colorSetting = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("settings_system_theme", false);
        if (colorSetting) {
            DynamicColors.applyToActivityIfAvailable(this);
        }

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        }

        setSupportActionBar(binding.toolBar);
        binding.toolBar.setNavigationIcon(com.google.android.material.R.drawable.abc_ic_ab_back_material);
        binding.toolBar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        binding.authorLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/GaodaGG"));
            startActivity(intent);
        });

        initReferences();
    }

    private void initReferences() {
        for (int i = 0; i < referencesName.length; i++) {
            ItemAboutReferencesBinding referencesBinding = ItemAboutReferencesBinding.inflate(getLayoutInflater(), binding.referencesList, true);
            referencesBinding.itemReferencesName.setText(referencesName[i]);
            referencesBinding.itemReferencesLink.setText(referencesLink[i]);

            ConstraintLayout layout = referencesBinding.getRoot();
            int finalI = i;
            layout.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(referencesLink[finalI]));
                startActivity(intent);
            });
        }
    }

}
