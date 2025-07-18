package com.gg.busStation.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewbinding.ViewBinding;

import com.gg.busStation.R;
import com.gg.busStation.databinding.ActivityMainBinding;
import com.gg.busStation.databinding.DialogLoadingBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.SettingsManager;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.location.LocationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private static final String releaseUrl = "https://api.github.com/repos/GaodaGG/HK-BusNow/releases/latest";
    private ActivityMainBinding binding;
    private DialogLoadingBinding dialogBinding;
    private AlertDialog loadingDialog;

    // 权限申请回调
    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (Boolean.FALSE.equals(result)) {
            Toast.makeText(this, R.string.dialog_permission_failed_message, Toast.LENGTH_SHORT).show();
        } else {
            LocationHelper.getLocation(true);
        }
    });

    // 数据初始化监听器
    BusDataManager.OnDataInitListener onDataInitListener = new BusDataManager.OnDataInitListener() {
        @Override
        public void start() {
            runOnUiThread(loadingDialog::show);
        }

        @Override
        public void progress(int now, int max, String tip){
            runOnUiThread(() -> {
                dialogBinding.updateIndicator.setMax(max);
                dialogBinding.updateIndicator.setMin(0);
                dialogBinding.updateIndicator.setProgress(now, true);
                dialogBinding.updateProgress.setText(now + " / " + max);
                dialogBinding.updateTips.setText(tip);
            });
        }

        @Override
        public void finish(boolean status) {
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                checkPermissions();

                SettingsManager settingsManager = SettingsManager.getInstance(MainActivity.this);
                settingsManager.setLastUpdateTime(System.currentTimeMillis());
                settingsManager.setInit(true);

                if (!status) {
                    Toast.makeText(MainActivity.this, R.string.error_getdata, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //防止ContentFrameLayout.setDecorPadding()报错
        getWindow().getDecorView();

        super.onCreate(savedInstanceState);

        setTransition(false);

        initView();
        if (savedInstanceState != null) {
            binding.bottomNavigation.setSelectedItemId(savedInstanceState.getInt("bottomNavigation"));
        } else {
            new Thread(() -> checkAppUpdate(false)).start();
        }
    }

    @Override
    ViewBinding getBinding() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        return binding;
    }

    @Override
    protected void initView() {
        super.initView();
        setSupportActionBar(binding.toolBar);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
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

        dialogBinding = DialogLoadingBinding.inflate(getLayoutInflater());
        loadingDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_loading)
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(() -> BusDataManager.initData(this, onDataInitListener, false)).start();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("bottomNavigation", binding.bottomNavigation.getSelectedItemId());
        super.onSaveInstanceState(outState);
    }

    public void checkPermissions() {
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        if (settingsManager.isInit()) {
            return;
        }

        int selfPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (selfPermission == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_permission_title)
                .setMessage(R.string.dialog_permission_location_message)
                .setNegativeButton(R.string.dialog_permission_decline, (dialog, which) -> {
                })
                .setPositiveButton(R.string.dialog_permission_accept, (dialog, which) -> requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                .show();
    }

    public boolean checkAppUpdate(boolean checkNow) {
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        if (!checkNow && !settingsManager.isAutoUpdateApp()) {
            return false;
        }

        String data;
        String oldVersion;
        try {
            data = HttpClientHelper.getData(releaseUrl);
            oldVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (IOException | PackageManager.NameNotFoundException e) {
            return false;
        }

        JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
        JsonElement tagName = jsonObject.get("tag_name");
        if (tagName == null) {
            return false;
        }

        String version = tagName.getAsString();
        if (oldVersion != null && oldVersion.equals(version.substring(1))) {
            return false;
        }

        String abi = Build.SUPPORTED_ABIS[0];
        int index = switch (abi) {
            case "arm64-v8a" -> 0;
            case "armeabi-v7a" -> 1;
            default -> 2;
        };
        Log.d("AppUpdate", "App ABI: " + abi);

        String updateContent = jsonObject.get("body").getAsString();
        String downloadUrl = jsonObject.getAsJsonArray("assets").get(index).getAsJsonObject().get("browser_download_url").getAsString();
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.dialog_update_title) + " " + version)
                .setMessage(getString(R.string.dialog_update_message) + "\n" + updateContent)
                .setNeutralButton(R.string.dialog_update_never, (dialogInterface, i) -> settingsManager.setUpdateApp(false))
                .setNegativeButton(R.string.dialog_update_no, null)
                .setPositiveButton(R.string.dialog_update_yes, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    startActivity(intent);
                });
        runOnUiThread(dialog::show);
        return true;
    }
}
