package com.desabisc.exceptions.kquestions;

public class Sidekick implements AutoCloseable {
    protected String n;
    public Sidekick(String n) {
        this.n = n;
    }

    @Override
    public void close() throws Exception {
        System.out.println("L");
    }

}
