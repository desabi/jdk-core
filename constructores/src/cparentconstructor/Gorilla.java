package cparentconstructor;

public class Gorilla extends TheAnimal {
    // if the parent class has more than one constructor,
    // the child class may use any valid parent constructor in is definition
    public Gorilla(int age) {
        super(age, "Gorilla");
    }

    public Gorilla() {
        super(5);
    }

    // the child constructors are not required to call matching
    // parent constructors
}
