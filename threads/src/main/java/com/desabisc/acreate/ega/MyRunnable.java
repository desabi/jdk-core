package com.desabisc.acreate.ega;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementing Runnable Interface: alternatively, you can implement the Runnable interface,
 * which provides a way to run code in a thread.
 */
@Slf4j
public class MyRunnable implements Runnable {

  @Override
  public void run() {
    log.info("Using thread implementing runnable");
  }
}
