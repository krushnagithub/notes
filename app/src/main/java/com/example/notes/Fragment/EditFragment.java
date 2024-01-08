package com.example.notes.Fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.notes.R;
import com.example.notes.database.NoteEntity;
import com.example.notes.databinding.FragmentEditBinding;
import com.example.notes.viewmodel.NoteViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditFragment extends Fragment {
    private String imagePath; // Add this variable

    private FragmentEditBinding binding;
    private NoteViewModel noteViewModel;
    private long noteId;
    private Bitmap selectedImage;
    private static final int REQUEST_IMAGE_PICK = 101;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar selectedDate = Calendar.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditBinding.inflate(inflater, container, false);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        initData();
        setupListeners();
        return binding.getRoot();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            noteId = bundle.getLong("noteId", 0);
            String title = bundle.getString("title", "");
            String date = bundle.getString("date", "");
            String content = bundle.getString("content", "");

            binding.editTextTitle.setText(title);
            binding.textViewDate.setText(date);
            binding.editTextNote.setText(content);

            byte[] imageByteArray = bundle.getByteArray("imageBitmap");
            if (imageByteArray != null && imageByteArray.length > 0) {
                selectedImage = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                binding.imageView.setImageBitmap(selectedImage);
                binding.imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupListeners() {
        binding.textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        binding.buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditedNote();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateText();

                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
    private void updateDateText() {
        binding.textViewDate.setText(dateFormat.format(selectedDate.getTime()));
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), data.getData());
                binding.imageView.setImageBitmap(selectedImage);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveEditedNote() {
        // Retrieve the new title and note content from the UI
        String newTitle = binding.editTextTitle.getText().toString().trim();
        String newNoteContent = binding.editTextNote.getText().toString().trim();

        // Check if the new title and note content are not empty
        if (!newTitle.isEmpty() && !newNoteContent.isEmpty()) {
            // Check if the noteId is valid (greater than 0)
            if (noteId > 0) {
                // Retrieve the existing note from the ViewModel using the noteId
                LiveData<NoteEntity> existingNoteLiveData = noteViewModel.getNoteById(noteId);
                existingNoteLiveData.observe(getViewLifecycleOwner(), existingNote -> {
                    // Check if the existing note is not null
                    if (existingNote != null) {
                        // Update the existing note with the new title and content
                        existingNote.setTitle(newTitle);
                        existingNote.setContent(newNoteContent);

                        // Check if a new image is selected
                        if (imagePath != null) {
                            // Update the note's image path
                            existingNote.setImagePath(imagePath);
                        }

                        // Get the current date
                        Date currentDate = Calendar.getInstance().getTime();
                        existingNote.setDate(currentDate); // Set the updated date

                        // Save the updated note to the database using the ViewModel
                        noteViewModel.update(existingNote);

                        // Remove the observer to prevent multiple updates
                        existingNoteLiveData.removeObservers(getViewLifecycleOwner());

                        // Show a success message using Toast
                        showToast("Note updated successfully");

                        // Navigate back to the notesFragment
                        NavController navController = NavHostFragment.findNavController(EditFragment.this);
                        navController.popBackStack();
                    } else {
                        // Log an error and show a message
                        Log.e("EditFragment", "Existing note is null");
                        showToast("Error: Note not found");
                    }
                });
            } else {
                // Log an error and show a message
                Log.e("EditFragment", "Invalid noteId");
                showToast("Error: Invalid noteId");
            }
        } else {
            // Handle the case where fields are empty
            showToast("Error: Title and content cannot be empty");
        }
    }



    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}