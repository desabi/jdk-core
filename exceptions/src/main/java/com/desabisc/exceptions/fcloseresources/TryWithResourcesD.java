package com.desabisc.exceptions.fcloseresources;

public class TryWithResourcesD {
    public static void main(String[] args) {
        try (MyFileclass a1 = new MyFileclass(1);
            MyFileclass a2 = new MyFileclass(2)) { // is executed in reverse order.
            throw new RuntimeException();
        } // finally is executed. close method is executed, prints the number 2 and 1.
        catch (Exception e) {
            System.out.println("ex");
        } finally {
            System.out.println("finally");
        }
    }

    /**
     * -> Does a try-with-resources guarantee a resource will be closed? No.
     * -> The try-with resources statement guarantees only the close() method will be called.
     * -> If the close method() encounters an exception of its own or the method is implemented poorly, a resource
     *    leak can still occur.
     * */
}
