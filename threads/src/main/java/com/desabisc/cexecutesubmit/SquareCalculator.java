package com.desabisc.cexecutesubmit;

import java.util.concurrent.Callable;

public class SquareCalculator implements Callable<Integer> {

    private final int number;

    public SquareCalculator(int number) {
        this.number = number;
    }

    @Override
    public Integer call() throws Exception {
        // Simulating a computation (calculating the square of the number)
        Thread.sleep(2000);
        return number * number;
    }
}
