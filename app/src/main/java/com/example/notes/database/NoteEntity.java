package com.example.notes.database;

// NoteEntity.java

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "note_table")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String date;
    public String content;
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    public byte[] image;


    public String getContent() {
        return content;
    }

    public byte[] getImage() {
        return image;
    }


    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getNoteContent() {
        return 0;
    }


    public Object getImageByteArray() {
        return image;
    }

    public void setTitle(String newTitle) {

    }

    public void setContent(String newNoteContent) {

    }

    public void setImage(byte[] toByteArray) {

    }

    public long getId() {
        return id;
    }

    public NoteEntity getValue() {

        return null;
    }

    public void setDate(Date currentDate) {

    }


    public void setId(long id) {
        this.id = id;
    }

    public void setImagePath(String imagePath) {

    }
}
