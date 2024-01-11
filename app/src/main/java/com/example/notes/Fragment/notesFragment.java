package com.example.notes.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.notes.R;
import com.example.notes.adapter.NoteAdapter;
import com.example.notes.database.NoteEntity;
import com.example.notes.databinding.FragmentNotesBinding;
import com.example.notes.viewmodel.NoteModel;
import com.example.notes.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class notesFragment extends Fragment {

    private FragmentNotesBinding binding;
    protected static NoteAdapter noteAdapter;
    static NoteViewModel noteViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        initView();
        // initData();
        setupListeners();
        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(notesFragment.this);
                navController.navigate(R.id.action_notesFragment_to_addFragment);
            }
        });

        binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }
        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }


    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Search Notes");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set gravity to center
        input.setGravity(Gravity.CENTER);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = input.getText().toString().trim();
                List<NoteModel> filteredList = noteAdapter.filter(query);

                 noteAdapter.updateData(filteredList); // Update the adapter's data with the filtered list

                if (filteredList.isEmpty()) {
                    // Show a toast message indicating no results were found
                    showToast("No results found for '" + query + "'");
                }
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Filter Options");

        // List of filter options
        String[] filterOptions = {"By Date", "By Content"};

        // Boolean array to track the selected options
        boolean[] checkedOptions = new boolean[filterOptions.length];

        builder.setMultiChoiceItems(filterOptions, checkedOptions, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // Handle the click on an option
                // You can store the state of selected options in the checkedOptions array
            }
        });

        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Apply the selected filters
                // You can use the checkedOptions array to determine the selected filters
                // Perform filtering based on selected options
                applyFilters(checkedOptions);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void applyFilters(boolean[] checkedOptions) {
        // Pass the original list of notes to filterNotes
        List<NoteModel> originalList = noteViewModel.getNoteList();

        if (originalList != null) {
            List<NoteModel> filteredList = filterNotes(checkedOptions, originalList);
            noteAdapter.updateData(filteredList);
        } else {
            // Handle the case where the original list is null
            showToast("No notes available to filter");
        }
    }

      private List<NoteModel> filterNotes(boolean[] checkedOptions, List<NoteModel> originalList) {
          List<NoteModel> filteredList = new ArrayList<>();

          if (originalList != null) {
              filteredList.addAll(originalList);

              // Example: Filtering by Date
              if (checkedOptions[0]) {
                  // Sort by date
                  Collections.sort(filteredList, new Comparator<NoteModel>() {
                      @Override
                      public int compare(NoteModel note1, NoteModel note2) {
                          // Compare notes based on date
                          return note1.getDate().compareTo(note2.getDate());
                      }
                  });
              }

              // Example: Filtering by Content
              if (checkedOptions[1]) {
                  // Sort by content
                  Collections.sort(filteredList, new Comparator<NoteModel>() {
                      @Override
                      public int compare(NoteModel note1, NoteModel note2) {
                          // Compare notes based on content
                          return note1.getContent().compareToIgnoreCase(note2.getContent());
                      }
                  });
              }

              // Add more conditions for additional sorting options
          }

          return filteredList;
      }

    private void initView() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteAdapter = new NoteAdapter(new ArrayList<>(), new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(int position) {
                NoteEntity clickedNote = noteAdapter.getNotes().get(position);

                Bundle bundle = new Bundle();
                bundle.putString("title", clickedNote.getTitle());
                bundle.putString("date", clickedNote.getDate());
                bundle.putString("content", clickedNote.getContent());
                byte[] imageByteArray = clickedNote.getImage();
                bundle.putByteArray("imageBitmap", imageByteArray);

                NavController navController = NavHostFragment.findNavController(notesFragment.this);
                navController.navigate(R.id.action_notesFragment_to_detailFragment, bundle);
            }

            @Override
            public void onDeleteClick(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Delete Note");
                builder.setMessage("Are you sure you want to delete this note?");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NoteEntity clickedNote = noteAdapter.getNotes().get(position);

                        noteViewModel.deleteNote(clickedNote);

                        // Notify the adapter that an item has been removed
                        noteAdapter.notifyItemRemoved(position);
                        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show();

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }


            @Override
            public void onEditClick(int position) {
                NoteEntity clickedNote = noteAdapter.getNotes().get(position);
                long clickedNoteId = clickedNote.getId(); // Assuming your NoteEntity has a method getId()

                // Navigate to the EditFragment and pass the noteId
                Bundle bundle = new Bundle();
                bundle.putLong("noteId", clickedNoteId);

                bundle.putString("title", clickedNote.getTitle());
                bundle.putString("date", clickedNote.getDate());
                bundle.putString("content", clickedNote.getContent());
                byte[] imageByteArray = clickedNote.getImage();
                bundle.putByteArray("imageBitmap", imageByteArray);

                NavController navController = NavHostFragment.findNavController(notesFragment.this);
                navController.navigate(R.id.action_notesFragment_to_editFragment, bundle);
            }

        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(noteAdapter);

        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), new Observer<List<NoteEntity>>() {
            @Override
            public void onChanged(List<NoteEntity> noteEntities) {
                Log.d("NotesFragment", "onChanged: Data changed. Size: " + noteEntities.size());

                noteAdapter.updateData(convertNoteEntitiesToNoteModels(noteEntities));
                noteAdapter.setNotes(noteEntities);
                noteAdapter.notifyDataSetChanged();
            }
        });
    }


    private List<NoteModel> convertNoteEntitiesToNoteModels(List<NoteEntity> noteEntities) {
        List<NoteModel> noteModels = new ArrayList<>();
        for (NoteEntity noteEntity : noteEntities) {
            Bitmap imageBitmap = null;
            byte[] imageByteArray = noteEntity.getImage();

            if (imageByteArray != null) {
                imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            }

            NoteModel noteModel = new NoteModel(
                    noteEntity.getTitle(),
                    noteEntity.getDate(),
                    noteEntity.getContent(),
                    imageBitmap
            );
            noteModels.add(noteModel);
        }
        return noteModels;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), new Observer<List<NoteEntity>>() {
            @Override
            public void onChanged(List<NoteEntity> noteEntities) {
                noteAdapter.updateData(convertNoteEntitiesToNoteModels(noteEntities));
            }
        });
    }

}