package com.desabisc.exceptions.cchaining;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ChainingB {
    /**
     * Java looks at the order of the catch blocks. If it is impossible for one of the catch blocks to be executed,
     * a compiler error about unreachable code occurs.
     *
     * This happens when a super class catch block appears before a subclass catch block.
     * */

    public void visitMonkeys() {
        try {
            seeAnimal();
        } catch (ExhibitClosedForLunch e) { // subclass exception
            System.out.println("Try back later");
        } catch (ExhibitClosed e) { // superclass exception
            System.out.println("Not today");
        }
        // the reverse order does not work
    }

    public void visitMonkeysError() {
        try {
            seeAnimal();
        } catch (ExhibitClosed  e) { // superclass exception
            System.out.println("Try back later");
        } catch (ExhibitClosedForLunch e) { // subclass exception. Exception has already been caught
            System.out.println("Not today");
        }
        // if the more specific ExhibitClosedForLunch exception is thrown, the catch block for ExhibitClosed runs.
    }

    void seeAnimal() {}

    // another example
    public void visitSnakes() {
        try {
        } catch (IllegalArgumentException e) { // superclass exception
        } catch (NumberFormatException e) { // subclass exception. Exception has already been caught
        }
        /**
         * NumberFormatException is a subclass of NumberFormat exception.
         * Since NumberFormatException is a subclass, it will always be caught by the first catch block,
         * making the second catch block unreachable code that does not compile.
         * */
    }

    // the previous examples are unchecked exceptions, inherits from RuntimeException
    // other example
    public void readFile() {
        try {
        } catch (IOException e) { // superclass exception: checked exception
        } catch (FileNotFoundException e) { // subclass exception. Exception has already been caught
        }
    }
}
