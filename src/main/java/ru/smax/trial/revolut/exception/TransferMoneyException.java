package ru.smax.trial.revolut.exception;

public class TransferMoneyException extends RuntimeException {
    private static final long serialVersionUID = -9147175530859695482L;

    TransferMoneyException(String message) {
        super(message);
    }
}
