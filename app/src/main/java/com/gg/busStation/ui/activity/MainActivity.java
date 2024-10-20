package com.gg.busStation.ui.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gg.busStation.R;
import com.gg.busStation.databinding.ActivityMainBinding;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String releaseUrl = "https://api.github.com/repos/GaodaGG/HK-BusNow/releases/latest";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        new Thread(this::checkAppUpdate).start();
    }

    private void initView() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        setSupportActionBar(binding.toolBar);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            NavigationUI.onNavDestinationSelected(item, navController);
            binding.toolBar.setNavigationIcon(null);

            return true;
        });

        //返回键监听
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.bottomNavigation.getSelectedItemId() != R.id.home_fragment) {
                    binding.bottomNavigation.setSelectedItemId(R.id.home_fragment);
                    return;
                }

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });
    }

    private void checkAppUpdate() {
        String dontUpdate = DataBaseManager.getSettings().get("dontUpdate");
        if ("true".equals(dontUpdate)) {
            return;
        }

        String data;
        String oldVersion;
        try {
            data = HttpClientHelper.getData(releaseUrl);
            oldVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (IOException | PackageManager.NameNotFoundException e) {
            return;
        }

        JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
        String version = jsonObject.get("tag_name").getAsString();
        if (oldVersion.equals(version.substring(1))) {
            return;
        }

        String downloadUrl = jsonObject.getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_update_title)
                .setMessage(R.string.dialog_update_message)
                .setNeutralButton(R.string.dialog_update_never, (dialogInterface, i) -> DataBaseManager.updateSetting("dontUpdate", "true"))
                .setNegativeButton(R.string.dialog_update_no, (dialogInterface, i) -> {
                })
                .setPositiveButton(R.string.dialog_update_yes, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    startActivity(intent);
                });
        runOnUiThread(dialog::show);
    }
}
