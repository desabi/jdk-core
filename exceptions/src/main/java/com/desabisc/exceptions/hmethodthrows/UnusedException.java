package com.desabisc.exceptions.hmethodthrows;

public class UnusedException {

    private void eatCarrot() {
    }

    /**
     * - Declaring an unused exception isn't considered unreachable code.
     * - It gives the method the option to change the implementation to thrown that exception in the future.
     * */
    public void good() throws NoMoreCarrotsException { // unused exception
        // B) In comparison, good() is free to declare other exceptions.
    }

//    public void bad() {
//        try {
//            eatCarrot(); // A) java knows that eatCarrot() can't thrown a checked exception, which means that there's
//                         //    no way fot the catch block in bad tp be reached
//        } catch (NoMoreCarrotsException e) { // DOES NOT COMPILE
//            System.out.println("sad rabbit");
//        }
//    }

}
