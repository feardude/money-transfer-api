package ru.smax.trial.revolut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.dao.AccountsDao;
import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.TransferMoneyPayload;

import java.util.List;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.stream.Collectors.toList;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;

@Slf4j
public class TransferWebService {
    private final AccountsDao accountsDao;

    @Inject
    public TransferWebService(AccountsDao accountsDao) {
        this.accountsDao = accountsDao;
    }

    void run() {
        before("/*",
                (request, response) -> log.info("{}: {}", request.requestMethod(), request.uri())
        );

        after("/*",
                (request, response) -> log.info("{}: {}, response: status={}, body=[{}]",
                        request.requestMethod(), request.uri(),
                        response.status(), response.body()
                )
        );

        get("/",
                (request, response) ->
                        "Money transfer trial for Revolut" +
                                "<br>" +
                                "<a href='/accounts'>Accounts</a>"
        );

        get("/accounts",
                (request, response) -> {
                    final List<String> accountLinks = accountsDao.getAccounts().stream()
                            .map(account -> format(
                                    "<a href='/accounts/%s'>%s</a>",
                                    account.getId(),
                                    account.toString()
                            ))
                            .collect(toList());
                    return format("<a href='/'>Home</a> " +
                                    "<br>" +
                                    "%s",
                            accountLinks
                    );
                }
        );

        get("/accounts/:id",
                (request, response) -> accountsDao.getAccount(Long.valueOf(request.params("id"))).toString()
        );

        post("/transfer",
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
}
