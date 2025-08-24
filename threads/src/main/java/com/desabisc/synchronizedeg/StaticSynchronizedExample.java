package com.desabisc.synchronizedeg;

import lombok.extern.slf4j.Slf4j;

/**
 * Example demonstrating the use of static synchronized methods.
 * Static synchronized methods synchronize on the class level, not the instance level.
 * This means all threads calling static synchronized methods on this class
 * will be synchronized, regardless of which instance they're using.
 */
@Slf4j
public class StaticSynchronizedExample {
    
    private static int staticCounter = 0;
    
    /**
     * Static synchronized method - synchronized on the class level
     * Only one thread can execute ANY static synchronized method of this class at a time
     */
    public static synchronized void incrementStaticCounter() {
        int currentValue = staticCounter;
        log.info("Thread: {} - Starting static increment: {}", 
                Thread.currentThread().getName(), currentValue);
        
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        staticCounter = currentValue + 1;
        log.info("Thread: {} - Completed static increment: {} -> {}", 
                Thread.currentThread().getName(), currentValue, staticCounter);
    }
    
    /**
     * Another static synchronized method - demonstrates mutual exclusion
     * between all static synchronized methods on the same class
     */
    public static synchronized void decrementStaticCounter() {
        int currentValue = staticCounter;
        log.info("Thread: {} - Starting static decrement: {}", 
                Thread.currentThread().getName(), currentValue);
        
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        staticCounter = currentValue - 1;
        log.info("Thread: {} - Completed static decrement: {} -> {}", 
                Thread.currentThread().getName(), currentValue, staticCounter);
    }
    
    /**
     * Static synchronized method that just reads the counter
     * This will also be synchronized with other static synchronized methods
     */
    public static synchronized int getStaticCounter() {
        log.info("Thread: {} - Reading static counter: {}", 
                Thread.currentThread().getName(), staticCounter);
        return staticCounter;
    }
    
    /**
     * Reset the static counter
     */
    public static synchronized void resetStaticCounter() {
        log.info("Thread: {} - Resetting static counter from {} to 0", 
                Thread.currentThread().getName(), staticCounter);
        staticCounter = 0;
    }
    
    /**
     * Non-synchronized static method - can be called simultaneously
     * This demonstrates the difference between synchronized and non-synchronized
     */
    public static void nonSynchronizedStaticMethod() {
        log.info("Thread: {} - Non-synchronized static method called", 
                Thread.currentThread().getName());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Thread: {} - Non-synchronized static method completed", 
                Thread.currentThread().getName());
    }
}
