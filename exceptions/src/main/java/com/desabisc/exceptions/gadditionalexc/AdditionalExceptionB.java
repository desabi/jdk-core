package com.desabisc.exceptions.gadditionalexc;

public class AdditionalExceptionB {
    public static void main(String[] args) throws Exception {
        try {
            throw new RuntimeException(); // this exception is catch in line 7
        } catch (RuntimeException e) {
            throw new RuntimeException(); // this is the same exception as the above and that's why is handled correctly
        } finally {
            throw new Exception(); // at the end, this exception is throw, and the two exceptions above, gets forgotten about
            // this is why you often see another try/catch inside a finally block, to make sure it doesn't mask the
            // exception from the catch block
        }
    }
}
