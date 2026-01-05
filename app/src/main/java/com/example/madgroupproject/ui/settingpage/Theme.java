package com.example.madgroupproject.ui.settingpage;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.example.madgroupproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Theme#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Theme extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Theme() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Theme.
     */
    // TODO: Rename and change types and number of parameters
    public static Theme newInstance(String param1, String param2) {
        Theme fragment = new Theme();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.setting_fragment_theme, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);


        Switch switchDark= view.findViewById(R.id.Dark);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        int savedMode = prefs.getInt(
                "theme_mode",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        switchDark.setChecked(
                savedMode == AppCompatDelegate.MODE_NIGHT_YES
        );

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setTheme(isChecked);
        });


    }



    private void setTheme(boolean darkMode) {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        //switch on
        if (darkMode) {
            prefs.edit()
                    .putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES)
                    .apply();

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );
        } else {
            prefs.edit()
                    .putInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
                    .apply();

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );
        }
    }

}