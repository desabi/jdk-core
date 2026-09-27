package com.desabisc.exceptions.achecked.student;

/**
 * Executable entry point for the checked-exception study example.
 *
 * <p>The program demonstrates two styles of handling checked exceptions:</p>
 *
 * <ol>
 *     <li>propagating them through another method with {@code throws};</li>
 *     <li>catching them inside the method that owns the user-facing decision.</li>
 * </ol>
 *
 * @see StudentRegistrationService
 */
public class CheckedExceptionDemo {

    /**
     * Creates the demo entry-point object.
     */
    public CheckedExceptionDemo() {
    }

    /**
     * Runs the complete checked-exception demonstration.
     *
     * @param args command-line arguments; they are not required by this demo
     */
    public static void main(String[] args) {

        StudentRegistrationService service =
                new StudentRegistrationService();

        System.out.println("=== Successful registration ===");

        service.tryRegisterStudent("Ana", "29");

        System.out.println("\n=== Exception handled inside service ===");

        service.tryRegisterStudent("A", "29");

        System.out.println("\n=== Exception propagated to main ===");

        try {
            runPropagatedExample(service);

        } catch (InvalidNameException | InvalidAgeException e) {

            System.out.println(
                    "main handled propagated exception: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Invokes registration but deliberately transfers the exception-handling
     * responsibility to the caller.
     *
     * @param service the registration service to use
     * @throws InvalidNameException when the supplied name violates the
     *                              registration rules
     * @throws InvalidAgeException when the supplied age is missing, malformed,
     *                             or outside the accepted range
     */
    private static void runPropagatedExample(
            StudentRegistrationService service)
            throws InvalidNameException, InvalidAgeException {

        service.registerStudent(
                "Luis",
                "not-a-number"
        );
    }
}