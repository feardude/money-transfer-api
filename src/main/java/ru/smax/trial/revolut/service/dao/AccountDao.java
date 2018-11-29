package ru.smax.trial.revolut.service.dao;

import ru.smax.trial.revolut.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    List<Account> getAccounts();

    Account getAccount(long id);

    void transferMoney(long fromAccountId, long toAccountId, BigDecimal amount);

    void withdrawMoney(long accountId, BigDecimal amount);

    void depositMoney(long accountId, BigDecimal amount);
}
