package com.desabisc.exceptions.achecked.bank;

/**
 * Orchestrates account onboarding and withdrawal workflows, showing both
 * checked-exception handling strategies side by side.
 */
public class AccountService {

    private final AccountValidator validator = new AccountValidator();

    /**
     * Opens a new account after validating the applicant's age.
     *
     * <p><strong>Strategy: declare and propagate.</strong> This method
     * calls {@link AccountValidator#validateAge(String)}, which can throw
     * {@link InvalidAgeException}. Rather than catching it here,
     * {@code openAccount} declares it with {@code throws}, because opening
     * an account is itself just one step of a larger onboarding flow
     * (see {@link Main}) that is better positioned to decide how to react
     * to a rejected applicant (e.g. show a form error).</p>
     *
     * @param ownerName the applicant's name
     * @param ageInput the applicant's age as text
     * @param initialDeposit the opening balance
     * @return a new {@link BankAccount} if the applicant is old enough
     * @throws InvalidAgeException if the applicant does not meet the minimum age
     */
    public BankAccount openAccount(String ownerName, String ageInput, double initialDeposit)
            throws InvalidAgeException {
        validator.validateAge(ageInput); // propagated, not caught, on purpose
        return new BankAccount(ownerName, initialDeposit);
    }

    /**
     * Attempts a withdrawal and reports whether it succeeded.
     *
     * <p><strong>Strategy: catch and translate to a return value.</strong>
     * This method calls {@link BankAccount#withdraw(double)}, which can
     * throw {@link InsufficientBalanceException}. Here the exception
     * <em>is</em> caught, because {@code AccountService} can meaningfully
     * decide what "insufficient balance" means for this workflow: log it
     * and report failure via a boolean, instead of forcing every caller up
     * the chain to deal with the checked exception directly.</p>
     *
     * @param account the account to withdraw from
     * @param amount the amount requested
     * @return {@code true} if the withdrawal succeeded, {@code false} if the balance was insufficient
     */
    public boolean processWithdrawal(BankAccount account, double amount) {
        try {
            account.withdraw(amount);
            return true;
        } catch (InsufficientBalanceException e) {
            System.err.println("Withdrawal failed: " + e.getMessage());
            return false;
        }
    }
}