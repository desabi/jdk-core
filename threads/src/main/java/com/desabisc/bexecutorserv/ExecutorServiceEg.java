package com.desabisc.bexecutorserv;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Java includes the Concurrency API to handle the complicated work of managing threads for you.
 * API includes the ExecutorService interface, which defines services that create and manage threads for you.
 * */
@Slf4j
public class ExecutorServiceEg {
    /**
     * The execute() method takes a Runnable as a parameter and submits it for execution.
     * It doesn't return any result.
     * There is another method, submit(). More examples in next packages.
     * */
    public static void main(String[] args) {

        // Creating a fixed-size thread pool with 2 threads
      try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {

        // Submitting tasks for execution using execute()
        executorService.execute(
            () -> log.info("Task 1 executed by: {}", Thread.currentThread().getName())
        );

        Runnable runnable = () -> log.info("Task 2 executed by: {}",
            Thread.currentThread().getName());
        executorService.execute(runnable);

        // Shutdown the executorService
        executorService.shutdown();
      }

        /*
        output:
        Task 1 executed by: pool-1-thread-1
        Task 2 executed by: pool-1-thread-2
         */

        // execute vs submit: see examples from cexecutesubmit package
    }
}
