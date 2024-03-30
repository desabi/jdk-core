package com.desabisc.exceptions.efinally;

public class FinallyA {
    /**
     * --> The finally blocks always executes, whether or not an exception occurs.
     * --> If an exception is thrown, the finally block is run after the catch block.
     * --> If no exception is thrown, the finally block is run after the try blocks completes.
     * --> A finally block is typically used to close resources such as files or databases.
     * */
    public void tryCatchFinally() {
        try {
            fall();
        } catch (Exception e) {
            System.out.println(e);
        } finally { // the finally blocks always executes
            System.out.println("all better");
        }
    }

    /**
     * --> The catch block is not required if finally is presente.
     * */
    public void tryWithFinally() {
        try {
            fall();
        } finally {
            System.out.println("all better");
        }

    }

    public void fall(){}
}
