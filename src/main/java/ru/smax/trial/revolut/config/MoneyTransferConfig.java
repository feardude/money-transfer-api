package ru.smax.trial.revolut.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.hsqldb.jdbc.JDBCDataSource;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.MoneyTransferWebService;
import ru.smax.trial.revolut.service.AccountService;
import ru.smax.trial.revolut.service.AccountServiceImpl;
import ru.smax.trial.revolut.service.dao.AccountDao;
import ru.smax.trial.revolut.service.dao.Sql2oAccountsDao;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class MoneyTransferConfig extends AbstractModule {
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;

    public MoneyTransferConfig() {
        this.dbUrl = ServiceProperties.getDbUrl();
        this.dbUsername = ServiceProperties.getDbUsername();
        this.dbPassword = ServiceProperties.getDbPassword();
    }

    @Override
    protected void configure() {
        bind(MoneyTransferWebService.class).in(Singleton.class);

        bind(AccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
        bind(AccountDao.class).to(Sql2oAccountsDao.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    private Sql2o provideSql2o() {
        return new Sql2o(provideDataSource());
    }

    @Provides
    @Singleton
    private DataSource provideDataSource() {
        populateDatabase();

        final JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(dbUrl);
        return dataSource;
    }

    private void populateDatabase() {
        // Only for trial demo purposes
        // Real app should not create initial data on start

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
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
        } catch (SQLException e) {
            log.error("Could not populate database", e);
            System.exit(1);
        }
    }
}
