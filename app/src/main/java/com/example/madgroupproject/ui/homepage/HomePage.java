package com.example.madgroupproject.ui.homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.madgroupproject.R;

public class HomePage extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 确保这里的布局文件名和你的 XML 文件名一致
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 绑定 XML 中的苹果图片 ID
        ImageView appleImage = view.findViewById(R.id.iv_apple);

        // 2. 设置点击跳转
        appleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用你之前确认的跳转 ID
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_appleFragment3);
            }
        });
    }
}