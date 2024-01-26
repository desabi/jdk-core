package polymorphism.dynamicb;

public class Splendor extends Bike {

    @Override
    void run() {
        System.out.println("walking safely with 30km");
    }

    public static void main(String[] args) {
        Bike bike1 = new Bike();
        Bike bike2 = new Splendor(); //upcasting

        bike1.run(); // bike running
        bike2.run(); // walking safely with 30km
    }
}
