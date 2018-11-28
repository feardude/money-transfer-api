package ru.smax.trial.revolut.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.hsqldb.jdbc.JDBCDataSource;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.TransferWebService;
import ru.smax.trial.revolut.dao.AccountsDao;
import ru.smax.trial.revolut.dao.Sql2oAccountsDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class TransferServiceModule extends AbstractModule {
    private static final String DB_URL = "jdbc:hsqldb:mem:localhost";

    @Override
    protected void configure() {
        bind(TransferWebService.class).in(Singleton.class);

        bind(AccountsDao.class).to(Sql2oAccountsDao.class);
    }

    @Provides
    @Singleton
    private Sql2o provideSql2o() {
        return new Sql2o(createDataSource());
    }

    private DataSource createDataSource() {
        try {
            populateDatabase();
        } catch (SQLException e) {
            log.error("Could not populate database", e);
            System.exit(1);
        }

        final JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(DB_URL);
        return dataSource;
    }

    private static void populateDatabase() throws SQLException {
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
}