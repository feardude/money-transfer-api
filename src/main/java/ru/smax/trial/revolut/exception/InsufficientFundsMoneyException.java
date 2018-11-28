package ru.smax.trial.revolut.exception;

public class InsufficientFundsMoneyException extends TransferMoneyException {
    private static final long serialVersionUID = -4788927489729880920L;

    public InsufficientFundsMoneyException(String message) {
        super(message);
    }
}
