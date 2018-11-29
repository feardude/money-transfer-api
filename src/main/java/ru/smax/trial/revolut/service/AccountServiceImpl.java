package ru.smax.trial.revolut.service;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.ProcessAccountMoneyPayload;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.dao.AccountDao;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;
import static ru.smax.trial.revolut.model.ProcessAccountMoneyPayload.Action.DEPOSIT;
import static ru.smax.trial.revolut.model.ProcessAccountMoneyPayload.Action.WITHDRAW;

@Slf4j
public class AccountServiceImpl implements AccountService {
    private static final Object LOCK = new Object();
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

        synchronized (LOCK) {
            verifyFundsSufficiency(payload.getFromAccountId(), payload.getAmount());
            accountDao.transferMoney(payload.getFromAccountId(), payload.getToAccountId(), payload.getAmount());
        }

        log.info("Money were transferred successfully [{}]", payload.toString());
    }

    @Override
    public void processAccountMoney(ProcessAccountMoneyPayload payload) {
        log.info("Requested account money processing [{}]", payload.toString());

        synchronized (LOCK) {
            if (DEPOSIT == payload.getAction()) {
                accountDao.depositMoney(payload.getAccountId(), payload.getAmount());
            }

            if (WITHDRAW == payload.getAction()) {
                verifyFundsSufficiency(payload.getAccountId(), payload.getAmount());
                accountDao.withdrawMoney(payload.getAccountId(), payload.getAmount());
            }
        }

        log.info("Account money were processed successfully [{}]", payload.toString());
    }

    private void verifyFundsSufficiency(long fromAccountId, BigDecimal amountToWithdraw) {
        final BigDecimal currentAmount = getAccount(fromAccountId).getAmount();

        if (currentAmount.compareTo(amountToWithdraw) < 0) {
            final String errorMessage = format("Insufficient funds for [account-id=%s]", fromAccountId);
            log.error(errorMessage);
            throw new InsufficientFundsException(errorMessage);
        }
    }
}
