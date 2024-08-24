package com.desabisc.efixedthread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Consider a scenario where you have a set of web requests to process.
 * Using a fixed thread pool ensures that only a limited number of requests are processed concurrently,
 * preventing overwhelming the server.
 */
@Slf4j
public class WebRequestHandler {
    /**
     * Simultaneous Request Handling: The fixed thread pool allows up to 4 web requests to be processed concurrently.
     * Additional requests will wait until a thread becomes available.
     * Thread Reuse: Once a thread finishes processing a request, it can be reused to handle another request.
     */
    public static void main(String[] args) {
        // Create a fixed thread pool with 4 threads
        try (ExecutorService requestHandler = Executors.newFixedThreadPool(4)) {

            // Simulate handling 8 web requests
            for (int i = 1; i <= 8; i++) {
                int requestId = i;
                requestHandler.submit(() -> handleRequest(requestId));
            }

            // Shut down the request handler executor
            requestHandler.shutdown();
        }
    }

    // Simulate handling a web request
    public static void handleRequest(int requestId) {
        log.info("Handling request {} in {}", requestId, Thread.currentThread().getName());
        // Simulate time taken to process the request
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Completed request {} in {}", requestId, Thread.currentThread().getName());
    }
}
