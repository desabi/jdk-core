package com.desabisc.exceptions.kquestions;

// Original class name: MoreHelp
public class Question24 {
    public void requireAssistance() {
        try (Sidekick is = new Sidekick("Adeline")) {
            System.out.println("O");
        } finally {
            System.out.println("k");
        }
    }

    public static void main(String[] args) {
        new Question24().requireAssistance();
        System.out.println("I");
    }
}
