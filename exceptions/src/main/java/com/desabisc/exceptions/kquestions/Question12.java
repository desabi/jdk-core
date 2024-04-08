package com.desabisc.exceptions.kquestions;

// Original class name: Cat
public class Question12 {
    public String name;

    public void knockSutffOver() {
        System.out.println("1");
        try {
            System.out.println("2");
            int x = Integer.parseInt(name);
            System.out.println("3");
        } catch (NullPointerException e) {
            System.out.println("4");
        }
    }

    public static void main(String[] args) {
        Question12 loki = new Question12();
        loki.name = "loki";
        loki.knockSutffOver();
        System.out.println("6");
    }
}
