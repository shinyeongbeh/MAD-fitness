package com.example.madgroupproject.ui.settingpage;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.madgroupproject.R;
import com.example.madgroupproject.main.MainActivity;

public class SettingsFragment extends Fragment {

    private NavController settingsNavController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up toolbar
        Toolbar toolbar = view.findViewById(R.id.myToolbar);
        ((MainActivity) requireActivity()).setSupportActionBar(toolbar);

        // Hide default title
        if (((MainActivity) requireActivity()).getSupportActionBar() != null) {
            ((MainActivity) requireActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((MainActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Custom title view
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.setting_custom_toolbar, null);
        TextView title = customView.findViewById(R.id.toolbarTitle);

        // Set up nested navigation
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.NFMainSetting);
        settingsNavController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                settingsNavController.getGraph()
        ).build();

        NavigationUI.setupActionBarWithNavController(
                (AppCompatActivity) requireActivity(),
                settingsNavController,
                appBarConfiguration
        );

        // Update custom title on destination change
        settingsNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String label = destination.getLabel() != null ? destination.getLabel().toString() : "Settings";
            title.setText(label);
        });
    }

    // This method will be called by MainActivity for back press
//    public boolean onBackPressed() {
//        if (settingsNavController != null && settingsNavController.getCurrentDestination().getId() != R.id.settingMainActivity) {
//            settingsNavController.navigateUp();
//            return true;
//        }
//        return false;
//    }
}