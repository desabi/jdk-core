package com.desabisc.cexecutesubmit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FactorialCalculatorMain {
    public static void main(String[] args) {
        // Creating a thread pool with 5 threads
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Submitting tasks to the executor
        int[] numbers = {5, 6, 7, 8, 9};
        for (int number : numbers) {
            Future<Long> futureResult = executor.submit(new FactorialCallableTask(number));

            // Retrieving results using Future
            try {
                long result = futureResult.get(); // get() blocks until the task completes
                System.out.println("Factorial of " + number + " is: " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutting down the executor
        executor.shutdown();
    }
}
