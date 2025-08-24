package com.desabisc.synchronizedeg;

import lombok.extern.slf4j.Slf4j;

/**
 * Main class to demonstrate static synchronized methods.
 * Shows how static synchronized methods synchronize at the class level.
 */
@Slf4j
public class StaticSynchronizedMain {
    
    public static void main(String[] args) {
        log.info("=== Demonstrating Static Synchronized Methods ===\n");
        
        // Test 1: Multiple threads calling static synchronized methods
        log.info("Test 1: Static synchronized methods (Class-level synchronization)");
        testStaticSynchronizedMethods();
        
        // Test 2: Mix of synchronized and non-synchronized static methods
        log.info("\nTest 2: Mix of synchronized and non-synchronized static methods");
        testMixedStaticMethods();
        
        log.info("\n=== Static Synchronized Methods Demonstration Complete ===");
    }
    
    private static void testStaticSynchronizedMethods() {
        // Reset counter
        StaticSynchronizedExample.resetStaticCounter();
        
        // Create threads that call static synchronized methods
        Thread[] incrementThreads = new Thread[3];
        Thread[] decrementThreads = new Thread[2];
        
        // Create increment threads
        for (int i = 0; i < 3; i++) {
            incrementThreads[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    StaticSynchronizedExample.incrementStaticCounter();
                }
            }, "IncrementThread-" + i);
        }
        
        // Create decrement threads
        for (int i = 0; i < 2; i++) {
            decrementThreads[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    StaticSynchronizedExample.decrementStaticCounter();
                }
            }, "DecrementThread-" + i);
        }
        
        // Start all threads
        for (Thread thread : incrementThreads) {
            thread.start();
        }
        for (Thread thread : decrementThreads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : incrementThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (Thread thread : decrementThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        int finalValue = StaticSynchronizedExample.getStaticCounter();
        log.info("Final static counter value: {}", finalValue);
        log.info("Expected value: 9 (15 increments - 6 decrements), Actual value: {}", finalValue);
    }
    
    private static void testMixedStaticMethods() {
        // Reset counter
        StaticSynchronizedExample.resetStaticCounter();
        
        // Create threads that call both synchronized and non-synchronized methods
        Thread[] threads = new Thread[4];
        
        for (int i = 0; i < 4; i++) {
            threads[i] = new Thread(() -> {
                // Call synchronized method
                StaticSynchronizedExample.incrementStaticCounter();
                
                // Call non-synchronized method
                StaticSynchronizedExample.nonSynchronizedStaticMethod();
                
                // Call synchronized method again
                StaticSynchronizedExample.incrementStaticCounter();
            }, "MixedThread-" + i);
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        int finalValue = StaticSynchronizedExample.getStaticCounter();
        log.info("Final static counter value: {}", finalValue);
        log.info("Expected value: 8 (4 threads × 2 increments), Actual value: {}", finalValue);
    }

}