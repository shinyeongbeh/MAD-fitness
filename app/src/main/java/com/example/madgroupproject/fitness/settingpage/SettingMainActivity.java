package com.example.madgroupproject.fitness.settingpage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.madgroupproject.R;

public class SettingMainActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.setting_activity_main);

        // Fix toolbar being too high


        // Set up custom toolbar
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ← enable back arrow

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(toolbar.getPaddingLeft(), systemBars.top, toolbar.getPaddingRight(), toolbar.getPaddingBottom());
            return insets;

        });

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.setting_custom_toolbar, null);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(customView);

        TextView title = customView.findViewById(R.id.toolbarTitle);

        // Navigation setup
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.NFMainSetting);
        NavController navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Update title on destination change
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Use custom label from nav_graph or fallback
            String label = destination.getLabel() != null ? destination.getLabel().toString() : "Page Title";
            title.setText(label);




        });
    }

    // Enable back button behavior
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = NavHostFragment.findNavController(
                getSupportFragmentManager().findFragmentById(R.id.NFMainSetting));
        return navController.navigateUp() || super.onSupportNavigateUp();




 /*
        // 1. Get the NavHostFragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.NFMainSetting);

        // 2. Get the NavController from the Fragment
        NavController navController = navHostFragment.getNavController();
        */
        //3. Setup Toolbar
        //Toolbar toolbar = findViewById(R.id.TBMainAct);
        //setSupportActionBar(toolbar);
        //NavigationUI.setupWithNavController(toolbar, navController);


        // 4. Setup Bottom Navigation
        //BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        //NavigationUI.setupWithNavController(bottomNavView, navController);

    }
}
