package com.desabisc.acreate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainThreads {
    public static void main(String[] args) {
        log.info("begin");
        (new ThreadReadInventory()).start();
        (new Thread(new RunnablePrintData())).start();
        (new ThreadReadInventory()).start();
        log.info("end");
    }
}
