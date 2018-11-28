package ru.smax.trial.revolut.service;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.exception.InsufficientFundsMoneyException;
import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.dao.AccountDao;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;

@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;

    @Inject
    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public List<Account> getAccounts() {
        return accountDao.getAccounts();
    }

    @Override
    public Account getAccount(long id) {
        return accountDao.getAccount(id);
    }

    @Override
    public void transferMoney(TransferMoneyPayload payload) {
        log.info("Requested money transfer [{}]", payload.toString());
        
        verifyFundsSufficiency(payload.getFromAccountId(), payload.getAmount());
        accountDao.transferMoney(payload.getFromAccountId(), payload.getToAccountId(), payload.getAmount());
        
        log.info("Money were transferred successfully [{}]", payload.toString());
    }

    private void verifyFundsSufficiency(long fromAccountId, BigDecimal amountToWithdraw) {
        final BigDecimal currentAmount = getAccount(fromAccountId).getAmount();

        if (currentAmount.compareTo(amountToWithdraw) < 0) {
            throw new InsufficientFundsMoneyException(format("Insufficient funds for [account-id=%s]", fromAccountId));
        }
    }
}
