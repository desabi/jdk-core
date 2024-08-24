package com.desabisc.cexecutesubmit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class SubmitExampleA {
    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Runnable runnable = () -> {
            for (int i = 0; i < 3; i++) {
                log.info("Printing record = " + i);
            }
        };

        // method submit with a runnable
        executorService.submit(runnable);

        // method submit with a callable
        Future<Integer> future = executorService.submit(new SquareCalculatorCallable(5));

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
