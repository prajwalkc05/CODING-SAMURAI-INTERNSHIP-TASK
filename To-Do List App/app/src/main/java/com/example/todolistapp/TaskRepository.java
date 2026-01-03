package com.example.todolistapp;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private TaskDao taskDao;
    private LiveData<List<TaskEntity>> allTasks;
    private ExecutorService executorService;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getDatabase(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<TaskEntity>> getAllTasks() {
        return allTasks;
    }

    public void insert(TaskEntity task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    public void update(TaskEntity task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(TaskEntity task) {
        executorService.execute(() -> taskDao.delete(task));
    }
}
