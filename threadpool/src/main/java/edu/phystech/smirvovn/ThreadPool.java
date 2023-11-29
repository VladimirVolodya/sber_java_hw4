package edu.phystech.smirvovn;

public interface ThreadPool {
    void start();

    void execute(Runnable runnable);

    void shutdown();
}
