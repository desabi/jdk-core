package com.desabisc.exceptions.fcloseresources;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TryWithResourcesB {

    /**
     * --> One or more resources can be opened in the try clause.
     * --> When there are multiple resources opened, thy are closed in reverse order.
     * --> Use semicolons to separate the resources declaration.
     * */
    public void workWithFile() throws Exception {
        try (
                FileInputStream in = new FileInputStream("data.txt");
                FileOutputStream out = new FileOutputStream("output.txt")
        ) {
            System.out.println("ok");
        }
    } // there is no catch block

    /**
     * A catch block is optional with a try-with-resources statement.
     * A try statement must have
     *   --> One or more catch blocks or
     *   --> A finally block
     * In the previous example, the finally clause exists implicitly.
     *
     * --> It is still valid to have a catch and/or a finally blocks.
     * --> Only a try-with-resources statement is permitted to omit both the catch and finally blocks.
     * --> The implicit finally block runs before any programmer-code ones.
     * --> The implicit finally runs before any catch/finally blocks that you code yourself.
     *
     * --> Two or more programmer-defined finally blocks are not allowed.
     * --> The implicit finally block defined by the compiler is not counted here.
     *
     * --> In a try-with-resources statement, Java requires classes that implements the AutoCloseable interface.
     * */
}
