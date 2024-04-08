package com.desabisc.exceptions.kquestions;

// Original Class Name: God
public class Question11 {

    public String name;

    public void runAway() {
        System.out.println("1");
        try {
            System.out.println("2");
            int x = Integer.parseInt(name);
            System.out.println("3");
        } catch (NumberFormatException e) {
            System.out.println("4");
        }
    }

    public static void main(String[] args) {
        Question11 webby = new Question11();
        webby.name = "Webby";
        webby.runAway();
        System.out.println("5");
    }
}
