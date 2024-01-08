package com.example.notes.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    // Existing methods...

    @Query("SELECT * FROM note_table WHERE id = :noteId")
    LiveData<NoteEntity> getNoteById(long noteId);



    @Query("SELECT * FROM note_table")
    LiveData<List<NoteEntity>> getAllNotes();

    @Insert
    long insert(NoteEntity noteEntity);
    @Delete
    void delete(NoteEntity note);
    @Update
    void update(NoteEntity note);



}
