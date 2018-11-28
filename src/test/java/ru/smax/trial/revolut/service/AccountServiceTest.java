package ru.smax.trial.revolut.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.dao.AccountDao;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AccountServiceTest {
    private final AccountDao accountDao = mock(AccountDao.class);
    private final AccountService accountService = new AccountServiceImpl(accountDao);

    @Rule
    public final ExpectedException expectedException = none();

    @Test
    public void transferMoney_success() {
        when(accountDao.getAccount(1))
                .thenReturn(Account.builder().amount(ONE).build());

        final TransferMoneyPayload payload = new TransferMoneyPayload(1, 2, ONE);
        accountService.transferMoney(payload);

        verify(accountDao).getAccount(1);
        verify(accountDao).transferMoney(1, 2, ONE);
        verifyNoMoreInteractions(accountDao);
    }

    @Test
    public void transferMoney_insufficientFunds() {
        when(accountDao.getAccount(1))
                .thenReturn(Account.builder().amount(ZERO).build());

        expectedException.expect(InsufficientFundsException.class);
        expectedException.expectMessage("Insufficient funds for [account-id=1]");

        final TransferMoneyPayload payload = new TransferMoneyPayload(1, 2, ONE);
        accountService.transferMoney(payload);

        verify(accountDao).getAccount(1);
        verifyNoMoreInteractions(accountDao);
    }
}
