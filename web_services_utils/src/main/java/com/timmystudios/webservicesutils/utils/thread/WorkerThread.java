package com.timmystudios.webservicesutils.utils.thread;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class WorkerThread extends Thread {

    private Handler handler = new Handler();
    private boolean enabled = true;
    private Task currentTask;
    private final ArrayList<Task> tasks = new ArrayList<>();
    private final ArrayList<OnTaskFinishedListener> listeners = new ArrayList<>();

    public void addTask(Task task) {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }

    public void removeTask(Task task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
    }

    public void removeAllTasks() {
        synchronized (tasks) {
            tasks.clear();
        }
    }

    public void addListener(OnTaskFinishedListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnTaskFinishedListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void removeAllListeners() {
        synchronized (listeners) {
            listeners.clear();
        }
    }

    public List<Task> getTasks() {
        synchronized (tasks) {
            return tasks;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void run() {
        super.run();
        while (enabled) {
            try {
                synchronized (tasks) {
                    if (tasks.size() > 0) {
                        currentTask = tasks.get(0);
                    }
                }
                if (currentTask != null) {
                    currentTask.execute();
                }
                synchronized (tasks) {
                    if (currentTask != null) {
                        tasks.remove(currentTask);
                        handler.post(new OnFinishedRunnable(currentTask));
                        currentTask = null;
                    }
                    if (tasks.size() == 0) {
                        tasks.wait();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class OnFinishedRunnable implements Runnable {
        private Task task;

        public OnFinishedRunnable(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            task.onFinished();
            synchronized (listeners) {
                for (OnTaskFinishedListener listener : listeners) {
                    listener.onTaskFinished(WorkerThread.this, task);
                }
            }
        }
    }

    public interface Task {
        void execute();
        void onFinished();
    }

    public interface OnTaskFinishedListener {
        void onTaskFinished(WorkerThread workerThread, Task finishedTask);
    }
}
