package com.desabisc.synchronizedeg;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main class to demonstrate instance synchronized methods.
 * Shows synchronized methods when accessed by multiple threads on the same instance.
 */
@Slf4j
public class SynchronizedMain {
    
    public static void main(String[] args) {
        SynchronizedExample example = new SynchronizedExample();
        
        // Test: Instance synchronized methods (thread-safe)
        log.info("Test: Instance synchronized methods (Thread-safe)");
        testSynchronizedMethodsWithExecutor(example);

    }
    
    // Original method commented out
    /*
    private static void testSynchronizedMethods(SynchronizedExample example) {
        // Create multiple threads that call synchronized method
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    example.incrementSynchronized();
                }
            }, "SyncThread-" + i);
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
        
        log.info("Final counter value (synchronized): {}", example.getCounter());
        log.info("Expected value: 50, Actual value: {}", example.getCounter());
        log.info("Race condition occurred: {}", example.getCounter() != 50 ? "YES" : "NO");
    }
    */
    
    /**
     * New method using ExecutorService for better thread management
     * Benefits: Thread reuse, better resource management, cleaner code
     */
    private static void testSynchronizedMethodsWithExecutor(SynchronizedExample example) {
        // Create a thread pool with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        log.info("Starting 5 threads using ExecutorService...");
        
        // Submit tasks to the executor
        for (int i = 0; i < 5; i++) {
            final int threadId = i + 1;
            executor.submit(() -> {
                log.info("Thread {} starting execution", threadId);
                for (int j = 0; j < 10; j++) {
                    example.incrementSynchronized();
                }
                log.info("Thread {} completed execution", threadId);
            });
        }
        
        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        
        try {
            // Wait for all tasks to complete with timeout
            if (executor.awaitTermination(1, TimeUnit.MINUTES)) {
                log.info("All threads completed successfully");
            } else {
                log.warn("Some threads did not complete within timeout");
            }
        } catch (InterruptedException e) {
            log.error("Thread was interrupted while waiting for completion: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        
        // Final results
        log.info("Final counter value (synchronized): {}", example.getCounter());
        log.info("Expected value: 50, Actual value: {}", example.getCounter());
        log.info("Race condition occurred: {}", example.getCounter() != 50 ? "YES" : "NO");
    }
}