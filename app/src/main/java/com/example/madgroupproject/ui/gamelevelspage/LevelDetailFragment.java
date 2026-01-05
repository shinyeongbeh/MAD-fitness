package com.example.madgroupproject.ui.gamelevelspage;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.madgroupproject.R;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.GameProgressEntity;
import com.example.madgroupproject.data.local.entity.UserProfile;
import com.example.madgroupproject.data.viewmodel.GameLevelViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class LevelDetailFragment extends Fragment {
    private TextView levelTitleTV, levelNumTV, levelDescTV, levelPercentageTV, levelDateTV;
    private ImageView levelFrameIV, levelProfileIV;
    private GameLevelViewModel viewModel;
    int levelNumber=1;
    ProgressBar levelProgressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.level_details, container, false);

        levelTitleTV = view.findViewById(R.id.idTVLevelName);
        levelNumTV = view.findViewById(R.id.idTVLevelNum);
        levelDescTV = view.findViewById(R.id.idTVDetail);
        levelFrameIV = view.findViewById(R.id.idIVFrame);
        levelPercentageTV = view.findViewById(R.id.idTVPercentage);
        levelDateTV = view.findViewById(R.id.idTVDate);
        levelProfileIV = view.findViewById(R.id.user);
        levelProgressBar = view.findViewById(R.id.progressBar);
        CardView cardView = view.findViewById(R.id.cardView);
        Button shareButton = view.findViewById(R.id.shareButton);

        viewModel = new ViewModelProvider(this).get(GameLevelViewModel.class);

        // static data from bundle
        if (getArguments() != null && getArguments().containsKey("LEVEL_NUMBER")) {
            levelNumber = getArguments().getInt("LEVEL_NUMBER", 1);
            levelTitleTV.setText(getArguments().getString("LEVEL_TITLE", ""));
            levelDescTV.setText(getArguments().getString("LEVEL_DESC", ""));

            int img = getArguments().getInt("LEVEL_FRAME", R.drawable.apples);
            levelFrameIV.setImageResource(img);

            levelNumTV.setText("Level " + String.valueOf(levelNumber));

        }

        // Completion date
        viewModel.observeHistoryForLevel(levelNumber)
                .observe(getViewLifecycleOwner(), history -> {
                    if (history != null) {
                        levelDateTV.setText(history.completedDate);
                    }
                });

        //level percentage
        viewModel.getLevel(levelNumber).observe(getViewLifecycleOwner(), level -> {
            if (level == null) return;
            viewModel.observeProgress().observe(getViewLifecycleOwner(), progress -> {
                if (progress == null) return;
                levelProgressBar.setVisibility(View.GONE);
                levelDateTV.setVisibility(View.GONE);
                levelPercentageTV.setVisibility(View.GONE);

                // NOT STARTED
                if (progress.currentLevel < levelNumber) {
                    return;
                }
                // IN PROGRESS
                else if (progress.currentLevel == levelNumber) {
                    float percent = (progress.progressValue / level.targetValue) * 100f;
                    levelPercentageTV.setVisibility(View.VISIBLE);
                    levelPercentageTV.setText(String.format("%.1f%%", percent));
                    //progress bar
                    levelProgressBar.setVisibility(View.VISIBLE);
                    levelProgressBar.setProgress((int) percent);
                    return;
                }
                // COMPLETED
                else {
                    levelPercentageTV.setVisibility(View.VISIBLE);
                    levelPercentageTV.setText("100%");
                    //progress bar
                    levelProgressBar.setVisibility(View.VISIBLE);
                    levelProgressBar.setProgress(100);
                    levelDateTV.setVisibility(View.VISIBLE);
                }

            });
        });

        //sync profile pic
        AppDatabase db = AppDatabase.getDatabase(getContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            UserProfile profile = db.userProfileDao().getProfile();

            if (profile != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Load profile image from URI
                    String uriString = profile.getProfileImageUri();
                    if (uriString != null && !uriString.isEmpty()) {
                        levelProfileIV.post(() -> {
                            try {
                                Bitmap original = MediaStore.Images.Media.getBitmap(
                                        requireContext().getContentResolver(),
                                        Uri.parse(uriString)
                                );
                                Bitmap circular = toCircularBitmap(original);
                                levelProfileIV.setImageBitmap(circular);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    }

                    // Optionally set other info
                    // levelNameTV.setText(profile.getName());
                });
            }
        });

        //share button
        shareButton.setOnClickListener(v -> shareCard(cardView));

        return view;
    }

    //share button logic
    // Convert CardView to Bitmap and share
    private void shareCard(View cardView) {
        // 1. Convert view to bitmap
        Bitmap bitmap = Bitmap.createBitmap(cardView.getWidth(), cardView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        cardView.draw(canvas);

        // 2. Save bitmap to cache
        File cachePath = new File(getContext().getCacheDir(), "images");
        cachePath.mkdirs();
        File file = new File(cachePath, "card.png");
        try (FileOutputStream stream = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 3. Get URI using FileProvider
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);

        // 4. Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Level"));
    }

    //remove black corners
    private Bitmap toCircularBitmap(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect src = new Rect(
                (source.getWidth() - size) / 2,
                (source.getHeight() - size) / 2,
                (source.getWidth() + size) / 2,
                (source.getHeight() + size) / 2
        );

        Rect dst = new Rect(0, 0, size, size);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, src, dst, paint);

        return output;
    }

}
