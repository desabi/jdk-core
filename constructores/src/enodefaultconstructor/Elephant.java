package enodefaultconstructor;

// error: there is no default constructor available in Mammal
public class Elephant extends Mammal {

    // 1st
    // since Elephant does not define any constructor,
    // the compiler will attempt to insert a default no-argument constructor
    // but there is no default constructor available in Mammal

    /*
    * public Elephant () {
    *    public Elephant() {}
    * }
    * */

    // 2nd (compile-time enhancement)
    // the compiler will auto-insert a call to super() as the first line
    // of the default no-argument constructor

    /*
     * public Elephant () {
     *    public Elephant() {
     *        super(); // does not compile
     *    }
     * }
     * */

    /*
    * You must declare at least one constructor in your child class
    * that explicitly calls a parent constructor via super()
    * */

    public Elephant() {
        super(10);
    }
}
