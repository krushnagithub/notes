package com.example.notes.viewmodel;

import android.graphics.Bitmap;

public class NoteModel {

    private String title;
    private String date;
    private String noteContent;
    private Bitmap image;

    public NoteModel(String title, String date, String noteContent, Bitmap image) {
        this.title = title;
        this.date = date;
        this.noteContent = noteContent;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getContent() {
        return null;
    }
}
