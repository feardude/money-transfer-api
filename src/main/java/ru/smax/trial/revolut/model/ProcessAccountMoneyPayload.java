package ru.smax.trial.revolut.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ProcessAccountMoneyPayload {
    private final Action action;
    private final long accountId;
    private final BigDecimal amount;

    public enum Action {
        DEPOSIT,
        WITHDRAW
    }
}
