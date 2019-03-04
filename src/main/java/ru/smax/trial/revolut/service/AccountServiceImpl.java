package ru.smax.trial.revolut.service;

import static java.lang.String.format;
import static ru.smax.trial.revolut.model.ProcessAccountMoneyPayload.Action.DEPOSIT;
import static ru.smax.trial.revolut.model.ProcessAccountMoneyPayload.Action.WITHDRAW;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.ProcessAccountMoneyPayload;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.dao.AccountDao;

@Slf4j
public class AccountServiceImpl implements AccountService {
    private static final Object LOCK = new Object();

    private final ConcurrentMap<Long, Lock> accountIdToLock;
    private final AccountDao accountDao;

    @Inject
    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
        this.accountIdToLock = new ConcurrentHashMap<>();
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

        final Long fromAccountId = payload.getFromAccountId();
        final Lock fromAccountLock = getLock(fromAccountId);

        try {
            fromAccountLock.lock();
            verifyFundsSufficiency(fromAccountId, payload.getAmount());

            final Long toAccountId = payload.getToAccountId();
            final Lock toAccountLock = getLock(toAccountId);

            try {
                toAccountLock.lock();
                accountDao.transferMoney(fromAccountId, toAccountId, payload.getAmount());
            }
            finally {
                toAccountLock.unlock();
            }
        }
        finally {
            fromAccountLock.unlock();
        }

        log.info("Money was transferred successfully [{}]", payload.toString());
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

        log.info("Account money was processed successfully [{}]", payload.toString());
    }

    private void verifyFundsSufficiency(long fromAccountId, BigDecimal amountToWithdraw) {
        final BigDecimal currentAmount = getAccount(fromAccountId).getAmount();

        if (currentAmount.compareTo(amountToWithdraw) < 0) {
            final String errorMessage = format("Insufficient funds for [account-id=%s]", fromAccountId);
            log.error(errorMessage);
            throw new InsufficientFundsException(errorMessage);
        }
    }

    private Lock getLock(Long accountId) {
        return accountIdToLock.computeIfAbsent(
                accountId,
                id -> new ReentrantLock(true)
        );
    }
}
