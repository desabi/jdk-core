package cloneableb;

public class HouseMain {
    public static void main(String[] args) {
        House house1 = new House(1, 1750.50);
        House house2 = (House) house1.clone();

        System.out.println("¿house1 == house2? : " + (house1==house2));
        System.out.println("¿house1.whenBuild = house2.whenBuild? : "+ (house1.getWhenBuild() == house2.getWhenBuild()));
    }
}
