package ru.smax.trial.revolut;

import com.google.inject.Guice;
import lombok.extern.slf4j.Slf4j;
import ru.smax.trial.revolut.config.TransferServiceModule;

@Slf4j
public class Application {
    public static void main(String[] args) {
        Guice.createInjector(new TransferServiceModule())
                .getInstance(TransferWebService.class)
                .run();
    }
}
