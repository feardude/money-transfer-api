package ru.smax.trial.revolut.service.dao;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Sql2o;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static org.junit.Assert.assertEquals;

public class AccountDaoIT {
    private static final String DB_URL = "jdbc:hsqldb:mem:testinmemdb";
    private AccountDao accountDao;

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
        accountDao = new Sql2oAccountsDao(sql2o);
    }

    private void populateDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, "SA", "");
             Statement statement = connection.createStatement()) {

            statement.execute(
                    "create table if not exists accounts (" +
                            "  id int primary key," +
                            "  idReal varchar(128) unique not null," +
                            "  amount decimal(10,6) default 0" +
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
    public void transferMoney() {
        accountDao.transferMoney(1, 2, withScale(0.5d));

        final BigDecimal expected1 = withScale(999.5d);
        final BigDecimal actual1 = accountDao.getAccount(1L).getAmount();
        assertEquals(expected1, actual1);

        final BigDecimal expected2 = withScale(1000.5d);
        final BigDecimal actual2 = accountDao.getAccount(2L).getAmount();
        assertEquals(expected2, actual2);
    }

    @Test
    public void depositMoney() {
        accountDao.depositMoney(1, withScale(1d));

        final BigDecimal expected1 = withScale(1001d);
        final BigDecimal actual1 = accountDao.getAccount(1L).getAmount();
        assertEquals(expected1, actual1);
    }

    @Test
    public void withdrawMoney() {
        accountDao.withdrawMoney(1, withScale(1d));

        final BigDecimal expected1 = withScale(999d);
        final BigDecimal actual1 = accountDao.getAccount(1L).getAmount();
        assertEquals(expected1, actual1);
    }


    private static BigDecimal withScale(double value) {
        return BigDecimal.valueOf(value)
                .setScale(6, ROUND_HALF_UP);
    }
}
