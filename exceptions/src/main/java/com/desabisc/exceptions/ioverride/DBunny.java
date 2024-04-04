package com.desabisc.exceptions.ioverride;

public class DBunny extends AHopper {
    /**
     * -> The following code is legal because it has an unchecked exception.
     * -> The reason that it's okay to declare new unchecked exceptions in a subclass method is that the declaration
     *    is redundant.
     * -> Methods are free to throw any unchecked exceptions they want without mentioning them in the method declaration.
     * */
    @Override
    public void hop() throws IllegalStateException {
    }
}
