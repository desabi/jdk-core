package com.desabisc.exceptions.jprintexc;

public class PrintException {

    private static void hop() {
        throw new RuntimeException("Cannot hop");
    }
    /**
     * --> There are three ways to print an exception.
     *    a) You cant let java print it out
     *    b) print just the message
     *    c) print where the stack trace comes from
     * */
    public static void main(String[] args) {
        try {
            hop();
        } catch (Exception e) {
            System.out.println(e);  // a) You cant let java print it out
            System.out.println(e.getMessage()); // b) print just the message
            e.printStackTrace(); // c) print where the stack trace comes from
        }
    }
}
