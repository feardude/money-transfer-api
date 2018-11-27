package ru.smax.trial.revolut.dao;

import lombok.extern.slf4j.Slf4j;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;

@Slf4j
public class AccountsDaoTest {
    private static final String DB_URL = "jdbc:hsqldb:mem:testinmemdb";
    private AccountsDao accountsDao;

    @Rule
    public final ExpectedException expectedException = none();

    @AfterClass
    public static void destroy() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, "SA", "");
             Statement statement = connection.createStatement()) {

            statement.execute("drop table accounts");
            connection.commit();
        }
    }

    @Before
    public void initDao() throws SQLException {
        populateDatabase();

        final JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(DB_URL);

        final Sql2o sql2o = new Sql2o(dataSource);
        accountsDao = new Sql2oAccountsDao(sql2o);
    }

    private void populateDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, "SA", "");
             Statement statement = connection.createStatement()) {

            statement.execute(
                    "create table if not exists accounts (" +
                            "  id int primary key," +
                            "  idReal varchar(128) unique not null," +
                            "  amount decimal default 0" +
                            ")"
            );
            connection.commit();

            statement.executeUpdate("delete from accounts");
            statement.executeUpdate("insert into accounts values (1, '1', 1000)");
            statement.executeUpdate("insert into accounts values (2, '2', 1000)");
            connection.commit();
        }
    }


    @Test
    public void transferMoney_success() {
        accountsDao.transferMoney(1, 2, BigDecimal.TEN);

        final int expected1 = 990;
        final int actual1 = accountsDao.getAccount(1L).getAmount().intValue();
        assertEquals(expected1, actual1);

        final int expected2 = 1010;
        final int actual2 = accountsDao.getAccount(2L).getAmount().intValue();
        assertEquals(expected2, actual2);
    }

    @Test
    public void transferMoney_insufficientFunds() {
        expectedException.expect(InsufficientFundsException.class);
        expectedException.expectMessage("Insufficient funds for [account-id=1]");
        accountsDao.transferMoney(1, 2, BigDecimal.valueOf(2000));
    }
}
