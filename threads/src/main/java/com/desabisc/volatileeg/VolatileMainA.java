package com.desabisc.volatileeg;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VolatileMainA {
    public static void main(String[] args) {
        VolatileExampleA thread = new VolatileExampleA();
        thread.start();
        try {
            TimeUnit.SECONDS.sleep(1);
            thread.stopRunning();
            thread.join();
        } catch (InterruptedException e) {
            log.error("Thread was interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
