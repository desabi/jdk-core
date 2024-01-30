package com.desabisc.executorserv;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ZooInfo {
    /**
     * Java includes the Concurrency API to handle the complicated work of managing threads for you.
     * API includes the ExecutorService interface, which defines services that create and manage threads for you.
     * */
    public static void main(String[] args) {
        ExecutorService executorService = null;

        Runnable task1 = () -> log.info("Printing zoo inventory");
        Runnable task2 = () -> {
            for (int i = 0; i < 3; i++) {
                log.info("Printint record = " + i);
            }
        };

        try {
            executorService = Executors.newSingleThreadExecutor();
            log.info("begin");
            executorService.execute(task1);
            executorService.execute(task2);
            executorService.execute(task1);
            log.info("end");
        } finally {
            if (executorService != null)
                executorService.shutdown();
        }
    }
}
