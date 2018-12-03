package ru.smax.trial.revolut;

import com.google.inject.Guice;
import ru.smax.trial.revolut.config.MoneyTransferConfig;

public class MoneyTransferStarter {
    public static void main(String[] args) {
        Guice.createInjector(new MoneyTransferConfig())
                .getInstance(MoneyTransferController.class)
                .run();
    }
}
