package com.gg.busStation.ui.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;

import com.gg.busStation.databinding.ActivityAboutBinding;
import com.gg.busStation.databinding.ItemAboutReferencesBinding;

public class AboutActivity extends BaseActivity {
    ActivityAboutBinding binding;

    private static final String[] referencesName = new String[]{"Google/Gson", "square/OKHttp3", "Google/Material Design", "Baidu/Baidu Location"};
    private static final String[] referencesLink = new String[]{"https://github.com/google/gson/", "https://github.com/square/okhttp/", "https://material.io/", "https://lbsyun.baidu.com/location/"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTransition(true);
        initView();
    }

    @Override
    ViewBinding getBinding() {
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        return binding;
    }

    @Override
    protected void initView() {
        super.initView();
        setSupportActionBar(binding.toolBar);

        binding.toolBar.setNavigationIcon(com.google.android.material.R.drawable.ic_arrow_back_black_24);
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
