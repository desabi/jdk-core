package com.desabisc.exceptions.achecked;

import java.io.IOException;

/**
 * <p>A checked exception is an exception that must be declared or handled by the application code where it is thrown.</br>
 * All of them <b>inherit from Exception but not RuntimeException.</b></p>
 * <p>The handle/declare rule means that all checked exceptions that could be thrown within a method are either:</p>
 * 1. Declared in the method signature or </br>
 * 2. Wrapped in compatible try catch blocks
 * */
public class CheckedA {

    // 1. Declared in the method signature
    // The following method declares that it might throw an IOException, which is a checked Exception
    void fallA(int distance) throws IOException { // throws declares that the might throw an Exception
        if (distance > 10) {
            throw new IOException(); // throw tells Java that you want to throw an Exception
        }
    }

    // 2. Wrapped in compatible try catch blocks
    // The following alternative version of the previous method handles de exception
    void fallB(int distance) {
        try {
            if (distance > 10) {
                throw new IOException();
            }
        } catch (Exception e) { // IOException is a subclass of exception
            e.printStackTrace();
        }
    }

    // checked exceptions must be declared or handled.
    // while unchecked exceptions can be optionally handled or declared
}
