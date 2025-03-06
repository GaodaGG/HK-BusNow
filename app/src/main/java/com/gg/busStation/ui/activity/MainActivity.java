package com.gg.busStation.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gg.busStation.R;
import com.gg.busStation.databinding.ActivityMainBinding;
import com.gg.busStation.function.BusDataManager;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.internet.HttpClientHelper;
import com.gg.busStation.function.location.LocationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final String releaseUrl = "https://api.github.com/repos/GaodaGG/HK-BusNow/releases/latest";
    // 权限申请回调
    private final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (Boolean.FALSE.equals(result)) {
            Toast.makeText(this, "权限授权失败，请手动给予", Toast.LENGTH_SHORT).show();
        } else {
            LocationHelper.getLocation(true);
        }
    });
    private AlertDialog loadingDialog;
    // 数据初始化监听器
    BusDataManager.OnDataInitListener onDataInitListener = new BusDataManager.OnDataInitListener() {
        @Override
        public void start() {
            runOnUiThread(loadingDialog::show);
        }

        @Override
        public void finish() {
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                checkPermissions();
            });
        }
    };
    private ActivityMainBinding binding;

    private static boolean isMIUI() {
        String miuiName;
        try {
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class);
            miuiName = (String) get.invoke(clazz, "ro.miui.ui.version.name");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            return false;
        }

        return miuiName != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //防止ContentFrameLayout.setDecorPadding()报错
        getWindow().getDecorView();

        super.onCreate(savedInstanceState);
        initView();
        new Thread(this::checkAppUpdate).start();
    }

    private void initView() {
        //解决MIUI小白条问题
        if (isMIUI()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        setSupportActionBar(binding.toolBar);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
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

        loadingDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_loading)
                .setView(R.layout.dialog_loading)
                .setCancelable(false)
                .create();

        new Thread(() -> {
            try {
                BusDataManager.initData(onDataInitListener);
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.error_getdata, Toast.LENGTH_SHORT).show());
            }
//                BusDataManager.getNearRoutes(100);
        }).start();
    }

    public void checkPermissions() {
        if ("true".equals(DataBaseManager.getSettings().get("isInit"))) {
            return;
        }

        int selfPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (selfPermission == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_permission_title)
                .setMessage(R.string.dialog_permission_message)
                .setNegativeButton(R.string.dialog_permission_decline, (dialog, which) -> {
                })
                .setPositiveButton(R.string.dialog_permission_accept, (dialog, which) -> requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                .show();

        DataBaseManager.updateSetting("isInit", "true");
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
