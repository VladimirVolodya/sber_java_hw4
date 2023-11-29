package edu.phystech.smirvovn;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

public class AppTest {
    
    @Test
    public void FixedThreadPoolTest(){
        var taskDuration = 5;
        var start = System.currentTimeMillis();
        var threadPool = new FixedThreadPool(10);
        for (int i = 0; i < 5; i++) {
            submitTask(threadPool, taskDuration);
        }
        threadPool.start();
        for (int i = 0; i < 10; i++) {
            submitTask(threadPool, taskDuration);
        }
        threadPool.shutdown();
        var finish = System.currentTimeMillis();
        var elapsed = (finish - start) / 1000;
        assertTrue(elapsed > 2 * taskDuration - 1);
        assertTrue(elapsed < 2 * taskDuration + 1);
    }

    @Test
    public void ScalableThreadPoolUpperBoundTest() {
        var taskDuration = 5;
        var start = System.currentTimeMillis();
        var threadPool = new ScalableThreadPool(10, 20);
        for (int i = 0; i < 21; i++) {
            submitTask(threadPool, taskDuration);
        }
        threadPool.start();
        threadPool.shutdown();
        var finish = System.currentTimeMillis();
        var elapsed = (finish - start) / 1000;
        assertTrue(elapsed > 2 * taskDuration - 1);
        assertTrue(elapsed < 2 * taskDuration + 1);
    }

    @Test
    public void ScalableThreadPoolScalingTest() {
        var taskDuration = 5;
        var start = System.currentTimeMillis();
        var threadPool = new ScalableThreadPool(10, 20);
        for (int i = 0; i < 5; i++) {
            submitTask(threadPool, taskDuration);
        }
        threadPool.start();
        for (int i = 0; i < 10; i++) {
            submitTask(threadPool, taskDuration);
        }
        threadPool.shutdown();
        var finish = System.currentTimeMillis();
        assertTrue((finish - start) / 1000 < taskDuration + 1);
    }

    void submitTask(ThreadPool threadPool, int taskDuration) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(taskDuration * 1000);
                } catch (InterruptedException e) {}
            }
        });
    }
}
