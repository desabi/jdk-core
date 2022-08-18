package cloneablea;

public class HouseMain {
    public static void main(String[] args) {

        /* two different objects with identical content */
        House house1 = new House(1, 1750.50);
        House house2 = (House) house1.clone();

        /*
        * The clone method in the Object class copies each field
        * from the original object to the target object.
        *
        * If the field is of primitive type, its value is copied.
        * If the field is an object, the reference of the field is copied. (shallow copy)
        * */

        // house1 and house2 are diferrent object with the same contents
        System.out.println("house1 == house2: " + (house1==house2));

        // shallow copy
        System.out.println("house1.whenBuilt  ==  house2.whenBuilt: " + (house1.getWhenBuild() == house2.getWhenBuild()));

        /*
        * the default clone method performs a shallow copy.
        * the custom clone method performs a deep copy.
        * */

    }
}
