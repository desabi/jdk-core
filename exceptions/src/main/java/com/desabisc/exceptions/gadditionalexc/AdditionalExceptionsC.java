package com.desabisc.exceptions.gadditionalexc;

public class AdditionalExceptionsC {
    public static String exceptions() {
        StringBuilder result = new StringBuilder();
        String v = null;

        try {
            try {
                result.append("before_");
                v.length(); // throws a NullPointerException
                result.append("after_"); // this line is skipped
            } catch (NullPointerException e) { // catch the exception
                result.append("catch_"); // this is added to result
                throw new RuntimeException(); // exception is thrown
            } finally {
                result.append("finally_"); // this is added to result
                throw new Exception(); // this exception is thrown
            }
        } catch (Exception e) { // the outer catch block then sees and exception was thrown and catches it.
            result.append("done"); // this is added to result
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String result = exceptions();
        System.out.println("result = " + result);
    }
}
