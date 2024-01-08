package com.example.notes.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.notes.databinding.FragmentDetailBinding;

public class DetailFragment extends Fragment {

    private FragmentDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        initData();
        return binding.getRoot();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title", "not Available");
            String date = bundle.getString("date", "not Available");
            String content = bundle.getString("content", "not Available");

            binding.titleTextView.setText(title);
            binding.dateTextView.setText(date);
            binding.contentTextView.setText(content);

            int defaultImageResource = bundle.getInt("imageResource", 0);
            if (defaultImageResource != 0) {
                binding.imageViewDetail.setImageResource(defaultImageResource);
                binding.imageViewDetail.setVisibility(View.VISIBLE);
            } else {
                // No image resource provided, hide the ImageView
                binding.imageViewDetail.setVisibility(View.GONE);
            }

            byte[] imageByteArray = bundle.getByteArray("imageBitmap");
            if (imageByteArray != null) {
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                binding.imageViewDetail.setImageBitmap(imageBitmap);
                binding.imageViewDetail.setVisibility(View.VISIBLE);
            }

        }
    }
}
