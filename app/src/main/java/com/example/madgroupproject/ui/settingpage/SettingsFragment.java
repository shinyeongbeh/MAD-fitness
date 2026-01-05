package com.example.madgroupproject.ui.settingpage;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // custom toolbar inside fragment layout
        TextView title = view.findViewById(R.id.toolbarTitle);
        if (title != null) {
            title.setText("Settings"); // initial title
        }

        // Setup nested navigation safely
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.NFMainSetting);
        if (fragment instanceof NavHostFragment) {
            settingsNavController = ((NavHostFragment) fragment).getNavController();

            settingsNavController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (title != null) {
                    String label = destination.getLabel() != null ? destination.getLabel().toString() : "Settings";
                    title.setText(label);
                }
            });
        } else {
            Log.w("SettingsFragment", "NFMainSetting fragment not found or is not NavHostFragment");
        }

    }




}

