package cparentconstructor;

public class Zoo {
    public Zoo() {
        System.out.println("Zoo created");
        // super() can only be used as the first statement on the constructor
        super(); // refers to constructor in java.lang.Object
    }
}
