package com.desabisc.cexecutesubmit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FactorialCalculatorMain {

  public static void main(String[] args) {
    // Creating a thread pool with 5 threads
    try (ExecutorService executor = Executors.newFixedThreadPool(5)) {

      // Submitting tasks to the executor
      int[] numbers = {5, 6, 7, 8, 9};

      for (int number : numbers) {
        Future<Long> futureResult = executor.submit(new FactorialCallableTask(number));

        // Retrieving results using Future
        try {
          long result = futureResult.get(); // get() blocks until the task completes
          log.info("Factorial of {} is: {}", number, result);
        } catch (InterruptedException | ExecutionException e) {
          log.error("Exception: {}", e.getMessage());
        }
      }

      // Shutting down the executor
      executor.shutdown();
    }
  }
}
