package com.desabisc.exceptions.achecked.student;

/**
 * Provides a small, in-memory example of student registration validation.
 *
 * <p>The service deliberately uses checked exceptions to model conditions
 * that the caller can address: an invalid name and an invalid age. No
 * filesystem, database, network, framework, or external library is required.</p>
 *
 * <p>The example also demonstrates an important distinction: the JDK's
 * {@link NumberFormatException} is unchecked, so it does not have to appear
 * in a {@code throws} clause. This service catches that unchecked exception
 * and translates it into the domain-specific checked exception
 * {@link InvalidAgeException}.</p>
 *
 * @see InvalidNameException
 * @see InvalidAgeException
 */
public class StudentRegistrationService {

    /**
     * Creates a registration service.
     */
    public StudentRegistrationService() {
    }

    /**
     * Registers a student after validating the supplied name and age.
     *
     * <p>Both validation failures are declared because this method chooses to
     * propagate them to its caller instead of deciding how the user interface
     * should respond.</p>
     *
     * @param name the student's name; it must contain non-whitespace text
     * @param ageText the student's age represented as an integer string; the
     *                accepted range is 18 through 120, inclusive
     * @throws InvalidNameException if {@code name} is {@code null}, blank, or
     *                              shorter than two characters after trimming
     * @throws InvalidAgeException if {@code ageText} is missing, is not an
     *                             integer, or represents an age outside the
     *                             accepted range
     */
    public void registerStudent(String name, String ageText)
            throws InvalidNameException, InvalidAgeException {

        validateName(name);

        int age = parseAndValidateAge(ageText);

        System.out.printf(
                "Registered student: %s (age %d)%n",
                name.trim(),
                age
        );
    }

    /**
     * Attempts a registration and handles the checked exceptions locally.
     *
     * <p>This method demonstrates the alternative to propagation: the method
     * catches the exceptions and converts the outcome into a boolean result.
     * The {@code finally} block executes whether registration succeeds or
     * fails.</p>
     *
     * @param name the student's name to validate
     * @param ageText the student's age to parse and validate
     * @return {@code true} when registration succeeds; {@code false} when a
     *         checked validation exception occurs
     */
    public boolean tryRegisterStudent(String name, String ageText) {

        try {
            registerStudent(name, ageText);
            return true;

        } catch (InvalidNameException | InvalidAgeException e) {
            System.out.println(
                    "Registration rejected: " + e.getMessage()
            );
            return false;

        } finally {
            System.out.println("Registration attempt finished.");
        }
    }

    /**
     * Validates a student name according to the example's simple rules.
     *
     * @param name the name to validate
     * @throws InvalidNameException if the value is {@code null}, blank, or
     *                              shorter than two characters after trimming
     */
    private void validateName(String name)
            throws InvalidNameException {

        if (name == null || name.isBlank()) {
            throw new InvalidNameException(
                    "Name must not be null or blank."
            );
        }

        if (name.trim().length() < 2) {
            throw new InvalidNameException(
                    "Name must contain at least two characters."
            );
        }
    }

    /**
     * Parses and validates the student's age.
     *
     * <p>The JDK method {@link Integer#parseInt(String)} can throw the
     * unchecked {@link NumberFormatException}. This method catches that
     * low-level exception and wraps it in the checked domain exception
     * {@link InvalidAgeException}, preserving the original cause.</p>
     *
     * @param ageText text expected to contain an integer age
     * @return the validated age
     * @throws InvalidAgeException if the value is missing, cannot be parsed as
     *                             an integer, or is outside the range 18..120
     */
    private int parseAndValidateAge(String ageText)
            throws InvalidAgeException {

        if (ageText == null || ageText.isBlank()) {
            throw new InvalidAgeException(
                    "Age must not be null or blank."
            );
        }

        final int age;

        try {
            age = Integer.parseInt(ageText.trim());

        } catch (NumberFormatException e) {
            throw new InvalidAgeException(
                    "Age must be a whole number: '"
                            + ageText
                            + "'.",
                    e
            );
        }

        if (age < 18 || age > 120) {
            throw new InvalidAgeException(
                    "Age must be between 18 and 120: " + age
            );
        }

        return age;
    }
}