package com.desabisc.exceptions.achecked.student;

/**
 * Indicates that a student age is missing, malformed, or outside the valid
 * age range accepted by the registration example.
 *
 * <p>This is a checked exception because the invalid value represents a
 * condition that a caller can reasonably recover from by requesting or
 * displaying corrected input.</p>
 *
 * @see StudentRegistrationService#registerStudent(String, String)
 */
public class InvalidAgeException extends Exception {

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message human-readable explanation of why the age is invalid
     */
    public InvalidAgeException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and an underlying cause.
     *
     * @param message human-readable explanation of the validation failure
     * @param cause the exception that caused the validation failure
     */
    public InvalidAgeException(String message, Throwable cause) {
        super(message, cause);
    }
}