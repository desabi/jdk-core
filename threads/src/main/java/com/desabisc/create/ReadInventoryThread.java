package com.desabisc.create;

import lombok.extern.slf4j.Slf4j;

/**
 * Create thread extending Thread
 * */
@Slf4j
public class ReadInventoryThread extends Thread {
    @Override
    public void run() { // overrides method in Thread
        log.info("Printing zoo inventory");
    }

    public static void main(String[] args) {
        (new ReadInventoryThread()).start();
        log.info("Main Thread");
    }
}
