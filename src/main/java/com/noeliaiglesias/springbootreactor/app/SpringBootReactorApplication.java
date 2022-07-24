package com.noeliaiglesias.springbootreactor.app;

import com.noeliaiglesias.springbootreactor.app.model.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(SpringBootReactorApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Noelia Iglesias");
        usuariosList.add("Pedro Romero");
        usuariosList.add("Juan González");
        usuariosList.add("María López");
        usuariosList.add("Pepe Viyuela");
        usuariosList.add("Pepe Navarro");

        Flux<String> nombres = Flux.fromIterable(usuariosList); /*Flux.just("Andres Guzman" , "Pedro Fulano" , "Maria Fulana", "Diego Sultano", "Juan Mengano", "Bruce Lee", "Bruce Willis");*/

        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("pepe"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Nombres no pueden ser vacíos");
                    }

                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                })
                .map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                });

        usuarios.subscribe(e -> log.info(e.toString()),
                           error -> log.severe(error.getMessage()),
                           () -> log.info("Ha finalizado la ejecución del observable con éxito!"));

        Flux<Usuario> users = Flux.just("Noelia Iglesias", "Pedro Romero ", "Juan González", "Pepe Viyuela", "Pepe Navarro")
                .map(el -> new Usuario(el.split(" ")[0], el.split(" ")[1]))
                .filter(user -> user.getNombre().equals("Pepe"))
                .doOnNext(user -> {
                    if (user == null) {
                        throw new RuntimeException("El nombre no puede ser vacío");
                    }
                    System.out.println("Emitiendo: " + user.getNombre() + " " + user.getApellido());
                })
                .map(user -> {
                    user.setNombre(user.getNombre().toUpperCase());
                    return user;
                });

        nombres.subscribe(log::info, error -> log.severe("Error: " + error.getMessage()), () -> log.info("Terminó nombres"));

        users.subscribe(e -> log.info(e.getNombre()), error -> log.severe("Error: " + error.getMessage()), () -> log.info("Terminó nombres2"));
//        Los fluxes son inmutables, es decir, los originales no se modifican.
//        Los fluxes son observables, es decir, se pueden suscribir a ellos.
//        Los fluxes son flujos de datos, es decir, se pueden emitir datos.
//        Los fluxes son flujos de eventos, es decir, se pueden emitir eventos.
//        Los fluxes son flujos de acciones, es decir, se pueden ejecutar acciones.

        nombres.subscribe(System.out::println);
        users.subscribe(System.out::println);
    }
}
