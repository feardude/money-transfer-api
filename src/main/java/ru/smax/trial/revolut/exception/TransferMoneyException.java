package ru.smax.trial.revolut.exception;

public class TransferMoneyException extends RuntimeException {
    private static final long serialVersionUID = -3939675579335028647L;

    public TransferMoneyException() {
        super();
    }

    TransferMoneyException(String message) {
        super(message);
    }
}
