package com.desabisc.exceptions.efinally;

public class FinallyB {

    /**
     * Possible results: 
     * --> 1 and 3 if no exception happens.
     * --> 2 and 3 if exception happens.
     * */
    public static int goHome() {
        try {
            // Optionally thrown an exception here
            System.out.println("1");
            return 1;
        } catch (Exception e) {
            System.out.println("2");
            return 2;
        } finally {
            System.out.println("3");
            return 3; // always returns 3
            /*
            * Because the finally block is executed shortly before the method completes,
            * it interrupts the return statement from inside both the try and catch blocks.
            * */
        }
    }

    public static void main(String[] args) {
        int result = goHome();
        System.out.println("result = " + result);
    }

    /**
     * --> While a finally block will always be executed it may not finish
     * */
    public String finallyNoExecuted() {
        MyInfo info = null;
        try {
            System.out.println("inside try");
        } finally {
            info.printDetails(); // if info is null, then the finally block would be executed, but it would stop on this line
                                 // and throw a NullPointerException, the next two lines would not be executed.
            System.out.println("Exiting");
            return "Zoo";
        }
    }

    /**
     * There is one exception to "the finally block always be executed"
     * */
    public void finallyRuleException() {
        try {
            System.exit(0); // Tells Java: Stop. End the program right now. Do not pass go.
        } finally {
            System.out.println("Never going to get here"); // Not printed
        }
    }
}
