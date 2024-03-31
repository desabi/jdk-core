package com.desabisc.exceptions.fcloseresources;

import java.io.FileInputStream;
import java.io.IOException;

public class TryWithResourcesA {

    // you close the resource to indicate you are done with it.
    public void readFile() {
        FileInputStream is = null;
        try {
            is = new FileInputStream("myfile.txt");
            // read file data
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * --> Java includes the try-with-resources statement to automatically close all resources opened in a try clause.
     * --> Also known as automatic resource management
     * --> Parentheses are required.
     * */
    public void readFileB() {
        // by using try with resources, we guarantee that as soon as a connection passes out of scope, Java will
        // attempt to close it within the same method.
        try (FileInputStream is = new FileInputStream("myfile.txt")) {
            // read file data.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * --> Behind the scenes, the compiler replaces a try-with-resources block with a try and finally block.
     * --> Hidden finally block or implicit finally block.
     * --> You can define a finally block, just be aware that the implicit one will be called first.
     * */
}
