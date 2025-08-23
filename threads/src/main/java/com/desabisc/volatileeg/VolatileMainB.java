package com.desabisc.volatileeg;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VolatileMainB {
    public static void main(String[] args) {
        VolatileExampleB volatileCounter = new VolatileExampleB();

        Runnable incrementerRunnable = () -> {
            for (int i = 0; i < 10; i++) {
                volatileCounter.increment();
            }
        };

        Thread threadA = new Thread(incrementerRunnable);
        Thread threadB = new Thread(incrementerRunnable);

        threadA.start();
        threadB.start();

        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            log.error("Thread was interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        log.info("Counter value: {}", volatileCounter.getCounter());
    }
}
