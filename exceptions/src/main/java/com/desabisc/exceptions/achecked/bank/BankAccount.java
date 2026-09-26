package com.desabisc.exceptions.achecked.bank;

/**
 * A minimal bank account holding a balance and supporting deposits and
 * checked-exception-governed withdrawals.
 */
public class BankAccount {

    private final String ownerName;
    private double balance;

    /**
     * Creates a new account for the given owner with an initial deposit.
     *
     * @param ownerName the account holder's name
     * @param initialDeposit the opening balance; must not be negative
     */
    public BankAccount(String ownerName, double initialDeposit) {
        this.ownerName = ownerName;
        this.balance = initialDeposit;
    }

    /**
     * Withdraws {@code amount} from the account.
     *
     * <p>This is an example of <strong>catching</strong> being the caller's
     * responsibility: this method declares the checked exception rather
     * than swallowing it, because {@code BankAccount} itself has no
     * meaningful way to "recover" from an insufficient balance — only the
     * calling workflow (see {@link AccountService#processWithdrawal}) knows
     * what to do (retry, notify the user, cancel the transaction).</p>
     *
     * @param amount the amount to withdraw; must be positive
     * @throws InsufficientBalanceException if {@code amount} exceeds the current balance
     */
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > balance) {
            throw new InsufficientBalanceException(
                    "Cannot withdraw " + amount + "; current balance is only " + balance + ".");
        }
        balance -= amount;
    }

    /** @return the account holder's name */
    public String getOwnerName() {
        return ownerName;
    }

    /** @return the current balance */
    public double getBalance() {
        return balance;
    }
}