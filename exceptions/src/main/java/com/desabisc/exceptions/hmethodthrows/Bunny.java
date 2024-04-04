package com.desabisc.exceptions.hmethodthrows;


public class Bunny {


    public static void main(String[] args) {
        //eatCarrot(); // DOES NOT COMPILE
    }

    /**
     * - NoMoreCarrotsException is a checked exception, this class extends Exception.
     * - Checked exceptions must be handled or declared
     * */
    private static void eatCarrot() throws NoMoreCarrotsException {
    }

    /**
     * -> eatCarrot() didn't actually throw an exception, it just declared that it could.
     * -> This is enough for the compiler to require the caller to handle or declare the exception.
     * */

}
