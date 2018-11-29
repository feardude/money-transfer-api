package ru.smax.trial.revolut.service.dao;

import com.google.inject.Inject;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.model.Account;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.String.format;

public class Sql2oAccountsDao implements AccountDao {
    private static final String PARAM_ACCOUNT_ID = "accountId";
    private static final String PARAM_AMOUNT = "amount";

    private final Sql2o sql2o;
    private final String withdrawMoney;
    private final String depositMoney;

    @Inject
    public Sql2oAccountsDao(Sql2o sql2o) {
        this.sql2o = sql2o;

        withdrawMoney = format("update accounts " +
                "set amount = amount - :%s " +
                "where id = :%s",
                PARAM_AMOUNT, PARAM_ACCOUNT_ID);

        depositMoney = format("update accounts " +
                "set amount = amount + :%s " +
                "where id = :%s",
                PARAM_AMOUNT, PARAM_ACCOUNT_ID);
    }

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
    public void transferMoney(long fromAccountId, long toAccountId, BigDecimal amount) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery(withdrawMoney)
                    .addParameter(PARAM_ACCOUNT_ID, fromAccountId)
                    .addParameter(PARAM_AMOUNT, amount)
                    .executeUpdate();

            conn.createQuery(depositMoney)
                    .addParameter(PARAM_ACCOUNT_ID, toAccountId)
                    .addParameter(PARAM_AMOUNT, amount)
                    .executeUpdate();

            conn.commit();
        }
    }

    @Override
    public void withdrawMoney(long accountId, BigDecimal amount) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery(withdrawMoney)
                    .addParameter(PARAM_ACCOUNT_ID, accountId)
                    .addParameter(PARAM_AMOUNT, amount)
                    .executeUpdate();
            conn.commit();
        }
    }

    @Override
    public void depositMoney(long accountId, BigDecimal amount) {
        try (Connection conn = sql2o.beginTransaction()) {
            conn.createQuery(depositMoney)
                    .addParameter(PARAM_ACCOUNT_ID, accountId)
                    .addParameter(PARAM_AMOUNT, amount)
                    .executeUpdate();
            conn.commit();
        }
    }
}
