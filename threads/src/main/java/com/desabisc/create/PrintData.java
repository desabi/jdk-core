package com.desabisc.create;

import lombok.extern.slf4j.Slf4j;

/**
 * Create thread implementing Runnable
 * */
@Slf4j
public class PrintData implements Runnable {
    @Override
    public void run() { // overrides method in Runnable
        for (int i = 0; i < 3; i++) {
            log.info("Printing record = " + i);
        }
    }

    public static void main(String[] args) {
        (new Thread(new PrintData())).start();
        log.info("Main thread");
    }
}
