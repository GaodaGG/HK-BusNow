package com.gg.busStation.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.gg.busStation.R;
import com.gg.busStation.data.bus.Route;
import com.gg.busStation.data.layout.ListItemData;
import com.gg.busStation.databinding.ActivityMainBinding;
import com.gg.busStation.function.DataBaseManager;
import com.gg.busStation.function.location.LocationHelper;
import com.gg.busStation.ui.adapter.MainAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            LocationHelper.init(this);
        } catch (Exception e) {
            Toast.makeText(this, "无法获取位置信息", Toast.LENGTH_SHORT).show();
        }

        DataBaseManager.initDB(this);
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar_menu, menu);
        MenuItem item = menu.findItem(R.id.search_toolbar_item);
        SearchView searchView = (SearchView) item.getActionView();
        if (searchView == null) {
            return false;
        }

        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setRouteList(newText);

                return false;
            }
        });

        return true;
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

            MenuItem menuItem = binding.toolBar.getMenu().findItem(R.id.search_fragment);
            if (item.getItemId() != R.id.home_fragment) {
                menuItem.setVisible(false);
                menuItem.collapseActionView();
            } else {
                menuItem.setVisible(true);
            }
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

    private void setRouteList(String newText) {
        List<Route> routes;
        if (newText.isEmpty()) {
            routes = DataBaseManager.getRoutes(50);
        } else {
            routes = DataBaseManager.getRoutes(newText);
        }

        RecyclerView recyclerView = findViewById(R.id.bus_list_view);
        MainAdapter adapter = (MainAdapter) recyclerView.getAdapter();
        List<ListItemData> data = new ArrayList<>();
        for (Route route : routes) {
            ListItemData listItemData = new ListItemData(route.getRoute(), route.getBound(), route.getOrig("zh_CN") + " -> " + route.getDest("zh_CN"), route.getBound(), route.getService_type());
            data.add(listItemData);
        }

        if (adapter != null) {
            adapter.submitList(data);
        }
    }
}
