package edu.phystech.smirvovn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {

    FixedThreadPool(int size) {
        taskQueue = new ArrayDeque<>();
        threads = new ArrayList<>();
        for (int i = 0; i < size; i++) {
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
        synchronized (taskQueue) {
            taskQueue.add(runnable);
            taskQueue.notify();
        }
    }

    @Override
    public void shutdown() {
        for (int i = 0; i < threads.size(); i++) {
            execute(new FinishTask());
        }
        for (var t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
    }

    private final Queue<Runnable> taskQueue;
    private final List<Thread> threads;

    private class TaskExecutor implements Runnable {

        @Override
        public void run() {
            while (true) {
                Runnable task = null;
                synchronized (taskQueue) {
                    while ((task = taskQueue.poll()) == null) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            System.err.println(e);
                            return;
                        }
                    }
                }
                if (task instanceof FinishTask) {
                    return;
                }
                task.run();
            }
        }
    }

}
