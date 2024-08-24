package com.desabisc.efixedthread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class CallableExample {

    public static void main(String[] args) {
        // Create a fixed thread pool with 2 threads
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            // Submit callable tasks to the executor
            Future<Integer> future1 = executor.submit(() -> performComputation(10));
            Future<Integer> future2 = executor.submit(() -> performComputation(20));

            try {
                // Retrieve and print the results
                log.info("Result of Task 1: {}", future1.get());
                log.info("Result of Task 2: {}", future2.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }

            // Shut down the executor
            executor.shutdown();
        }
    }

    // Simulate a computation task
    public static int performComputation(int number) {
        log.info("Computing {} in {}", number, Thread.currentThread().getName());
        return number * number;
    }
}
