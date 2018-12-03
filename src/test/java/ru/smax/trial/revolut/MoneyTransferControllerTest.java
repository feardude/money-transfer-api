package ru.smax.trial.revolut;

import com.despegar.http.client.GetMethod;
import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.http.client.PostMethod;
import com.despegar.sparkjava.test.SparkServer;
import org.junit.ClassRule;
import org.junit.Test;
import ru.smax.trial.revolut.model.Account;
import ru.smax.trial.revolut.model.ProcessAccountMoneyPayload;
import ru.smax.trial.revolut.model.TransferMoneyPayload;
import ru.smax.trial.revolut.service.AccountService;
import spark.servlet.SparkApplication;

import static com.feardude.commons.ResourceUtils.readJson;
import static com.feardude.commons.ResourceUtils.readJsonList;
import static com.feardude.commons.ResourceUtils.readResourceAsString;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class MoneyTransferControllerTest {
    private static final AccountService accountService = mock(AccountService.class);

    public static class MoneyTransferControllerTestSparkApplication implements SparkApplication {
        @Override
        public void init() {
            new MoneyTransferController(accountService, 4567).run();
        }
    }

    @ClassRule
    public static SparkServer<MoneyTransferControllerTestSparkApplication> testServer =
            new SparkServer<>(MoneyTransferControllerTestSparkApplication.class, 4567);

    @Test
    public void root() throws HttpClientException {
        final GetMethod getRoot = testServer.get("/", true);
        final HttpResponse response = testServer.execute(getRoot);

        final String expectedBody = readResourceAsString("controller/response_api.json");
        final String actualBody = new String(response.body());

        assertEquals(HTTP_OK, response.code());
        assertEquals(expectedBody, actualBody);
        verifyMock();
    }

    @Test
    public void api() throws HttpClientException {
        final GetMethod getApi = testServer.get("/api", false);
        final HttpResponse response = testServer.execute(getApi);

        final String expectedBody = readResourceAsString("controller/response_api.json");
        final String actualBody = new String(response.body());

        assertEquals(HTTP_OK, response.code());
        assertEquals(expectedBody, actualBody);
        verifyMock();
    }

    @Test
    public void accounts() throws HttpClientException {
        when(accountService.getAccounts())
                .thenReturn(readJsonList("controller/accounts/accounts.json", Account.class));

        final GetMethod getAccounts = testServer.get("/api/accounts", false);
        final HttpResponse response = testServer.execute(getAccounts);

        final String expectedBody = readResourceAsString("controller/accounts/response_accounts.json");
        final String actualBody = new String(response.body());

        verify(accountService).getAccounts();
        verifyMock();
        assertEquals(HTTP_OK, response.code());
        assertEquals(expectedBody, actualBody);
    }

    @Test
    public void account_info() throws HttpClientException {
        when(accountService.getAccount(1))
                .thenReturn(readJson("controller/accounts/account_1.json", Account.class));

        final GetMethod getAccount = testServer.get("/api/accounts/1", false);
        final HttpResponse response = testServer.execute(getAccount);

        final String expectedBody = readResourceAsString("controller/accounts/response_account_1.json");
        final String actualBody = new String(response.body());

        verify(accountService).getAccount(1);
        verifyMock();
        assertEquals(HTTP_OK, response.code());
        assertEquals(expectedBody, actualBody);
    }

    @Test
    public void account_deposit() throws HttpClientException {
        when(accountService.getAccount(1))
                .thenReturn(readJson("controller/accounts/deposit/account.json", Account.class));

        final String payload = readResourceAsString("controller/accounts/deposit/payload.json");
        final PostMethod postDeposit = testServer.post("/api/accounts/1", payload, false);
        final HttpResponse response = testServer.execute(postDeposit);

        final String expectedBody = readResourceAsString("controller/accounts/deposit/account.json");
        final String actualBody = new String(response.body());

        verify(accountService).getAccount(1);
        verify(accountService).processAccountMoney(readJson("controller/accounts/deposit/payload.json", ProcessAccountMoneyPayload.class));
        verifyMock();
        assertEquals(HTTP_OK, response.code());
        assertEquals(expectedBody, actualBody);
    }

    @Test
    public void account_withdraw_success() throws HttpClientException {
        when(accountService.getAccount(1))
                .thenReturn(readJson("controller/accounts/withdraw/account.json", Account.class));

        final String payload = readResourceAsString("controller/accounts/withdraw/payload.json");
        final PostMethod postWithdraw = testServer.post("/api/accounts/1", payload, false);
        final HttpResponse response = testServer.execute(postWithdraw);

        final String expectedBody = readResourceAsString("controller/accounts/withdraw/account.json");
        final String actualBody = new String(response.body());

        verify(accountService).getAccount(1);
        verify(accountService).processAccountMoney(readJson("controller/accounts/withdraw/payload.json", ProcessAccountMoneyPayload.class));
        verifyMock();
        assertEquals(HTTP_OK, response.code());
        assertEquals(expectedBody, actualBody);
    }

    @Test
    public void transfer_success() throws HttpClientException {
        final String payload = readResourceAsString("controller/transfer/payload.json");
        final PostMethod postWithdraw = testServer.post("/api/transfer", payload, false);
        final HttpResponse response = testServer.execute(postWithdraw);

        verify(accountService).transferMoney(readJson("controller/transfer/payload.json", TransferMoneyPayload.class));
        verifyMock();
        assertEquals(HTTP_OK, response.code());
    }

    private void verifyMock() {
        verifyNoMoreInteractions(accountService);
        clearInvocations(accountService);
    }
}
