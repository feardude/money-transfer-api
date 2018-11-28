package ru.smax.trial.revolut.service;

import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.TransferMoneyPayload;

import java.util.List;

public interface AccountService {
    List<Account> getAccounts();

    Account getAccount(long id);

    void transferMoney(TransferMoneyPayload payload);
}
