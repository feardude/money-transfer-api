package ru.smax.trial.revolut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.exception.TransferMoneyException;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.AccountService;

import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.redirect;

@Slf4j
public class MoneyTransferWebService {
    private final AccountService accountService;

    @Inject
    public MoneyTransferWebService(AccountService accountService) {
        this.accountService = accountService;
    }

    void run() {
        before("/*",
                (request, response) -> log.info("{}: {}", request.requestMethod(), request.uri())
        );

        after("/*",
                (request, response) -> log.info("{}: {}, response status = {}",
                        request.requestMethod(), request.uri(),
                        response.status(), response.body()
                )
        );

        redirect.get("/", "/api");

        exceptionHandling();

        get("/api",
                (request, response) -> {
                    final Map<String, String> map = new HashMap<>();
                    map.put("/accounts", "get");
                    map.put("/accounts/:id", "get");
                    map.put("/transfer", "post");
                    return toJson(map);
                }
        );

        get("/api/accounts",
                (request, response) -> toJson(accountService.getAccounts())
        );

        get("/api/accounts/:id",
                (request, response) -> toJson(accountService.getAccount(Long.valueOf(request.params("id"))))
        );

        post("/api/transfer",
                (request, response) -> {
                    final ObjectMapper mapper = new ObjectMapper();
                    final TransferMoneyPayload payload = mapper.readValue(request.body(), TransferMoneyPayload.class);
                    accountService.transferMoney(payload);
                    response.status(HTTP_OK);
                    return "";
                }
        );
    }

    private void exceptionHandling() {
        exception(TransferMoneyException.class,
                (exception, request, response) -> {
                    log.error(exception.getMessage(), exception);
                    response.status(HTTP_BAD_REQUEST);
                    response.body(exception.getMessage());
                }
        );

        exception(JsonProcessingException.class,
                (exception, request, response) -> {
                    log.error("Invalid request payload", exception);
                    response.status(HTTP_BAD_REQUEST);
                    response.body(exception.getMessage());
                }
        );
    }

    private String toJson(Object object) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
