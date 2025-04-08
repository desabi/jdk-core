package com.desabisc.acreate.ega;

public class MainThreads {
  public static void main(String[] args) {
    // Extending Thread class
    MyThread myThread = new MyThread();
    // Implementing Runnable
    Thread myRunnable = new Thread(new MyRunnable());

    myThread.start();
    myRunnable.start();

    // When multiple threads access shared resources concurrently, synchronization is essential
    // to prevent data inconsistency or conflicts.

    // In general, you should extend the Thread class only under specific circumstances,
    // such as when you are creating your own priority-based thread.

    // In most situations, you should implement the Runnable interface rather than extend the
    // Thread class.

    // We can now create and manage threads indirectly using an ExecutorService.
  }
}
