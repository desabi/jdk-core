package com.desabisc.exceptions.achecked.bank;

/**
 * Signals that a withdrawal was requested for an amount greater than the
 * current balance of a {@link BankAccount}.
 *
 * <p>This is a <strong>checked</strong> exception: an insufficient balance
 * is an entirely expected, recoverable situation in the normal operation of
 * a banking application — the caller can offer to retry with a smaller
 * amount, or reject the transaction gracefully. It is not a bug in the
 * program, so it is not modeled as a {@link RuntimeException}.</p>
 *
 * <p>Callers should expect this exception whenever
 * {@link BankAccount#withdraw(double)} is invoked with an amount that
 * exceeds the account's current balance.</p>
 *
 * @see BankAccount#withdraw(double)
 */
public class InsufficientBalanceException extends Exception {

    /**
     * Creates a new {@code InsufficientBalanceException} with a message
     * describing the shortfall.
     *
     * @param message a human-readable explanation of the failure
     */
    public InsufficientBalanceException(String message) {
        super(message);
    }
}