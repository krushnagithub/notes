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
    private String selectedImagePath;
    private LiveData<List<NoteEntity>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }
    public String getSelectedImagePath() {
        return selectedImagePath;
    }

    public void setSelectedImagePath(String selectedImagePath) {
        this.selectedImagePath = selectedImagePath;
    }


    public void insert(NoteEntity note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.insert(note);
        });
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public void deleteNote(NoteEntity clickedNote) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.delete(clickedNote);
        });
    }

    public void update(NoteEntity note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            noteDao.update(note);
        });
    }

    public LiveData<NoteEntity> getNoteById(long noteId) {
        return noteDao.getNoteById(noteId);
    }

    public List<NoteModel> getNoteList() {
        return null;
    }
}

