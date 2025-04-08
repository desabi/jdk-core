package com.desabisc.acreate.ega;

import lombok.extern.slf4j.Slf4j;

/**
 * Extending Thread Class: You can create a thread by extending the Thread class and overriding its
 * run() method, where the code to be executed concurrently goes.
 */
@Slf4j
public class MyThread extends Thread {

  @Override
  public void run() {
    log.info("Using thread extending Thread");
  }

}
