package com.desabisc.exceptions.fcloseresources;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class TryWithResourcesB {

    /**
     * <p>One or more resources can be opened in the try clause.</p>
     * <p>When there are multiple resources opened, they are closed in reverse order.</p>
     * <p>Use semicolons to separate the resources declaration.</p>
     * */
    public void workWithFile() throws Exception {
        try (
                // they are closed in reverse order.
                FileInputStream in = new FileInputStream("data.txt");
                FileOutputStream out = new FileOutputStream("output.txt")
        ) {
            System.out.println("ok");
        }
    } // there is no catch block,  a catch block is optional with a try-with-resources statement.
    // The "finally" clause exists implicitly.

    /**
     * A catch block is optional with a try-with-resources statement.
     * A try statement must have
     *   --> One or more catch blocks or
     *   --> A finally block
     * In the previous example, the "finally" clause exists implicitly.
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
