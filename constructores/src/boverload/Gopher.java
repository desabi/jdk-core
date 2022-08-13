package boverload;

public class Gopher {
    // the constructor is calling itself infinitely
    // error: Recursive constructor invocation
    public Gopher(int dugHoles) {
        this(5);
    }
}
