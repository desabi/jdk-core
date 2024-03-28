package com.desabisc.exceptions.cchaining;

public class ChainingA {

    /**
     * In this example, there are three custom exceptions. All are unchecked exceptions because they directly
     * or indirectly extend RuntimeException.
     *
     * There are three possibilities:
     * */
    public void visitPorcupine() {
        try {
            seeAnimal(); // if this doesn't throw an exception, nothing is printed out.
        } catch (AnimalsOutForWalk e) {
            System.out.println("Try back later"); // if the animal is out for a walk, only the first catch blocks run.
        } catch (ExhibitClosed e) {
            System.out.println("Not today"); // if the exhibit is closed, only the second catch block runs.
        }
        // It is not possible for both catch blocks to be executed when chained together like this.
        // The Order of the catch block could be reversed because the exceptions don't inherit from each other.
    }

    void seeAnimal() {}
}

