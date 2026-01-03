package com.example.notesapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

    private List<Note> noteList; // Master list containing all notes
    private List<Note> filteredList; // List currently displayed
    private Context context;
    private DatabaseHelper dbHelper;
    private String currentQuery = "";

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.noteList = new ArrayList<>(notes);
        this.filteredList = new ArrayList<>(notes);
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note note = filteredList.get(position);
        holder.textViewTitle.setText(note.getTitle());
        holder.textViewContent.setText(note.getContent());
        holder.textViewTimestamp.setText(note.getTimestamp());
        
        holder.imageViewPin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        // Click: Edit Note
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditNoteActivity.class);
            intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
            intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
            intent.putExtra(AddEditNoteActivity.EXTRA_CONTENT, note.getContent());
            intent.putExtra(AddEditNoteActivity.EXTRA_PINNED, note.isPinned());
            context.startActivity(intent);
        });

        // Long Click: Options (Pin/Delete)
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(note);
            return true;
        });
    }

    private void showOptionsDialog(Note note) {
        String pinOption = note.isPinned() ? "Unpin Note" : "Pin Note";
        String[] options = {pinOption, "Delete Note"};

        new AlertDialog.Builder(context)
            .setTitle("Choose Action")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    togglePin(note);
                } else if (which == 1) {
                    showDeleteDialog(note);
                }
            })
            .show();
    }

    private void togglePin(Note note) {
        note.setPinned(!note.isPinned());
        dbHelper.updateNote(note);
        Toast.makeText(context, note.isPinned() ? "Note Pinned" : "Note Unpinned", Toast.LENGTH_SHORT).show();
        // Refresh data to update sorting
        refreshData();
    }

    private void showDeleteDialog(Note note) {
        new AlertDialog.Builder(context)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete", (dialog, which) -> {
                dbHelper.deleteNote(note);
                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                
                // Remove from local lists to avoid full refresh flicker, or just refresh
                // For robustness with search, let's refresh from DB but keep query
                refreshData();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    // Refresh data from DB and re-apply filter
    public void refreshData() {
        List<Note> allNotes = dbHelper.getAllNotes();
        setNotes(allNotes);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }
    
    public void setNotes(List<Note> notes) {
        this.noteList = new ArrayList<>(notes);
        // Re-apply current filter
        filter(currentQuery);
    }

    public void filter(String text) {
        currentQuery = text;
        filteredList.clear();
        
        if (text == null || text.isEmpty()) {
            filteredList.addAll(noteList);
        } else {
            String lowerCaseText = text.toLowerCase();
            for (Note note : noteList) {
                if (note.getTitle().toLowerCase().contains(lowerCaseText) ||
                    note.getContent().toLowerCase().contains(lowerCaseText)) {
                    filteredList.add(note);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewContent;
        TextView textViewTimestamp;
        ImageView imageViewPin;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewContent = itemView.findViewById(R.id.text_view_content);
            textViewTimestamp = itemView.findViewById(R.id.text_view_timestamp);
            imageViewPin = itemView.findViewById(R.id.image_view_pin);
        }
    }
}
