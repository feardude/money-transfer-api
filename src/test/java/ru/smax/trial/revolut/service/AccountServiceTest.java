package ru.smax.trial.revolut.service;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.smax.trial.revolut.model.ProcessAccountMoneyPayload.Action.DEPOSIT;
import static ru.smax.trial.revolut.model.ProcessAccountMoneyPayload.Action.WITHDRAW;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.ProcessAccountMoneyPayload;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.dao.AccountDao;

public class AccountServiceTest {
    private final AccountDao dao = mock(AccountDao.class);
    private final ConcurrentMap<Long, Lock> accountIdToLock = new ConcurrentHashMap<>();
    private final AccountService service = new AccountServiceImpl(dao, accountIdToLock);

    @Rule
    public final ExpectedException expectedException = none();

    @Test
    public void transferMoney_success() {
        when(dao.getAccount(1))
                .thenReturn(Account.builder().amount(ONE).build());

        final TransferMoneyPayload payload = new TransferMoneyPayload(1L, 2L, ONE);
        service.transferMoney(payload);

        verify(dao).getAccount(1);
        verify(dao).transferMoney(1, 2, ONE);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void transferMoney_insufficientFunds() {
        expectedException.expect(InsufficientFundsException.class);
        expectedException.expectMessage("Insufficient funds for [account-id=1]");

        when(dao.getAccount(1))
                .thenReturn(Account.builder().amount(ZERO).build());

        final TransferMoneyPayload payload = new TransferMoneyPayload(1L, 2L, ONE);
        service.transferMoney(payload);

        verify(dao).getAccount(1);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void processAccountMoney_deposit() {
        final ProcessAccountMoneyPayload payload = new ProcessAccountMoneyPayload(DEPOSIT, 1L, ONE);
        service.processAccountMoney(payload);
        verify(dao).depositMoney(1, ONE);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void processAccountMoney_withdraw_success() {
        when(dao.getAccount(1))
                .thenReturn(Account.builder().amount(ONE).build());

        final ProcessAccountMoneyPayload payload = new ProcessAccountMoneyPayload(WITHDRAW, 1L, ONE);
        service.processAccountMoney(payload);

        verify(dao).getAccount(1);
        verify(dao).withdrawMoney(1, ONE);
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void processAccountMoney_withdraw_insufficientFunds() {
        expectedException.expect(InsufficientFundsException.class);
        expectedException.expectMessage("Insufficient funds for [account-id=1]");

        when(dao.getAccount(1))
                .thenReturn(Account.builder().amount(ZERO).build());

        final ProcessAccountMoneyPayload payload = new ProcessAccountMoneyPayload(WITHDRAW, 1L, ONE);
        service.processAccountMoney(payload);

        verify(dao).getAccount(1);
        verifyNoMoreInteractions(dao);
    }
}
