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
public class TransferMoneyPayload {
    private final long fromAccountId;
    private final long toAccountId;
    private final BigDecimal amount;
}
