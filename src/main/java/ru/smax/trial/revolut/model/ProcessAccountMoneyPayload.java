package ru.smax.trial.revolut.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class ProcessAccountMoneyPayload {
    @NonNull
    private final Action action;

    @NonNull
    private final Long accountId;

    @NonNull
    private final BigDecimal amount;

    public enum Action {
        DEPOSIT,
        WITHDRAW
    }
}
