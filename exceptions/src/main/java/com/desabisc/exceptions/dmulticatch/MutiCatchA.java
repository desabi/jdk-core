package com.desabisc.exceptions.dmulticatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.MissingResourceException;

public class MutiCatchA {
    /**
     * --> Multi catch is intended to be used for exceptions that aren´t related.
     * --> The only difference between multi-catch blocks and chaining catch blocks is that:
     *     order does not matter for a multi-catch block within a single catch expression.
     * */
    public static void main(String[] args) {
        try {
            System.out.println(Integer.parseInt(args[1]));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Missing or invalid input");
        }
    }

    private void mightThrow() throws DateTimeParseException, IOException {
    }
    /*public void doesNotCompile() {
        try {
            mightThrow();
        } catch (FileNotFoundException | IllegalStateException e) {
        } catch (InputMismatchException e | MissingResourceException e) { // extra variable name InputMismatchException e
        } catch (FileNotFoundException | IllegalArgumentException e) { // FileNotFoundException was already caught
            // you can´t list the same exception type more than once in the same try statement
        } catch (Exception e) { // this and the next are reversed
        } catch (IOException e) { // the more general superclass (Exception) must be caught after their subclasses (IOException)

        }
    }*/
}
