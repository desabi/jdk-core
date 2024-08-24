package com.desabisc.bexecutorserv;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ExecuteExample {
    /**
     * The execute() method takes a Runnable as a parameter and submits it for execution. It doesn't return any result.
     * */
    public static void main(String[] args) {
        // Creating a fixed-size thread pool with 2 threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Submitting task for execution using execute()
        executorService.execute(
                () -> log.info("Task 1 executed by: {}", Thread.currentThread().getName())
        );

        executorService.execute(
                () -> log.info("Task 2 executed by: {}", Thread.currentThread().getName())
        );

        // Shutdown the executorService
        executorService.shutdown();

        /*
        output:
        Task 1 executed by: pool-1-thread-1
        Task 2 executed by: pool-1-thread-2
         */

        // execute vs submit: see examples from cexecutesubmit package
    }
}
