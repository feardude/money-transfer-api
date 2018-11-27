package ru.smax.trial.revolut.exception;

public class MoneyTransferException extends Exception {
    private static final long serialVersionUID = 6049932025100987857L;

    public MoneyTransferException() {
        super();
    }

    MoneyTransferException(String message) {
        super(message);
    }
}
