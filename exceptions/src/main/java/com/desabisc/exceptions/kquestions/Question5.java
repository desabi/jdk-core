package com.desabisc.exceptions.kquestions;

public class Question5 {
    public static void main(String[] args) {
        Object obj = Integer.valueOf(3);
        String str = (String) obj; // ClassCastException
        obj = null;
        System.out.println(obj.equals(null));
    }
}
