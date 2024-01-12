package com.example.notes.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.notes.database.AppDatabase;
import com.example.notes.database.NoteDao;
import com.example.notes.database.NoteEntity;
import java.util.List;

public class NoteRepository {
    private NoteDao noteDao;
    private LiveData<List<NoteEntity>> allNotes;

    public NoteRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public void deleteNote(NoteEntity note) {
        new DeleteNoteAsyncTask(noteDao).execute(note);
    }


    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public void insert(NoteEntity noteEntity) {
        new InsertAsyncTask(noteDao).execute(noteEntity);
    }

    private static class InsertAsyncTask extends AsyncTask<NoteEntity, Void, Void> {
        private NoteDao noteDao;

        private InsertAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(NoteEntity... noteEntities) {
            noteDao.insert(noteEntities[0]);
            return null;
        }
    }
}
     class DeleteNoteAsyncTask extends AsyncTask<NoteEntity, Void, Void> {
         private NoteDao noteDao;

         DeleteNoteAsyncTask(NoteDao noteDao) {
             this.noteDao = noteDao;
         }

         @Override
         protected Void doInBackground(NoteEntity... notes) {
             noteDao.delete(notes[0]);
             return null;
         }

         public void update(NoteEntity noteEntity) {
             new UpdateAsyncTask(noteDao).execute(noteEntity);
         }

         private static class UpdateAsyncTask extends AsyncTask<NoteEntity, Void, Void> {
             private NoteDao noteDao;

             private UpdateAsyncTask(NoteDao noteDao) {
                 this.noteDao = noteDao;
             }

             @Override
             protected Void doInBackground(NoteEntity... noteEntities) {
                 noteDao.update(noteEntities[0]);
                 return null;
             }

             @Override
             protected void onPostExecute(Void aVoid) {
                 // After updating the note, notify the LiveData about the change
                 // This ensures that the LiveData triggers an update for the observers
                 // on the main (UI) thread.
                 super.onPostExecute(aVoid);
             }
         }
     }