package com.noeliaiglesias.springbootreactor.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.logging.Logger;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(SpringBootReactorApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Flux<String> nombres = Flux.just("Noelia", "Pedro", "Juan")
                .doOnNext(el -> {
                    if (el.isEmpty()) {
                        throw new RuntimeException("El nombre no puede ser vacío");
                    }
                    System.out.println("Emitiendo: " + el);
                });

        nombres.subscribe(log::info, error -> log.severe("Error: " + error.getMessage()), new Runnable() {
            @Override
            public void run() {
                log.info("Terminó");
            }
        });
    }
}
