package com.desabisc.exceptions.achecked.bank;

/**
 * Signals that an applicant's declared age does not satisfy the minimum
 * age requirement for opening a {@link BankAccount}.
 *
 * <p>This is a <strong>checked</strong> exception because it represents a
 * foreseeable, recoverable business condition: the caller (for example, an
 * onboarding UI) can reasonably prompt the applicant again with corrected
 * information. It is not a programming error, so it is modeled as a
 * checked exception rather than a {@link RuntimeException}.</p>
 *
 * <p>Callers should expect this exception whenever
 * {@link AccountValidator#validateAge(String)} is invoked with an age
 * below the required minimum.</p>
 *
 * @see AccountValidator#validateAge(String)
 */
public class InvalidAgeException extends Exception {

    /**
     * Creates a new {@code InvalidAgeException} with a message describing
     * why the applicant's age was rejected.
     *
     * @param message a human-readable explanation of the failure
     */
    public InvalidAgeException(String message) {
        super(message);
    }
}