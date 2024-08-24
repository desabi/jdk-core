package com.desabisc.efixedthread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In cases where you have CPU-intensive tasks, it's often beneficial to use a fixed thread pool with a size
 * equal to the number of available CPU cores.
 * This example demonstrates processing a set of CPU-bound tasks.
 */
@Slf4j
public class CPUBoundTasks {

    /**
     * CPU Utilization: By matching the thread pool size to the number of CPU cores, you ensure that all cores
     * are utilized without overwhelming the CPU.
     * Task Execution: Each task performs a CPU-intensive operation (calculating square roots in a loop),
     * demonstrating how the fixed thread pool handles such tasks.
     */
    public static void main(String[] args) {
        // Get the number of available processors
        int numCores = Runtime.getRuntime().availableProcessors();
        log.info("numCores: {}", numCores);

        // Create a fixed thread pool with the same number of threads as CPU cores
        try (ExecutorService executor = Executors.newFixedThreadPool(numCores)) {

            // Submit CPU-bound tasks to the executor
            for (int i = 1; i <= numCores; i++) {
                int taskId = i;
                executor.submit(() -> performCPUIntensiveTask(taskId));
            }

            // Shut down the executor
            executor.shutdown();
        }
    }

    // Simulate a CPU-intensive task
    public static void performCPUIntensiveTask(int taskId) {
        log.info("Task {} is running in {}", taskId, Thread.currentThread().getName());
        // Simulate CPU work
        for (int i = 0; i < 1_000_000; i++) {
            Math.sqrt(i);
        }
        log.info("Task {} completed in {}", taskId,  Thread.currentThread().getName());
    }
}
