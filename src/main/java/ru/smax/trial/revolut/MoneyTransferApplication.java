package ru.smax.trial.revolut;

import com.google.inject.Guice;
import ru.smax.trial.revolut.config.MoneyTransferConfig;

public class MoneyTransferApplication {
    public static void main(String[] args) {
        Guice.createInjector(new MoneyTransferConfig())
                .getInstance(MoneyTransferWebService.class)
                .run();
    }
}
