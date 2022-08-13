package boverload;

public class AnotherGopher {
    // the first constructor calls the second constructor
    // error: recursive constructor invocation
    public AnotherGopher() {
        this(5);
    }

    // the second constructor calls the firs constructor
    // error: recursive constructor invocation
    public AnotherGopher(int dugHoles) {
        this();
    }
}
