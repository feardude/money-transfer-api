package ru.smax.trial.revolut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.dao.AccountsDao;
import ru.smax.trial.revolut.exception.InsufficientFundsException;
import ru.smax.trial.revolut.model.TransferMoneyPayload;

import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.redirect;

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

        redirect.get("/", "/api");

        get("api",
                (request, response) -> {
                    final Map<String, String> map = new HashMap<>();
                    map.put("/accounts", "get");
                    map.put("/accounts/:id", "get");
                    map.put("/transfer", "post");
                    return toJson(map);
                }
        );

        get("/accounts",
                (request, response) -> toJson(accountsDao.getAccounts())
        );

        get("/accounts/:id",
                (request, response) -> toJson(accountsDao.getAccount(Long.valueOf(request.params("id"))))
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
                        log.info("Money were transferred successfully [from-account-id={}, to-account-id={}, amount={}]",
                                payload.getFromAccountId(), payload.getToAccountId(), payload.getAmount()
                        );

                        response.status(HTTP_OK);
                        return "";
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

    private String toJson(Object object) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}
