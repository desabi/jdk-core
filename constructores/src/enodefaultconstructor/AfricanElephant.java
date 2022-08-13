package enodefaultconstructor;

/*
* Subclasses of Elephant class can rely on compiler enhancements.
* */
public class AfricanElephant extends Elephant {
}

/*
* public class AfricanElephant extends Elephant {
*     public AfricanElephant(){}
* }
*
* public class AfricanElephant extends Elephant {
*     public AfricanElephant(){
*         super(); // calls default constructor (no-arg)
*     }
* }
*
*  super() always refers to the most direct parent.
* */
