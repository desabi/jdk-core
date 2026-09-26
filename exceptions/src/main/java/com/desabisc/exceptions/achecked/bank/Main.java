package com.desabisc.exceptions.achecked.bank;

/**
 * Entry point demonstrating the full lifecycle of both custom checked
 * exceptions: propagation via {@code throws} and handling via
 * {@code try-catch}, plus a contrast with an unchecked exception.
 */
public class Main {

    public static void main(String[] args) {

        AccountService service = new AccountService();

        // --- Demonstration 1: a checked exception propagated up to main ---
        try {
            BankAccount underageAttempt = service.openAccount("Underage Applicant", "16", 100.0);
            System.out.println("Account opened for: " + underageAttempt.getOwnerName());
        } catch (InvalidAgeException e) {
            // This catch exists because openAccount() DECLARES the checked
            // exception instead of handling it — main is where it is
            // finally caught.
            System.err.println("Onboarding rejected: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Unchecked exception, caught here only to contrast with the
            // checked InvalidAgeException above — not required by the compiler.
            System.err.println("Onboarding rejected: age was not a valid number.");
        } finally {
            System.out.println("Finished processing underage applicant.\n");
        }

        // --- Demonstration 2: a checked exception handled deeper in the call chain ---
        try {
            BankAccount account = service.openAccount("Abi", "30", 100.0);
            System.out.println("Account opened for: " + account.getOwnerName()
                    + " with balance " + account.getBalance());

            boolean firstWithdrawal = service.processWithdrawal(account, 40.0);
            System.out.println("Withdrawal of 40.0 succeeded? " + firstWithdrawal
                    + " | balance now: " + account.getBalance());

            // This withdrawal exceeds the remaining balance. Notice that
            // InsufficientBalanceException is NOT caught here in main —
            // it was already handled inside AccountService.processWithdrawal,
            // which is why this call compiles without a throws clause or a catch.
            boolean secondWithdrawal = service.processWithdrawal(account, 1000.0);
            System.out.println("Withdrawal of 1000.0 succeeded? " + secondWithdrawal
                    + " | balance now: " + account.getBalance());

        } catch (InvalidAgeException e) {
            System.err.println("Onboarding rejected: " + e.getMessage());
        } finally {
            System.out.println("Finished processing valid applicant.");
        }
    }
}