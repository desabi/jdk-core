package com.desabisc.synchronizedeg;

import lombok.extern.slf4j.Slf4j;

/**
 * Example demonstrating the use of synchronized keyword with instance methods.
 * This class shows synchronized methods when accessed by multiple threads on the same instance.
 */
@Slf4j
public class SynchronizedExample {
    
    private int counter = 0;
    
    /**
     * SYNCHRONIZED method - only one thread can access this method at a time
     * This prevents race conditions and ensures thread safety
     */
    public synchronized void incrementSynchronized() {
        int currentValue = counter;
        // Simulate some processing time
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        counter = currentValue + 1;
        log.info("Thread: {} - Sync increment: {} -> {}", 
                Thread.currentThread().getName(), currentValue, counter);
    }
    
    /**
     * Another synchronized method - demonstrates that synchronized methods
     * on the same object are mutually exclusive
     */
    public synchronized void decrementSynchronized() {
        int currentValue = counter;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        counter = currentValue - 1;
        log.info("Thread: {} - Sync decrement: {} -> {}", 
                Thread.currentThread().getName(), currentValue, counter);
    }
    
    public int getCounter() {
        return counter;
    }

}