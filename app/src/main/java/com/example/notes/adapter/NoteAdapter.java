package com.example.notes.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.notes.R;
import com.example.notes.database.NoteEntity;
import com.example.notes.viewmodel.NoteModel;

import java.util.ArrayList;
import java.util.List;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final OnNoteClickListener onNoteClickListener;
    private List<NoteModel> noteList;
    private List<NoteEntity> notes;





    public void setNotes(List<NoteEntity> notes) {
        this.notes = notes;
    }

    public List<NoteEntity> getNotes() {
        return notes;
    }

    public void notifyItemRangeChanged() {

    }

    public interface OnNoteClickListener {
        void onNoteClick(int position);

        void onDeleteClick(int position);

        void onEditClick(int position);
    }

    public NoteAdapter(List<NoteModel> noteList, OnNoteClickListener onNoteClickListener) {
        this.noteList = noteList;
        this.onNoteClickListener = onNoteClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notesview, parent, false);
        return new NoteViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel noteModel = noteList.get(position);

        holder.titleTextView.setText(noteModel.getTitle());
        holder.dateTextView.setText(noteModel.getDate());
        holder.contentTextView.setText(noteModel.getNoteContent());

        // Set the image if available
        if (noteModel.getImage() != null) {
            holder.imageView.setImageBitmap(noteModel.getImage());
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            // If no image is available, you can hide the ImageView
            holder.imageView.setVisibility(View.GONE);
        }


        int numbering = position + 1;
        holder.numberingText.setText(String.valueOf(numbering));

        int colorResId = (numbering % 2 == 0) ? R.color.evenColor : R.color.oddColor;
        holder.numberingText.setBackgroundResource(colorResId);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNoteClickListener.onNoteClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showOptionsDialog(holder, position);
                return true;
            }
        });
    }


    private void showOptionsDialog(NoteViewHolder holder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Options");

        CharSequence[] options = new CharSequence[]{"Edit", "Delete"};

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        onNoteClickListener.onEditClick(position);
                        break;
                    case 1:
                        onNoteClickListener.onDeleteClick(position);
                        break;
                }
            }
        });

        builder.show();
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0; // Null check added here
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        TextView contentTextView;
        ImageView imageView;
        TextView numberingText;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            dateTextView = itemView.findViewById(R.id.date);
            contentTextView = itemView.findViewById(R.id.contantnotes);
            imageView = itemView.findViewById(R.id.smallImageView);
            numberingText = itemView.findViewById(R.id.numberingText);
        }
    }

    // Method to update data based on the search query
    public List<NoteModel> filter(String query) {
        List<NoteModel> filteredList = new ArrayList<>();

        if (noteList == null) {
            return filteredList;
        }

        if (query.isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            query = query.toLowerCase();
            for (NoteModel note : noteList) {
                if (note.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(note);
                }
            }
        }
        notifyDataSetChanged();
        return filteredList;
    }
    public void deleteItem(int position) {
        if (position != RecyclerView.NO_POSITION && onNoteClickListener != null) {
            onNoteClickListener.onDeleteClick(position);
        }
    }
    public void updateData(List<NoteModel> newData) {
        noteList.clear();
        noteList.addAll(newData);
        notifyDataSetChanged();
        Log.d("NoteAdapter", "updateData: Data updated. New size: " + noteList.size());

    }
}