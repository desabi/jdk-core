package polymorphism.dynamica;

public class DynamicPolymorphismExample {
    public static void main(String[] args) {

        // Creating objects of different classes
        Animal animal = new Animal();
        Dog dog = new Dog();
        Cat cat = new Cat();

        // Polymorphic behavior: calling makeSound method on different objects
        animal.makeSound(); // Output: Animal makes a sound
        dog.makeSound();    // Output: Dog barks
        cat.makeSound();    // Output: Cat meows

        // Using polymorphism in an array
        Animal[] animals = new Animal[3];
        animals[0] = animal;
        animals[1] = dog;
        animals[2] = cat;
        System.out.println();

        // Iterating through the array and calling makeSound on each object
        for (Animal a : animals) {
            a.makeSound();
        }
    }
}
