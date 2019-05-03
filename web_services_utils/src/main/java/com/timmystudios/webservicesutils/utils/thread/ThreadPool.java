package com.timmystudios.webservicesutils.utils.thread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ThreadPool implements WorkerThread.OnTaskFinishedListener {

    private final LinkedList<WorkerThread.Task> tasks = new LinkedList<>();
    private WorkerThread[] threads;

    public ThreadPool(int threadCount) {
        threads = new WorkerThread[threadCount];
        WorkerThread thread;
        for (int i = 0; i < threadCount; i++) {
            thread = new WorkerThread();
            thread.addListener(this);
            thread.start();
            threads[i] = thread;
        }
    }

    @Override
    public void onTaskFinished(WorkerThread workerThread,
                               WorkerThread.Task finishedTask) {
        synchronized (tasks) {
            if (tasks.size() > 0) {
                WorkerThread.Task task = tasks.remove();
                addTask(task);
            }
        }
    }

    public void addTask(WorkerThread.Task task) {
        synchronized (tasks) {
            boolean taskAdded = false;
            List<WorkerThread.Task> threadTasks;
            for (WorkerThread thread : threads) {
                threadTasks = thread.getTasks();
                if (threadTasks.size() == 0) {
                    thread.addTask(task);
                    taskAdded = true;
                    break;
                }
            }
            if (!taskAdded) {
                tasks.add(task);
            }
        }
    }

    public void removeTask(WorkerThread.Task task) {
        synchronized (tasks) {
            for (WorkerThread thread : threads) {
                thread.removeTask(task);
            }
            tasks.remove(task);
        }
    }

    public void removeAllTasks() {
        synchronized (tasks) {
            for (WorkerThread thread : threads) {
                thread.removeAllTasks();
            }
            tasks.clear();
        }
    }

    public List<WorkerThread.Task> getAllTasks() {
        synchronized (tasks) {
            List<WorkerThread.Task> allTasks = new ArrayList<>();
            for (WorkerThread thread : threads) {
                allTasks.addAll(thread.getTasks());
            }
            allTasks.addAll(tasks);
            return allTasks;
        }
    }
}
