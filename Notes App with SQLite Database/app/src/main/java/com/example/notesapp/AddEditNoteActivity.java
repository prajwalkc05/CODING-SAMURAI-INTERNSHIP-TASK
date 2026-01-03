package com.example.notesapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditNoteActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "com.example.notesapp.EXTRA_ID";
    public static final String EXTRA_TITLE = "com.example.notesapp.EXTRA_TITLE";
    public static final String EXTRA_CONTENT = "com.example.notesapp.EXTRA_CONTENT";
    public static final String EXTRA_PINNED = "com.example.notesapp.EXTRA_PINNED";

    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSave;
    private Button buttonDelete;
    private DatabaseHelper dbHelper;
    private int noteId = -1;
    private boolean isPinned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        Toolbar toolbar = findViewById(R.id.add_edit_toolbar);
        setSupportActionBar(toolbar);
        
        editTextTitle = findViewById(R.id.edit_text_title);
        editTextContent = findViewById(R.id.edit_text_content);
        buttonSave = findViewById(R.id.button_save);
        buttonDelete = findViewById(R.id.button_delete);
        
        dbHelper = new DatabaseHelper(this);
        
        // Setup Toolbar
        if (getSupportActionBar() != null) {
            // We rely on the XML navigationIcon and navigationIconTint for the correct icon color
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Handle Home Button Click to Close Activity
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_ID)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Note");
            }
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextContent.setText(intent.getStringExtra(EXTRA_CONTENT));
            noteId = intent.getIntExtra(EXTRA_ID, -1);
            isPinned = intent.getBooleanExtra(EXTRA_PINNED, false);
            
            buttonDelete.setVisibility(View.VISIBLE);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog();
                }
            });
        } else {
             if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Note");
            }
             buttonDelete.setVisibility(View.GONE);
        }
        
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();
        
        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            Toast.makeText(this, "Please insert a title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Note note = new Note(title, content, timestamp, isPinned);
        
        if (noteId != -1) {
            note.setId(noteId);
            dbHelper.updateNote(note);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addNote(note);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        }
        
        finish();
    }
    
    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteNote(noteId);
                        Toast.makeText(AddEditNoteActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
