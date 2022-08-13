package cparentconstructor;

public class AnotherZoo {
    public AnotherZoo() {
        super(); // refers to constructor in java.lang.Object
        System.out.println("Zoo created");
        super(); // super() can only be called once
    }
}
