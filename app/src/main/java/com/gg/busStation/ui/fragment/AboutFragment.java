package com.gg.busStation.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.gg.busStation.databinding.FragmentAboutBinding;
import com.gg.busStation.databinding.ItemAboutReferencesBinding;

public class AboutFragment extends Fragment {
    private FragmentAboutBinding binding;

    private static final String[] referencesName = new String[]{"Google/Gson", "square/OKHttp3", "Google/Material Design", "Baidu/Baidu Location"};
    private static final String[] referencesLink = new String[]{"https://github.com/google/gson/", "https://github.com/square/okhttp/", "https://material.io/", "https://lbsyun.baidu.com/location/"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
