package comparable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PersonaMain {
    public static void main(String[] args) {

        // ordenar personas por edad

        List<Persona> personas = Arrays.asList(
                new Persona("pedro", 20),
                new Persona("juan", 30),
                new Persona("maria", 40),
                new Persona("gema", 15)
        );

        Collections.sort(personas);

        for (Persona p :personas) {
            System.out.println(p.getNombre() + " - " + p.getEdad());
        }

    }
}
