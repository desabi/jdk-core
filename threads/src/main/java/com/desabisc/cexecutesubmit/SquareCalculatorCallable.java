package com.desabisc.cexecutesubmit;

import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SquareCalculatorCallable implements Callable<Integer> {

    private final int number;

    public SquareCalculatorCallable(int number) {
        this.number = number;
    }

    @Override
    public Integer call() throws Exception {
        // Simulating a computation (calculating the square of the number)
        log.info("Start processing a long task...");
        Thread.sleep(5000); // five seconds
        return number * number;
    }
}
