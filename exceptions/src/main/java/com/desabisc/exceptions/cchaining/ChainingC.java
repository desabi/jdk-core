package com.desabisc.exceptions.cchaining;

public class ChainingC {
    public void visitManatees() {
        try {
        } catch (NumberFormatException e1) {
            System.out.println(e1);
        } catch (IllegalArgumentException e2) {
            //System.out.println(e1); // e1 out of scope
        }
    }
}
