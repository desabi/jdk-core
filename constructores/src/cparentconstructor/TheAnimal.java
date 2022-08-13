package cparentconstructor;

public class TheAnimal {
    private int age;
    private String name;

    // if the parent class has more than one constructor,
    // the child class may use any valid parent constructor in is definition
    public TheAnimal(int age, String name) {
        super();
        this.age = age;
        this.name = name;
    }

    public TheAnimal(int age) {
        super();
        this.age = age;
        this.name = null;
    }
}
