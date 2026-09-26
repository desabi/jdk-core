package com.desabisc.exceptions.efinally;

public class FinallyA {
    /**
     * The finally blocks always executes, whether an exception occurs. </br>
     * a) If an exception is thrown, the finally block is run after the catch block.</br>
     * b) If no exception is thrown, the finally block is run after the try blocks completes.
     * <p>A finally block is typically used to close resources such as files or databases.</p>
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
     * The catch block is not required if finally is present.
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
