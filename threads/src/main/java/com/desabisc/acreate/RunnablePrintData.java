package com.desabisc.acreate;

import lombok.extern.slf4j.Slf4j;

/**
 * Create thread implementing Runnable
 * */
@Slf4j
public class RunnablePrintData implements Runnable {
    @Override
    public void run() { // overrides method in Runnable
        log.info("Implementing Runnable: print data");
        for (int i = 0; i < 3; i++) {
            log.info("Printing record = {}", i);
        }
    }

    public static void main(String[] args) {
        (new Thread(new RunnablePrintData())).start();
        log.info("Runnable: Main thread");
    }
}
