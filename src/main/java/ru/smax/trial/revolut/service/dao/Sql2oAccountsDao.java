package ru.smax.trial.revolut.service.dao;

import com.google.inject.Inject;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.model.Account;

import java.math.BigDecimal;
import java.util.List;

public class Sql2oAccountsDao implements AccountDao {
    private final Sql2o sql2o;

    @Inject
    public Sql2oAccountsDao(Sql2o sql2o) {
        this.sql2o = sql2o;
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
            final String withdrawMoney = "update accounts " +
                    "set amount = amount - :amount " +
                    "where id = :fromAccountId";

            final String addMoney = "update accounts " +
                    "set amount = amount + :amount " +
                    "where id = :toAccountId";

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
}
