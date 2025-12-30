package com.example.madgroupproject.ui.settingpage;

import static kotlinx.serialization.descriptors.ContextAwareKt.withContext;

import android.os.Bundle;

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
import com.example.madgroupproject.data.local.entity.UserProfile;

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

        db = AppDatabase.getDatabase(requireContext());



        //load current profile

        /*profile = db.userProfileDao().getProfile();
        if (profile != null) {
            etName.setText(profile.getName());
            etEmail.setText(profile.getEmail());
        } else {
            // First time, create empty profile
            profile = new UserProfile(" ", " ", " ", " ", " ", "");
            db.userProfileDao().insert(profile);

        }*/

        // Run Room query on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
             profile = db.userProfileDao().getProfile();

            if (profile == null) {
                profile = new UserProfile("", "", "", "", "", "");
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

                // Run DB update on background thread
                Executors.newSingleThreadExecutor().execute(() -> db.userProfileDao().update(profile));


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



        //Frame

        profileUser = view.findViewById(R.id.user);
        frame = view.findViewById(R.id.imgFrame);

        // current profile picture
        //profile.setImageResource(R.drawable.profile_pic);

        // Demo frame (always applied)
        frame.setImageResource(R.drawable.frame_1);

    }


}






