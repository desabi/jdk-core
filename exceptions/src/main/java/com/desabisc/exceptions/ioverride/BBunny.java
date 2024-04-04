package com.desabisc.exceptions.ioverride;

public class BBunny extends BHopper {

    /**
     * -> An overridden method in a subclass is allowed to declare fewer exceptions than the superclass or interface.
     *    This is legal because callers are already handling them.
     * -> An overridden method not declaring one of the exceptions thrown by the parent method is similar to the method
     *    declaring it throws an exception that it never actually throws.
     *  -> THIS RULE APPLIES ONLY TO CHECKED EXCEPTIONS
     * */
    @Override
    public void hop() {
    }
}
