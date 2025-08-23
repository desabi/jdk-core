package com.desabisc.volatileeg;

import lombok.extern.slf4j.Slf4j;

/**
 * En este ejemplo, la variable running está marcada como volatile.
 * El método run comprueba la variable running en un bucle.
 * El método stopRunning establece running en false, lo que detiene el bucle.
 * La palabra clave volatile garantiza que los cambios en running sean visibles
 * inmediatamente para el método run.
 */
@Slf4j
public class VolatileExampleA extends Thread {
    private volatile boolean running = true;

    public void run() {
        while (running) {
            // Thread is running
            log.info("Thread: {} is running", Thread.currentThread().getName());
        }
        log.info("Thread stopped");
    }

    public void stopRunning() {
        running = false;
    }
    
}
