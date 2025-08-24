Overview

- In a multi-threaded environment, a race condition occurs when two or more threads attempt to update mutable shared data at the same time. Java offers a mechanism to avoid race conditions by synchronizing thread access to shared data.
- A piece of logic marked with synchronized becomes a synchronized block, allowing only one thread to execute at any given time.

Use

We can use the synchronized keyword on different levels:

1. Instance methods
2. Static methods
3. Code blocks

When we use a synchronized block, Java internally uses a monitor, also known as a monitor lock or intrinsic lock, to provide synchronization. These monitors are bound to an object; therefore, all synchronized blocks of the same object can have only one thread executing them at the same time.