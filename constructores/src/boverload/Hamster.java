package boverload;

public class Hamster {
    private String color;
    private int weight;

    /*
    public Hamster(int weight) {
        this.weight = weight; // duplicate
        color = "brown";
    }

    public Hamster(int weight, String color) {
        this.weight = weight; // duplicate
        this.color = color;
    }
    */

    public Hamster(int weight, String color) {
        this.weight = weight; // duplicate
        this.color = color;
    }

    // a constructor call another constructor
    public Hamster(int weight) {
        // the this() call must be the first statement in the constructor
        this(weight, "brown");
    }

    // this refers to a instance of the class
    // this () refers to a constructor call within the class
}
