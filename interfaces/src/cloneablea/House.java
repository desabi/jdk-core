package cloneablea;

import java.util.Date;

public class House implements Cloneable, Comparable<House> {
    private int id;
    private double area;
    private Date whenBuild;

    public House(int id, double area) {
        this.id = id;
        this.area = area;
        this.whenBuild = new Date();
    }

    public int getId() {
        return id;
    }

    public double getArea() {
        return area;
    }

    public Date getWhenBuild() {
        return whenBuild;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    @Override
    public int compareTo(House o) {
        if (this.area > o.area) {
            return 1;
        } else if (this.area < o.area) {
            return -1;
        } else {
            return 0;
        }
    }
}
