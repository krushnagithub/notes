package com.example.notes.Fragment;

import static com.example.notes.Fragment.notesFragment.noteAdapter;
import static com.example.notes.Fragment.notesFragment.noteViewModel;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.notes.R;
import com.example.notes.adapter.NoteAdapter;
import com.example.notes.database.NoteEntity;
import com.example.notes.databinding.FragmentAddBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class addFragment extends Fragment {
    private ProgressBar progressBar;

    private FragmentAddBinding binding;
    private Bitmap selectedImage;
    private static final int REQUEST_IMAGE_PICK = 101;
    private Calendar selectedDate = Calendar.getInstance();
    private NoteAdapter noteAdapter;  // Initialize the NoteAdapter here


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBinding.bind(inflater.inflate(R.layout.fragment_add, container, false));
        initView();
        setupListeners();
        return binding.getRoot();
    }

    private void initView() {
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
                saveNote();
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


    private void saveNote() {
        String title = binding.editTextTitle.getText().toString().trim();
        String noteContent = binding.editTextNote.getText().toString().trim();

        if (!title.isEmpty() && !noteContent.isEmpty()) {
            String formattedDate = dateFormat.format(selectedDate.getTime());

            if (selectedImage != null) {
                // Create and show the ProgressDialog
                ProgressDialog progressDialog = new ProgressDialog(requireContext());
                progressDialog.setMessage("Saving note...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Create a new thread to perform the database operations
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Simulate some background work
                            Thread.sleep(2000);

                            // Perform the actual note-saving operation
                            saveNoteToDatabase(title, formattedDate, noteContent);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            // Dismiss the ProgressDialog on the UI thread
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }
                }).start();
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNoteToDatabase(String title, String formattedDate, String noteContent) {
        NoteEntity newNoteEntity = new NoteEntity();
        newNoteEntity.title = title;
        newNoteEntity.date = formattedDate;
        newNoteEntity.content = noteContent;

        if (selectedImage != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            newNoteEntity.image = stream.toByteArray();
        }

        // Save the new note using the NoteViewModel
        noteViewModel.insert(newNoteEntity);

        // Log the data inserted
        Log.d("RoomDatabase", "Inserted new note: " +
                "Title: " + newNoteEntity.title +
                ", Date: " + newNoteEntity.date +
                ", Content: " + newNoteEntity.content +
                ", Image size: " + (newNoteEntity.image != null ? newNoteEntity.image.length : 0) + " bytes");

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NavController navController = NavHostFragment.findNavController(addFragment.this);
                navController.popBackStack();
            }
        });
    }
}