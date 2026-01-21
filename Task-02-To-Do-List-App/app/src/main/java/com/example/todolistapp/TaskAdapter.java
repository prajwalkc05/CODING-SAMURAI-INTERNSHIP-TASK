package com.example.todolistapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskEntity> taskList;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onUpdate(TaskEntity task);
        void onDelete(TaskEntity task);
        void onEdit(TaskEntity task);
    }

    public TaskAdapter(List<TaskEntity> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<TaskEntity> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskEntity task = taskList.get(position);
        
        // Set Task Name
        holder.tvTask.setText(task.taskName);
        
        // Remove listener temporarily to avoid triggering it while setting state
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted);

        // Update UI based on completion status
        updateTaskUI(holder, task.isCompleted);
        
        // Handle CheckBox click (Update Status)
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isCompleted = isChecked;
            updateTaskUI(holder, isChecked);
            listener.onUpdate(task);
        });

        // Handle Delete Button click
        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(task);
        });

        // Handle Edit button click
        holder.btnEdit.setOnClickListener(v -> {
            listener.onEdit(task);
        });
    }

    /**
     * Updates the UI elements (Status text, colors, and strikethrough) 
     * based on whether the task is completed or pending.
     */
    private void updateTaskUI(ViewHolder holder, boolean isCompleted) {
        if (isCompleted) {
            // COMPLETED Status
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            
            // Remove strikethrough and set text color to gray
            holder.tvTask.setPaintFlags(holder.tvTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTask.setTextColor(Color.GRAY);
        } else {
            // PENDING Status
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
            
            // Remove Strikethrough from Task Name
            holder.tvTask.setPaintFlags(holder.tvTask.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTask.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTask, tvStatus;
        CheckBox checkBox;
        Button btnDelete, btnEdit;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTask = itemView.findViewById(R.id.tvTask);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
