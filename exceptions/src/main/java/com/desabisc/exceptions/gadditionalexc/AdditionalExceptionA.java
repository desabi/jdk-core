package com.desabisc.exceptions.gadditionalexc;

import java.io.FileReader;
import java.io.IOException;

public class AdditionalExceptionA {

    private static FileReader read() throws IOException {
        // code or exception goes here
        // If this method throws a NullPointerException, the catch block from lines 19 to 26 will be skipped,
        // because isn't a IOExecption. The main method will terminate early.

        // A) if this method does throw an IOException, the catch block from lines 19 to 26 gets run.
        // This exception is handled in line 23 and the handle is done.
        return null;
    }

    public static void main(String[] args) {
        FileReader reader = null;

        try {
          reader = read();
        } catch (IOException e) {
            try {
                if (reader != null)
                    reader.close(); // b) if the close() method does throw an exception, Java looks for more catch blocks.
            } catch (IOException inner) { // c) This last exception is handled here.
                                          // a different exception might be thrown
            }
        }
    }
}
