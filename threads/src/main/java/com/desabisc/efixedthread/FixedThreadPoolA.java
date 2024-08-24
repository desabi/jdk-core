package com.desabisc.efixedthread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FixedThreadPoolA {
    /**
     * The Executors.newFixedThreadPool() method creates a thread pool with a fixed number of threads.
     * This is useful when you need to limit the number of concurrent tasks to a specific number,
     * while still allowing multiple tasks to be executed in parallel.
     */
    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {

            Runnable runnableTask1 = () -> log.info("Task 1 is running in {}", Thread.currentThread().getName());
            Runnable runnableTask2 = () -> log.info("Task 2 is running in {}", Thread.currentThread().getName());
            Runnable runnableTask3 = () -> log.info("Task 3 is running in {}", Thread.currentThread().getName());

            executorService.submit(runnableTask1);
            executorService.submit(runnableTask2);
            executorService.submit(runnableTask3);

            executorService.shutdown();
        }
        /*
        The output varies:
        Task 2 is running in pool-1-thread-2
        Task 1 is running in pool-1-thread-1
        Task 3 is running in pool-1-thread-2
         */
    }
}
