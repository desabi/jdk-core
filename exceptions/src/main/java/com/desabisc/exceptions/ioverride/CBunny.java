package com.desabisc.exceptions.ioverride;

public class CBunny extends BHopper {

    /**
     * - A class is allowed to declare a subclass of an exception type.
     * - The superclass or interface has already taken care of a broader type.
     * - Broader type: Exception.
     * - Subclass Type: CanNotHopException in CBunny.java class
     *
     * --> CBunny could declare that it throws the Exception directly,
     * --> or it could declare that it throws a more specific type of exception (CanNotHopException?)
     * --> It could even declare that it throws nothing at all.
     *
     * -> THIS RULE APPLIES ONLY TO CHECKED EXCEPTIONS
     *
     * */
    @Override
    public void hop() throws CanNotHopException {
    }
}
