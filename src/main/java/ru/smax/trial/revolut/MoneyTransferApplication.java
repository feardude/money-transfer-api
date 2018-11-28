package ru.smax.trial.revolut;

import com.google.inject.Guice;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.config.MoneyTransferModule;

@Slf4j
public class MoneyTransferApplication {
    public static void main(String[] args) {
        Guice.createInjector(new MoneyTransferModule())
                .getInstance(MoneyTransferWebService.class)
                .run();
    }
}
