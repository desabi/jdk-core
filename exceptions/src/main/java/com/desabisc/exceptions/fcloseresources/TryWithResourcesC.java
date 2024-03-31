package com.desabisc.exceptions.fcloseresources;

import java.util.Scanner;

public class TryWithResourcesC {
    /**
     * --> The implicit finally runs before any catch/finally blocks that you code yourself.
     * */
    public void methodTest() {
        try (Scanner s = new Scanner(System.in)) {
            s.nextLine();
        } // finally is executed. Scanner has gone out of scope at the end of the try clause.
        catch (Exception e) {
           // s.nextInt();
        } finally {
           // s.nextInt();
        }
    }
}
