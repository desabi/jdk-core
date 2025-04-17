package com.desabisc.dexecutorsinglethread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Executors.newSingleThreadExecutor() method creates an ExecutorService that uses a single worker thread
 * to execute tasks.
 * This is useful when you want to ensure that tasks are executed sequentially, one after the other,
 * in a single thread.
 */
@Slf4j
public class SingleThreadA {

    public static void main(String[] args) {
        // Creates an executor with a single thread.
        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {

            Runnable runnableTask1 = () -> log.info("Task 1 is running in {}", Thread.currentThread().getName());
            Runnable runnableTask2 = () -> log.info("Task 2 is running in {}", Thread.currentThread().getName());
            Runnable runnableTask3 = () -> log.info("Task 3 is running in {}", Thread.currentThread().getName());

            // Since only one thread is used, tasks are executed sequentially in the order they were submitted.
            executorService.submit(runnableTask1);
            executorService.submit(runnableTask2);
            executorService.submit(runnableTask3);

            executorService.shutdown();
        }
        /*
        output:
        Task 1 is running in pool-1-thread-1
        Task 2 is running in pool-1-thread-1
        Task 3 is running in pool-1-thread-1
         */
    }
}
