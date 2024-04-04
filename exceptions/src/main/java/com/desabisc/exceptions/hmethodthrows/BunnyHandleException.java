package com.desabisc.exceptions.hmethodthrows;

public class BunnyHandleException {
    /**
     * - NoMoreCarrotsException is a checked exception, this class extends Exception.
     * - Checked exceptions must be handled or declared
     * */
    private static void eatCarrot() throws NoMoreCarrotsException {
    }

    /**
     * -> eatCarrot() didn't actually throw an exception, it just declared that it could.
     * -> This is enough for the compiler to require the caller to handle or declare the exception.
     * -> The compiler is still on the lookup for unreachable code. Declaring an unused exception isn't considered
     *    unreachable code
     * */

    // handle the exception
    public static void main(String[] args) {
        try {
            eatCarrot();
        } catch (NoMoreCarrotsException e) { // handle the exception
            System.out.println("sad rabbit");
        }
    }
}
