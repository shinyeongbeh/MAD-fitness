package com.example.madgroupproject.ui.settingpage;

import static kotlinx.serialization.descriptors.ContextAwareKt.withContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.GameProgressDao;
import com.example.madgroupproject.data.local.entity.UserProfile;
import com.example.madgroupproject.data.repository.GameLevelRepository;

import java.util.concurrent.Executors;

import kotlinx.coroutines.Dispatchers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Account#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Account extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Account() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Account.
     */
    // TODO: Rename and change types and number of parameters
    public static Account newInstance(String param1, String param2) {
        Account fragment = new Account();
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
        return inflater.inflate(R.layout.setting_fragment_account, container, false);
    }

    private EditText etName, etEmail, etPhone, etBirthday, etWeight, etHeight;
private Button btnEdit;
private boolean isEditing = false;
private AppDatabase db;
private UserProfile profile;

        private ImageView profileUser , frame ;
private Uri selectedImageUri;

private String profileImageUri;


@Override
public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    etName = view.findViewById(R.id.Username);
    etEmail = view.findViewById(R.id.Email);
    etPhone = view.findViewById(R.id.phone);
    etBirthday = view.findViewById(R.id.Age);
    etWeight = view.findViewById(R.id.Weight);
    etHeight = view.findViewById(R.id.Height);
    btnEdit = view.findViewById(R.id.button_Account_Edit);
    profileUser = view.findViewById(R.id.user);
    frame = view.findViewById(R.id.imgFrame);


    //not allow to edit when not in edit mode
    etName.setEnabled(false);
    etEmail.setEnabled(false);
    etPhone.setEnabled(false);
    etBirthday.setEnabled(false);
    etWeight.setEnabled(false);
    etHeight.setEnabled(false);

    db = AppDatabase.getDatabase(requireContext());
    gameProgressDao = db.gameProgressDao();




    // Run Room query on background thread
    Executors.newSingleThreadExecutor().execute(() -> {
        profile = db.userProfileDao().getProfile();

        if (profile == null) {
            profile = new UserProfile("", "", "", "", "", "",null);
            db.userProfileDao().insert(profile);
        }



        // Update UI on main thread
        requireActivity().runOnUiThread(() -> {

            etName.setText(profile.getName());
            etEmail.setText(profile.getEmail());
            etPhone.setText(profile.getPhone());
            etBirthday.setText(profile.getBirthday());
            etWeight.setText(profile.getWeight());
            etHeight.setText(profile.getHeight());

            String uriString = profile.getProfileImageUri();//from db
            if (uriString != null && !uriString.isEmpty()) {
                Uri uri = Uri.parse(uriString);
                try {
                    profileUser.setImageURI(Uri.parse(uriString));
                } catch (SecurityException e) {
                    profileUser.setImageResource(R.drawable.puppiescouk_053399600_1663317083_2412);
                }

            }


        });
    });



    btnEdit.setOnClickListener(v -> {
        if (!isEditing) {
            //save changes
            etName.setEnabled(true);
            etEmail.setEnabled(true);
            etPhone.setEnabled(true);
            etBirthday.setEnabled(true);
            etWeight.setEnabled(true);
            etHeight.setEnabled(true);
            //upload photo
            profileUser.setEnabled(true);
            profileUser.setAlpha(0.8f); // visual hint

            profileUser.setOnClickListener(img -> {
                imagePicker.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );
            });


            btnEdit.setText("Save");

            isEditing = true;

        } else {
            //enter edit mode

            // Save changes to Room
            profile.setName(etName.getText().toString().trim());
            profile.setEmail(etEmail.getText().toString().trim());
            profile.setPhone(etPhone.getText().toString().trim());
            profile.setBirthday(etBirthday.getText().toString().trim());
            profile.setWeight(etWeight.getText().toString().trim());
            profile.setHeight(etHeight.getText().toString().trim());

            profileUser.setEnabled(false);
            profileUser.setAlpha(1.0f);
            profileUser.setOnClickListener(null);

            // Save profile image URI if user selected a new image
            if (selectedImageUri != null) {
                profile.setProfileImageUri(selectedImageUri.toString());
            }




            // Save to database in background
            Executors.newSingleThreadExecutor().execute(() -> {
                db.userProfileDao().update(profile); //
            });

            etName.setEnabled(false);
            etEmail.setEnabled(false);
            etPhone.setEnabled(false);
            etBirthday.setEnabled(false);
            etWeight.setEnabled(false);
            etHeight.setEnabled(false);
            btnEdit.setText("Edit");

            isEditing = false;

            Toast.makeText(requireContext(), "Profile saved locally!", Toast.LENGTH_SHORT).show();
        }

    });




    // current profile picture
    //profile.setImageResource(R.drawable.profile_pic);

    // Demo frame (always applied)
    frame.setImageResource(R.drawable.frame_1);

    //auto update frame based on current level
    gameProgressDao.getCurrentLevel().observe(getViewLifecycleOwner(), level -> {
        int frameRes = getFrameByLevel(level);
        frame.setImageResource(frameRes);
    });

}



private GameProgressDao gameProgressDao;

//load the frame based on the level (drawable image compile into integer)

private int getFrameByLevel(int level) {
    if (level == 1) return R.drawable.frame_1;
    if (level == 2) return R.drawable.frame_2;
    if (level == 3)  return R.drawable.frame_3;
    if (level == 4)  return R.drawable.frame_4;
    if (level == 5)  return R.drawable.frame_5;
    if(level == 6) return R.drawable.frame_6;
    if(level == 7) return R.drawable.frame_7;
    if(level == 8) return R.drawable.frame_8;
    if(level == 9) return R.drawable.frame_9;
    if(level == 10) return R.drawable.frame_10;
    if(level == 11) return R.drawable.frame_11;
    if(level == 12) return R.drawable.frame_12;
    if(level == 13) return R.drawable.frame_13;
    if(level == 14) return R.drawable.frame_14;
    if(level == 15) return R.drawable.frame_15;
    if(level == 16) return R.drawable.frame_16;
    if(level == 17) return R.drawable.frame_17;
    if(level == 18) return R.drawable.frame_18;
    if(level == 19) return R.drawable.frame_19;
    if(level == 20) return R.drawable.frame_20;


    return R.drawable.frame_1;
}



//for upload image profile
//start photo picker (give temporary permission)
private ActivityResultLauncher<PickVisualMediaRequest> imagePicker =
        registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {

                        //IMPORTANT
                        requireContext().getContentResolver()
                                .takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );

                        selectedImageUri = uri;
                        profileUser.setImageURI(uri);
                    }
                }
        );




}






