package com.desabisc.exceptions.bunchecked;

/**
 * An unchecked exception in any exception that does not need to be declared or handled by the application code
 * where it is thrown.
 *
 * Are often referred as runtime exceptions, include any class that inherits RuntimeException or Error classes.
 * */
public class UncheckedA {

    // example: a NullPointerException can be thrown in the body of the following method if the input reference
    // is null.
    void fall(String input) {
        System.out.println(input.toLowerCase());
    }
}
