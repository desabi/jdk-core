package com.desabisc.cexecutesubmit;

import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FactorialCallableTask implements Callable<Long> {

    private final int number;

    public FactorialCallableTask(int number) {
        this.number = number;
    }

    @Override
    public Long call() throws Exception {
        log.info("Starting task, calculate factorial of: {}", number);
        Thread.sleep(4000);
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
