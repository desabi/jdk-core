package polymorphism.dynamica;

public class Cat extends Animal {

    // Overriding the makeSound method in another subclass
    @Override
    void makeSound() {
        System.out.println("Cat meows");
    }
}
