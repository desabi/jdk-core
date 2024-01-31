package com.desabisc.cexecutesubmit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class SubmitExampleA {
    public static void main(String[] args) {
        // Creating a fixed-size thread pool with 3 threads
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Future<Integer> future = executorService.submit(new SquareCalculator(5));

        try {
            // Blocking and getting the result from the Future
            int result = future.get();
            log.info("Result: {}", result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }

        // Shutdown the executorService
        executorService.shutdown();
    }
}
