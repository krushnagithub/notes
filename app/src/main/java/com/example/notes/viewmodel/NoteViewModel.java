package com.example.notes.viewmodel;

// NoteViewModel.java
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.notes.database.AppDatabase;
import com.example.notes.database.NoteDao;
import com.example.notes.database.NoteEntity;

import java.util.Date;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private NoteDao noteDao;
    private LiveData<List<NoteEntity>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public void insert(NoteEntity note) {
        // Run the insert operation in a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insert(note);
        });
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public void deleteNote(NoteEntity clickedNote) {
        // Run the delete operation in a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.delete(clickedNote);
        });
    }

    public void update(NoteEntity note) {
        // Run the update operation in a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.update(note);
        });
    }

    public LiveData<NoteEntity> getNoteById(long noteId) {
        // Use the noteDao to retrieve the note by ID
        return noteDao.getNoteById(noteId);
    }

    public List<NoteModel> getNoteList() {
        return null;
    }
}
