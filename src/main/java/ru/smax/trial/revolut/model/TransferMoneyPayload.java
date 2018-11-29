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
public class TransferMoneyPayload {
    @NonNull
    private final Long fromAccountId;

    @NonNull
    private final Long toAccountId;

    @NonNull
    private final BigDecimal amount;
}
