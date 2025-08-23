package com.desabisc.volatileeg;

import lombok.extern.slf4j.Slf4j;

/**
 * En este ejemplo, la variable counter está marcada como volatile.
 * Dos hilos incrementan la variable counter simultáneamente. 
 * La palabra clave volatile garantiza que cada hilo vea el valor más reciente de counter.
 */
@Slf4j
public class VolatileExampleB {
    private volatile int counter = 0;

    public void increment() {
        log.info("Thread: {} Counter: {}", Thread.currentThread().getName(), counter);
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}
