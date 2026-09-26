package com.desabisc.exceptions.achecked.bank;

/**
 * Validates applicant data supplied as raw strings (as would arrive from a
 * form or a console prompt) before an account is opened.
 */
public class AccountValidator {

    /** Minimum age, in years, required to open an account. */
    public static final int MINIMUM_AGE = 18;

    /**
     * Parses and validates an applicant's age.
     *
     * <p>This method declares {@link InvalidAgeException} with {@code throws}
     * rather than catching it: age validation is a business rule that
     * belongs to the caller's workflow, so the responsibility for deciding
     * how to react (reject the applicant, ask again, log it) is handed
     * upward instead of being decided here.</p>
     *
     * <p>Note that {@link NumberFormatException} — thrown by
     * {@link Integer#parseInt(String)} when {@code ageInput} is not a
     * valid integer — is <strong>unchecked</strong> and is deliberately
     * <em>not</em> declared. It is included here only to contrast with the
     * checked {@code InvalidAgeException}: the compiler does not require
     * (and would not even allow) declaring it as if it were checked.</p>
     *
     * @param ageInput the applicant's age as text, e.g. {@code "17"}
     * @return the parsed age, guaranteed to be at least {@link #MINIMUM_AGE}
     * @throws InvalidAgeException if the parsed age is below {@link #MINIMUM_AGE}
     * @throws NumberFormatException if {@code ageInput} is not a valid integer (unchecked)
     */
    public int validateAge(String ageInput) throws InvalidAgeException {
        int age = Integer.parseInt(ageInput); // unchecked NumberFormatException may propagate freely
        if (age < MINIMUM_AGE) {
            throw new InvalidAgeException(
                    "Applicant age " + age + " is below the minimum of " + MINIMUM_AGE + ".");
        }
        return age;
    }
}