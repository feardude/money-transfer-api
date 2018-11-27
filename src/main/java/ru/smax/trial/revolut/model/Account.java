package ru.smax.trial.revolut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class Account {
    private final long id;
    private final String idReal;
    private final BigDecimal amount;
}
