package com.example.todolistapp;

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
        holder.tvTask.setText(task.taskName);
        
        // Remove listener to prevent triggering it during recycling
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted);
        
        // Handle CheckBox click (Update)
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.isCompleted = isChecked;
            listener.onUpdate(task);
        });

        // Handle Delete Button click (Delete)
        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(task);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTask;
        CheckBox checkBox;
        Button btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTask = itemView.findViewById(R.id.tvTask);
            checkBox = itemView.findViewById(R.id.checkBox);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
