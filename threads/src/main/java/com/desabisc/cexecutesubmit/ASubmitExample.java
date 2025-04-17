package com.desabisc.cexecutesubmit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The submit() method is similar to execute(), but it returns a Future object, which can be used to
 * retrieve the result of the computation.
 */
@Slf4j
public class ASubmitExample {

  public static void main(String[] args) {
    try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {

      Runnable runnable = () -> {
        for (int i = 0; i < 3; i++) {
          log.info("Printing record = {}", i);
        }
      };

      // method submit() with a runnable, it returns a Future<?> but it is not used
      // passing a runnable, return type void
      executorService.submit(runnable);

      // method submit() with a callable, it returns a Future<Integer>
      // passing a callable, returns a value, call method from SquareCalculatorCallable
      Future<Integer> future = executorService.submit(new SquareCalculatorCallable(5));

      try {
        // Blocking and getting the result from the Future<Integer>
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
}
