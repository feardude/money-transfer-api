package ru.smax.trial.revolut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hsqldb.jdbc.JDBCDataSource;
import org.sql2o.Sql2o;
import ru.smax.trial.revolut.dao.AccountsDao;
import ru.smax.trial.revolut.dao.Sql2oAccountsDao;
import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.TransferMoneyPayload;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.stream.Collectors.toList;
import static spark.Spark.get;
import static spark.Spark.post;

@Slf4j
public class HelloSpark {
    private static final String DB_URL = "jdbc:hsqldb:mem:localhost";

    public static void main(String[] args) {
        final Sql2o sql2o = new Sql2o(createDataSource());
        final AccountsDao accountsDao = new Sql2oAccountsDao(sql2o);

        get(
                "/",
                (request, response) ->
                        "Money transfer trial for Revolut" +
                                "<br>" +
                                "<a href='/accounts'>Accounts</a>"
        );

        get(
                "/accounts",
                (request, response) -> {
                    final List<String> accountLinks = accountsDao.getAccounts().stream()
                            .map(account -> format(
                                    "<a href='/accounts/%s'>Account (id-real=%s)</a>",
                                    account.getId(),
                                    account.getIdReal()
                            ))
                            .collect(toList());
                    return format("<a href='/'>Home</a> " +
                                    "<br>" +
                                    "%s",
                            accountLinks
                    );
                }
        );

        get(
                "/accounts/:id",
                (request, response) -> accountsDao.getAccount(Long.valueOf(request.params("id"))).toString()
        );

        post(
                "/transfer",
                (request, response) -> {
                    try {
                        final ObjectMapper mapper = new ObjectMapper();
                        final TransferMoneyPayload payload = mapper.readValue(request.body(), TransferMoneyPayload.class);

                        log.info("Requested transfer [from-account-id={}, to-account-id={}, amount={}]",
                                payload.getFromAccountId(), payload.getToAccountId(), payload.getAmount()
                        );

                        accountsDao.transferMoney(payload.getFromAccountId(), payload.getToAccountId(), payload.getAmount());
                        response.status(HTTP_OK);
                        return format("Money were transferred successfully [from-account-id=%s, to-account-id=%s, amount=%s]",
                                payload.getFromAccountId(), payload.getToAccountId(), payload.getAmount()
                        );
                    } catch (JsonProcessingException e) {
                        log.error("Invalid request payload", e);
                        response.status(HTTP_BAD_REQUEST);
                        return e.getMessage();
                    } catch (InsufficientFundsException e) {
                        log.error(e.toString());
                        response.status(HTTP_BAD_REQUEST);
                        return e.getMessage();
                    }
                }
        );
    }

    private static DataSource createDataSource() {
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
