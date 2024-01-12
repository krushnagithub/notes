package com.example.notes.Fragment;

import static com.example.notes.Fragment.notesFragment.noteAdapter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.notes.R;
import com.example.notes.adapter.NoteAdapter;
import com.example.notes.database.NoteEntity;
import com.example.notes.databinding.FragmentEditBinding;
import com.example.notes.viewmodel.NoteModel;
import com.example.notes.viewmodel.NoteViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    private NoteAdapter noteAdapter;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditBinding.inflate(inflater, container, false);
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        noteAdapter = notesFragment.noteAdapter;
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            try {
                // Decode the image with the specified options
                selectedImage = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), data.getData());

                // Save the image to a file and get the image path
                imagePath = saveImageToFile(selectedImage);

                // Set the image using Glide or other image loading libraries
                Glide.with(requireContext()).load(imagePath).into(binding.imageView);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToFile(Bitmap bitmap) {
        try {
            // Save the image to a file
            File imagesDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(imagesDir, "image_" + System.currentTimeMillis() + ".jpg");

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private void saveEditedNote() {
        String newTitle = binding.editTextTitle.getText().toString().trim();
        String newNoteContent = binding.editTextNote.getText().toString().trim();

        if (!newTitle.isEmpty() && !newNoteContent.isEmpty()) {

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
                        Log.d("EditFragment", "Note updated in the database");

                        // Notify the RecyclerView adapter about the change
                        if (noteAdapter != null) {
                            NoteModel updatedNoteModel = convertNoteEntityToNoteModel(existingNote);

                            // Update the existing note in the RecyclerView
                            noteAdapter.updateItem(updatedNoteModel);
                            showToast("Note updated successfully");
                            NavController navController = NavHostFragment.findNavController(EditFragment.this);
                            navController.popBackStack();
                        } else {
                            Log.e("EditFragment", "NoteAdapter is null");
                            showToast("Error: NoteAdapter is null");
                        }
                    } else {
                        Log.e("EditFragment", "Existing note is null");
                        showToast("Error: Note not found");
                    }
                });
            } else {
                Log.e("EditFragment", "Invalid noteId");
                showToast("Error: Invalid noteId");
            }
        } else {
            showToast("Error: Title and content cannot be empty");
        }
    }

    private NoteModel convertNoteEntityToNoteModel(NoteEntity noteEntity) {
        Bitmap imageBitmap = null;
        byte[] imageByteArray = noteEntity.getImage();

        if (imageByteArray != null) {
            imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
        }

        return new NoteModel(
                noteEntity.getTitle(),
                noteEntity.getDate(),
                noteEntity.getContent(),
                imageBitmap
        );
    }


    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
  }