package com.desabisc.cexecutesubmit;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class SubmitExample {
    /**
     * The submit() method is similar to execute(), but it returns a Future object,
     * which can be used to retrieve the result of the computation.
     * */
    public static void main(String[] args) {
        // Creating a single-threaded executor service
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Submitting a Callable task for execution using submit()
        Future<String> future = executorService.submit(
                () -> {
                    Thread.sleep(2000); // Simulating a time-consuming task
                    return "Task completed by: " + Thread.currentThread().getName();
                }
        );

        try {
            // Blocking and getting the result from the future
            String result = future.get();
            log.info("Result: {}", result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted exception: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }

        /*
        * In the SubmitExample, we use a Callable instead of a Runnable because submit() expects a Callable
        * which can return a result. The Future object allows us to retrieve the result once the task is completed.
        * */

        // Shutdown the executorService
        executorService.shutdown();

        // ocp book, pag 852
        /*
        * We tend to prefer submit() over execute(), even if you don´t store the Future reference
        * */
    }
}
