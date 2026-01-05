package com.example.madgroupproject.ui.settingpage;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.madgroupproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingPage extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingPage.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingPage newInstance(String param1, String param2) {
        SettingPage fragment = new SettingPage();
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
        return inflater.inflate(R.layout.fragment_setting_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        //All navigation

        //App
        Button btnApp = view.findViewById(R.id.button_App_SP);
        View.OnClickListener OCLApp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.toApp);
            }
        };
        btnApp.setOnClickListener(OCLApp);


        //Account
        Button btnAccount = view.findViewById(R.id.button_Account_SP);
        View.OnClickListener OCLAcc = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.toAccount);
            }
        };
        btnAccount.setOnClickListener(OCLAcc);


        //Notification
        Button btnNotification = view.findViewById(R.id.button_Notification_SP);
        View.OnClickListener OCLNoc = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.toNotification2);
            }
        };
        btnNotification.setOnClickListener(OCLNoc);

        //not use
        //Privacy
        Button btnPrivacy = view.findViewById(R.id.button_Privacy_SP);
        View.OnClickListener OCLPri = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.toPrivacy);
            }
        };
        btnPrivacy.setOnClickListener(OCLPri);

        //Theme
        Button btnTheme = view.findViewById(R.id.button_Theme_SP);
        View.OnClickListener OCLThe = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.toTheme);
            }
        };
        btnTheme.setOnClickListener(OCLThe);

        //not use
        //Sync
        Button btnSync = view.findViewById(R.id.button_Sync_SP);
        View.OnClickListener OCLSync = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.toSync2);
            }
        };
        btnSync.setOnClickListener(OCLSync);





    }

}
