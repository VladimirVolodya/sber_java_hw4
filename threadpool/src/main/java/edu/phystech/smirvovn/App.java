package edu.phystech.smirvovn;

import java.time.LocalDateTime;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        var threadPool = new FixedThreadPool(3);
        for (int i = 0; i < 20; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("sleeping " + LocalDateTime.now());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            });
        }
        threadPool.start();
        for (int i = 0; i < 80; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("sleeping " + LocalDateTime.now());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            });
        }
        threadPool.shutdown();
    }
}
