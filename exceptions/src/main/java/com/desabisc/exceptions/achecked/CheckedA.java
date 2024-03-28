package com.desabisc.exceptions.achecked;

import java.io.IOException;

/**
 * A checked exception is an exception that must be declared or handled by the application code where it is thrown.
 * All of them inherit from Exception but not RuntimeException.
 *
 * The handle/declare rule means that all checked exceptions that could be thrown within a method are either
 *  -> wrapped in compatible try catch blocks or
 *  -> declared in the method signature.
 * */
public class CheckedA {

    // how to declare an exception
    // the following method declares that it might throw an IOException, which is a checked Exception
    void fallA(int distance) throws IOException { // throws declares that the might throw an Exception
        if (distance > 10) {
            throw new IOException(); // throw tells Java that you want to throw an Exception
        }
    }

    // how to handle an exception
    // the following alternative version of the previous method handles de exception
    void fallB(int distance) {
        try {
            if (distance > 10) {
                throw new IOException();
            }
        } catch (Exception e) { // IOException is a subclass of exception
            e.printStackTrace();
        }
    }

    // checked exceptions must be handled or declares.
    // while unchecked exceptions can be optionally handled or declared
}
