package ru.smax.trial.revolut.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.Account;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;

@AllArgsConstructor
@Slf4j
public class Sql2oAccountsDao implements AccountsDao {
    private final Sql2o sql2o;

    @Override
    public List<Account> getAccounts() {
        try (Connection conn = sql2o.open()) {
            final String selectAccounts = "select * from accounts";

            return conn.createQuery(selectAccounts)
                    .executeAndFetch(Account.class);
        }
    }

    @Override
    public Account getAccount(long id) {
        try (Connection conn = sql2o.open()) {
            final String selectAccount = "select * from accounts where id = :id";

            return conn.createQuery(selectAccount)
                    .addParameter("id", id)
                    .executeAndFetchFirst(Account.class);
        }
    }

    @Override
    public void transferMoney(long fromAccountId, long toAccountId, BigDecimal amount) throws InsufficientFundsException {
        try (Connection conn = sql2o.beginTransaction()) {
            final String withdrawMoney = "update accounts " +
                    "set amount = amount - :amount " +
                    "where id = :fromAccountId";

            final String addMoney = "update accounts " +
                    "set amount = amount + :amount " +
                    "where id = :toAccountId";

            validateSufficientFunds(fromAccountId, amount);

            conn.createQuery(withdrawMoney)
                    .addParameter("amount", amount)
                    .addParameter("fromAccountId", fromAccountId)
                    .executeUpdate();

            conn.createQuery(addMoney)
                    .addParameter("amount", amount)
                    .addParameter("toAccountId", toAccountId)
                    .executeUpdate();

            conn.commit();
        }
    }

    private void validateSufficientFunds(long fromAccountId, BigDecimal amountToWithdraw) throws InsufficientFundsException {
        final BigDecimal currentAmount = getAccount(fromAccountId).getAmount();

        if (currentAmount.compareTo(amountToWithdraw) < 0) {
            throw new InsufficientFundsException(format("Insufficient funds for [account-id=%s]", fromAccountId));
        }
    }
}
