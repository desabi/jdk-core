package dcompilerenhancements;

// the folowing three class and constructor
// definition are equivalents because the
// compiler will automatically convert them all
// to the last example

public class Donkey {
    // default constructor added
    // Is supplied if there are no constructor present.
}

public class Donkey {
    public Donkey();
}

public class Donkey {
    public Donkey() {
        super();
    }
}