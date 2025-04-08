package com.desabisc.acreate;

import lombok.extern.slf4j.Slf4j;

/**
 * Create thread extending Thread
 * */
@Slf4j
public class ThreadReadInventory extends Thread {
    @Override
    public void run() { // overrides method in Thread
        log.info("Extending Thread: Printing zoo inventory");
    }

    public static void main(String[] args) {
        (new ThreadReadInventory()).start();
        log.info("Extending Thread: Main Thread");
    }
}
