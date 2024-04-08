package com.desabisc.exceptions.kquestions;

public class Question2 {

    public void ohNo(ArithmeticException ae) throws Exception {
        if (ae==null){
            throw new Exception();
        } else {
            throw ae;
        }
    }
}
