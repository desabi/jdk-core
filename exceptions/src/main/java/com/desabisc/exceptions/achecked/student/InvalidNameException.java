package com.desabisc.exceptions.achecked.student;

/**
 * Indicates that a student name does not satisfy the application's
 * registration rules.
 *
 * <p>This exception is checked because an invalid name is an expected
 * condition that the caller can reasonably handle, for example by asking the
 * user to enter a different value. Extending {@link Exception} rather than
 * {@link RuntimeException} makes the catch-or-declare requirement explicit.</p>
 *
 * @see StudentRegistrationService#registerStudent(String, String)
 */
public class InvalidNameException extends Exception {

    /**
     * Creates an exception with the specified detail message.
     *
     * @param message human-readable explanation of why the name is invalid
     */
    public InvalidNameException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and an underlying cause.
     *
     * @param message human-readable explanation of the validation failure
     * @param cause the exception that caused the validation failure
     */
    public InvalidNameException(String message, Throwable cause) {
        super(message, cause);
    }
}