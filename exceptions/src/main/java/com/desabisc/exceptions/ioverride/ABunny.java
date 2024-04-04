package com.desabisc.exceptions.ioverride;

public class ABunny extends AHopper {

    /**
     * -> when a class overrides a method from a superclass or implements a method from an interface, it's not
     *    allowed to add new CHECKED EXCEPTIONS to the method signature.
     * -> THIS RULE APPLIES ONLY TO CHECKED EXCEPTIONS
     * */
    //@Override
    //public void hop() throws CanNotHopException { // DOES NOT COMPILE
    //}

}
