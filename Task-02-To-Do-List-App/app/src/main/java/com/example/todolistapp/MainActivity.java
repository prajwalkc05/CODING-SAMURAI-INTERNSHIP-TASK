package com.example.todolistapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {

    private TaskAdapter taskAdapter;
    private TextInputEditText etTask;
    private Button btnAdd;
    private RecyclerView recyclerView;
    
    private TaskViewModel taskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        etTask = findViewById(R.id.etTask);
        btnAdd = findViewById(R.id.btnAdd);
        recyclerView = findViewById(R.id.recyclerView);

        // Initialize Adapter with empty list first
        taskAdapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);
        
        // Add default animation to RecyclerView
        recyclerView.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());

        // Initialize ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Observe LiveData
        taskViewModel.getAllTasks().observe(this, tasks -> {
            // Update the cached copy of the tasks in the adapter.
            taskAdapter.updateList(tasks);
        });

        // Handle Add Button Click
        btnAdd.setOnClickListener(v -> {
            String taskName = etTask.getText().toString().trim();
            if (!taskName.isEmpty()) {
                TaskEntity newTask = new TaskEntity(taskName, false);
                taskViewModel.insert(newTask);
                etTask.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Please enter a task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUpdate(TaskEntity task) {
        taskViewModel.update(task);
    }

    @Override
    public void onDelete(TaskEntity task) {
        taskViewModel.delete(task);
    }

    @Override
    public void onEdit(TaskEntity task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);
        builder.setView(dialogView);

        final EditText etEditTask = dialogView.findViewById(R.id.etEditTask);
        etEditTask.setText(task.taskName);

        builder.setTitle("Edit Task")
                .setPositiveButton("Update", (dialog, which) -> {
                    String updatedTaskName = etEditTask.getText().toString().trim();
                    if (!updatedTaskName.isEmpty()) {
                        task.taskName = updatedTaskName;
                        taskViewModel.update(task);
                    } else {
                        Toast.makeText(MainActivity.this, "Task name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
