package ru.smax.trial.revolut.dao;

import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountsDao {
    List<Account> getAccounts();

    Account getAccount(long id);

    void transferMoney(long fromAccountId, long toAccountId, BigDecimal amount) throws InsufficientFundsException;
}
