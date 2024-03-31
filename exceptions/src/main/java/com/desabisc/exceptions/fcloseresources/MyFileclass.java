package com.desabisc.exceptions.fcloseresources;

public class MyFileclass implements AutoCloseable {
    private final int num;

    public MyFileclass(int num) {
        this.num = num;
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing: " + num);
    }
}
