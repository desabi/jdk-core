package cloneablea;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CloneableExamples {
    public static void main(String[] args) {
        Calendar calendar = new GregorianCalendar(2013, 2, 1);
        // copies the reference of calendar to calendar1
        Calendar calendar1 = calendar;
        Calendar calendar2 = (Calendar) calendar.clone();

        System.out.println("calendar == calendar1 is: " + (calendar==calendar1)); // true
        System.out.println("calendar == calendar2 is: " + (calendar==calendar2)); // false
        System.out.println("calendar.equals(calendar2) is: " + calendar.equals(calendar2)); // true

        // == should be used during reference comparison.
        // == checks if both references points to same location or not.

        // equals() method should be used for content comparison.
        // equals() method evaluates the content to check the equality

    }
}
