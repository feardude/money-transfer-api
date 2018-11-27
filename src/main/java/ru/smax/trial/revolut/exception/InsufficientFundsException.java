package ru.smax.trial.revolut.exception;

public class InsufficientFundsException extends MoneyTransferException {
    private static final long serialVersionUID = -4788927489729880920L;

    public InsufficientFundsException(String message) {
        super(message);
    }
}
