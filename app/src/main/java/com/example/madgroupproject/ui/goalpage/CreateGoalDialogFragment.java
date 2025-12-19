package com.example.madgroupproject.ui.goalpage;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.madgroupproject.R;

public class CreateGoalDialogFragment extends DialogFragment {

    private static final String ARG_GOAL_NAME = "goal_name";
    private static final String ARG_GOAL_LABEL = "goal_label";
    private static final String ARG_GOAL_POSITION = "goal_position";
    private static final String ARG_SUGGESTED_NAME = "suggested_name";
    private static final String ARG_SUGGESTED_LABEL = "suggested_label";

    private EditText etGoalName;
    private Spinner spinnerLabel;
    private TextView tvReminderTime;
    private SwitchCompat switchReminder;
    private Button btnSaveGoal, btnDeleteGoal;

    private int selectedHour = 18;
    private int selectedMinute = 30;
    private boolean isEditMode = false;
    private int goalPosition = -1;

    public static CreateGoalDialogFragment newInstance() {
        return new CreateGoalDialogFragment();
    }

    public static CreateGoalDialogFragment newEditInstance(String name, String label, int position) {
        CreateGoalDialogFragment fragment = new CreateGoalDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GOAL_NAME, name);
        args.putString(ARG_GOAL_LABEL, label);
        args.putInt(ARG_GOAL_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateGoalDialogFragment newSuggestedInstance(String name, String label) {
        CreateGoalDialogFragment fragment = new CreateGoalDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUGGESTED_NAME, name);
        args.putString(ARG_SUGGESTED_LABEL, label);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppDialogTheme); // Optional: custom style
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_goal_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupSpinner();
        loadArguments();
        setupListeners();
        updateTimeDisplay();
    }

    private void initViews(View view) {
        etGoalName = view.findViewById(R.id.etGoalName);
        spinnerLabel = view.findViewById(R.id.spinnerLabel);
        tvReminderTime = view.findViewById(R.id.tvReminderTime);
        switchReminder = view.findViewById(R.id.switchReminder);
        btnSaveGoal = view.findViewById(R.id.btnSaveGoal);
        btnDeleteGoal = view.findViewById(R.id.btnDeleteGoal);

        // Hide delete button by default
        btnDeleteGoal.setVisibility(View.GONE);
    }

    private void setupSpinner() {
        String[] labels = {"Exercise", "Habit", "Relax", "Work", "Study", "Health"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabel.setAdapter(adapter);
    }

    private void loadArguments() {
        Bundle args = getArguments();
        if (args == null) return;

        if (args.containsKey(ARG_GOAL_POSITION)) {
            isEditMode = true;
            goalPosition = args.getInt(ARG_GOAL_POSITION);
            String name = args.getString(ARG_GOAL_NAME);
            String label = args.getString(ARG_GOAL_LABEL);

            etGoalName.setText(name);
            setSpinnerValue(label);
            btnDeleteGoal.setVisibility(View.VISIBLE);
        }

        if (args.containsKey(ARG_SUGGESTED_NAME)) {
            String name = args.getString(ARG_SUGGESTED_NAME);
            String label = args.getString(ARG_SUGGESTED_LABEL);
            etGoalName.setText(name);
            setSpinnerValue(label);
        }
    }

    private void setSpinnerValue(String label) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerLabel.getAdapter();
        int position = adapter.getPosition(label);
        if (position >= 0) {
            spinnerLabel.setSelection(position);
        }
    }

    private void setupListeners() {
        tvReminderTime.setOnClickListener(v -> showTimePicker());
        btnSaveGoal.setOnClickListener(v -> saveGoal());
        btnDeleteGoal.setOnClickListener(v -> deleteGoal());

        // Optional: close on back icon if you add one
        View btnBack = getView().findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> dismiss());
        }
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeDisplay();
                },
                selectedHour,
                selectedMinute,
                true
        );
        dialog.show();
    }

    private void updateTimeDisplay() {
        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
        tvReminderTime.setText(time);
    }

    private void saveGoal() {
        String name = etGoalName.getText().toString().trim();
        String label = spinnerLabel.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a goal name", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle result = new Bundle();
        result.putString("goal_name", name);
        result.putString("goal_label", label);
        if (isEditMode) {
            result.putInt("goal_position", goalPosition);
        }

        getParentFragmentManager().setFragmentResult("goal_request", result);
        dismiss();
    }

    private void deleteGoal() {
        Bundle result = new Bundle();
        result.putBoolean("goal_deleted", true);
        result.putInt("goal_position", goalPosition);

        getParentFragmentManager().setFragmentResult("goal_request", result);
        dismiss();
    }
}