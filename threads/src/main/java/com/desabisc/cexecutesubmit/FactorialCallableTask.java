package com.desabisc.cexecutesubmit;

import java.util.concurrent.Callable;

public class FactorialCallableTask implements Callable<Long> {

    private final int number;

    public FactorialCallableTask(int number) {
        this.number = number;
    }

    @Override
    public Long call() throws Exception {
        return calculateFactorial(number);
    }

    private long calculateFactorial(int n) {
        long factorial = 1;
        for (int i = 1; i <= n; i++) {
            factorial *= i;
        }
        return factorial;
    }
}
