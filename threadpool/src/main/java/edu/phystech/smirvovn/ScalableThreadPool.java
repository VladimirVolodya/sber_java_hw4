package edu.phystech.smirvovn;

import java.lang.Thread.State;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ScalableThreadPool implements ThreadPool {

    ScalableThreadPool(int min, int max) {
        additionalMax = max - min;
        waiting = 0;
        taskQueue = new ArrayDeque<>();
        threads = new ArrayList<>();
        additionalThreads = new HashSet<>();
        for (int i = 0; i < min; i++) {
            threads.add(new Thread(new TaskExecutor()));
        }
    }

    @Override
    public void start() {
        for (var t : threads) {
            t.start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        clearUnusedThreads();
        boolean create = additionalThreads.size() < additionalMax;
        synchronized (taskQueue) {
            if (!(create &= (waiting == 0))) {
                taskQueue.add(runnable);
                taskQueue.notify();
            }
        }
        if (create) {
            additionalThreads.add(new Thread(new AdditionalTaskExecutor(runnable)));
        }
    }

    @Override
    public void shutdown() {
        for (int i = 0; i < threads.size() + additionalThreads.size(); i++) {
            synchronized (taskQueue) {
                taskQueue.add(new FinishTask());
            }
        }
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
        for (var t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    private void clearUnusedThreads() {
        additionalThreads.removeIf(t -> t.getState() == State.TERMINATED);
    }

    int additionalMax;
    int waiting;
    private final Queue<Runnable> taskQueue;
    private final List<Thread> threads;
    private final Set<Thread> additionalThreads;

    private class TaskExecutor implements Runnable {

        @Override
        public void run() {
            while (true) {
                Runnable task = null;
                synchronized (taskQueue) {
                    ++waiting;

                    while ((task = taskQueue.poll()) == null) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            System.err.println(e);
                            return;
                        }
                    }

                    --waiting;
                }
                if (task instanceof FinishTask) {
                    return;
                }
                task.run();
            }
        }
    }

    private class AdditionalTaskExecutor implements Runnable {

        public AdditionalTaskExecutor(Runnable task) {
            firstTask = task;
        }

        @Override
        public void run() {
            firstTask.run();
            while (true) {
                Runnable task = null;
                synchronized (taskQueue) {
                    task = taskQueue.poll();
                }
                if (task == null) {
                    return;
                }
                if (task instanceof FinishTask) {
                    return;
                }
                task.run();
            }
        }

        private Runnable firstTask;
    }
    
}
