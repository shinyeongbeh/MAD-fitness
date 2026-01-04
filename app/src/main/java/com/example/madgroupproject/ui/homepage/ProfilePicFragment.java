package com.example.madgroupproject.ui.homepage;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.GameProgressDao;
import com.example.madgroupproject.data.local.entity.UserProfile;

import java.util.concurrent.Executors;

public class ProfilePicFragment extends Fragment {
    public ImageView frame;
    public ImageView profilePic;
    private AppDatabase db;
    private GameProgressDao gameProgressDao;
    private UserProfile profile;

    public ProfilePicFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_profile_pic_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        frame = view.findViewById(R.id.imgFrame);
        profilePic = view.findViewById(R.id.user);


        db = AppDatabase.getDatabase(requireContext());
        gameProgressDao = db.gameProgressDao();

        Executors.newSingleThreadExecutor().execute(() -> {
            profile = db.userProfileDao().getProfile();
                requireActivity().runOnUiThread(() -> {
                    if(profile!=null) {
                        String profilePicUri = profile.getProfileImageUri();
                        profilePic.setImageURI(Uri.parse(profilePicUri));
                    }
                });
        });



//            // Update UI on main thread
//            requireActivity().runOnUiThread(() -> {
//
//                etName.setText(profile.getName());
//                etEmail.setText(profile.getEmail());
//                etPhone.setText(profile.getPhone());
//                etBirthday.setText(profile.getBirthday());
//                etWeight.setText(profile.getWeight());
//                etHeight.setText(profile.getHeight());
//
//                String uriString = profile.getProfileImageUri();//from db
//                if (uriString != null && !uriString.isEmpty()) {
//                    Uri uri = Uri.parse(uriString);
//                    try {
//                        profileUser.setImageURI(Uri.parse(uriString));
//                    } catch (SecurityException e) {
//                        profileUser.setImageResource(R.drawable.puppiescouk_053399600_1663317083_2412);
//                    }
//
//                }
//

//            });


        // current profile picture
//    profile.setImageResource(R.drawable.profile_pic);

        // Demo frame (always applied)
        frame.setImageResource(R.drawable.frame_1);

        //auto update frame based on current level
        gameProgressDao.getCurrentLevel().observe(getViewLifecycleOwner(), level -> {
            int frameRes = getFrameByLevel(level);
            frame.setImageResource(frameRes);
        });

    }


    private int getFrameByLevel(int level) {
        if (level == 1) return R.drawable.frame_1;
        if (level == 2) return R.drawable.frame_2;
        if (level == 3) return R.drawable.frame_3;
        if (level == 4) return R.drawable.frame_4;
        if (level == 5) return R.drawable.frame_5;
        if (level == 6) return R.drawable.frame_6;
        if (level == 7) return R.drawable.frame_7;
        if (level == 8) return R.drawable.frame_8;
        if (level == 9) return R.drawable.frame_9;
        if (level == 10) return R.drawable.frame_10;
        if (level == 11) return R.drawable.frame_11;
        if (level == 12) return R.drawable.frame_12;
        if (level == 13) return R.drawable.frame_13;
        if (level == 14) return R.drawable.frame_14;
        if (level == 15) return R.drawable.frame_15;
        if (level == 16) return R.drawable.frame_16;
        if (level == 17) return R.drawable.frame_17;
        if (level == 18) return R.drawable.frame_18;
        if (level == 19) return R.drawable.frame_19;
        if (level == 20) return R.drawable.frame_20;


        return R.drawable.frame_1;
    }
}