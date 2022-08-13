package cparentconstructor;

public class Zebra extends Animal {
    public Zebra(int age) {
        super(age); // refers to constructor in animal
    }

    public Zebra() {
        this(4); // refers to constructor in Zebra with int argument
    }
}
