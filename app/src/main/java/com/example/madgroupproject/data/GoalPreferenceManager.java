package com.example.madgroupproject.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.example.madgroupproject.ui.goalpage.GoalFragment.Goal;

public class GoalPreferenceManager {
    private static final String PREF_NAME = "GoalsData";
    private static final String KEY_GOALS = "goals_list";

    private final SharedPreferences prefs;
    private final Gson gson;

    public GoalPreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<Goal> loadGoals() {
        String json = prefs.getString(KEY_GOALS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Goal>>(){}.getType();
            return gson.fromJson(json, type);
        }
        return createDefaultGoals();
    }

    public void saveGoals(List<Goal> goals) {
        String json = gson.toJson(goals);
        prefs.edit().putString(KEY_GOALS, json).apply();
    }

    private List<Goal> createDefaultGoals() {
        List<Goal> defaults = new ArrayList<>();
        defaults.add(new Goal("Walk the dog", "Exercise", getIconForLabel("Exercise"), false));
        defaults.add(new Goal("Drink 8 glass water", "Habit", getIconForLabel("Habit"), false));
        defaults.add(new Goal("Listening to Podcast", "Relax", getIconForLabel("Relax"), false));
        return defaults;
    }

    public int getIconForLabel(String label) {
        switch (label) {
            case "Exercise": return com.example.madgroupproject.R.drawable.ic_exercise;
            case "Habit": return com.example.madgroupproject.R.drawable.ic_water;
            case "Relax": return com.example.madgroupproject.R.drawable.ic_podcast;
            case "Work": return com.example.madgroupproject.R.drawable.ic_work;
            case "Study": return com.example.madgroupproject.R.drawable.ic_study;
            case "Health": return com.example.madgroupproject.R.drawable.ic_health;
            default: return com.example.madgroupproject.R.drawable.ic_exercise;
        }
    }
}
