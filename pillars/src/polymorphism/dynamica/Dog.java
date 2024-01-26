package polymorphism.dynamica;

public class Dog extends Animal {
    // Overriding the makeSound method in the subclass

    @Override
    void makeSound() {
        System.out.println("Dog barks");
    }
}
