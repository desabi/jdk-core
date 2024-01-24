package com.desabisc.examplea;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZooInfo {
    /**
     * Java includes the Concurrency API to handle the complicated work of managing threads for you.
     * API includes the ExecutorService interface, which defines services that create and manage threads for you.
     * */
    public static void main(String[] args) {
        ExecutorService executorService = null;

        Runnable task1 = () -> System.out.println("Printing zoo inventory");
        Runnable task2 = () -> {
            for (int i = 0; i < 3; i++) {
                System.out.println("Printint record = " + i);
            }
        };

        try {
            executorService = Executors.newSingleThreadExecutor();
            System.out.println("begin");
            executorService.execute(task1);
            executorService.execute(task2);
            executorService.execute(task1);
            System.out.println("end");
        } finally {
            if (executorService != null)
                executorService.shutdown();
        }
    }
}
