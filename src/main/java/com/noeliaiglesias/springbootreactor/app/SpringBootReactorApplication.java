package com.noeliaiglesias.springbootreactor.app;

import com.noeliaiglesias.springbootreactor.app.model.Comentarios;
import com.noeliaiglesias.springbootreactor.app.model.Usuario;
import com.noeliaiglesias.springbootreactor.app.model.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;


@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(SpringBootReactorApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        // ejemploIterablesStringFlux();

        // ejemploFlux();

        // ejemploFluxConFlatMap();

        // getEjemploFluxToString();

        // ejemploCollectList();

        // ejemploUsuarioComentariosFlatMap();

        // ejemploUsuarioComentariosZipWith();

        // ejemploUsuarioComentariosZipWithForma2();

        //ejemploZipWithRangos();

        // ejemploInterval();

        //ejemploDelayElements();

        // ejemploIntervalInfinito();

        // ejemploIntervalDesdeCreate();

        ejemploContraPresion();
    }

    private void ejemploContraPresion() {

        Flux.range(1, 10)
                .log()
                //.limitRate(5)
                .subscribe(new Subscriber<>() {

                    private final Integer limite = 5;
                    private Subscription s;
                    private Integer consumido = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer t) {
                        log.info(t.toString());
                        consumido++;
                        if (consumido.equals(limite)) {
                            consumido = 0;
                            s.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.info("Error: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("completado");
                    }
                });
    }

    private void ejemploIntervalDesdeCreate() {
        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                private Integer contador = 0;

                @Override
                public void run() {
                    emitter.next(++contador);
                    if (contador == 10) {
                        timer.cancel();
                        emitter.complete();
                    }

                    if (contador == 5) {
                        timer.cancel();
                        emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                    }

                }
            }, 1000, 1000);
        }).subscribe(next -> log.info(next.toString()), error -> log.severe(error.getMessage()),
                     () -> log.info("Hemos terminado"));
    }

    private void ejemploIntervalInfinito() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown).flatMap(i -> {
            if (i >= 5) {
                return Flux.error(new InterruptedException("Solo hasta 5!"));
            }
            return Flux.just(i);
        }).map(i -> "Hola " + i).retry(2).subscribe(log::info, e -> log.severe(e.getMessage()));

        latch.await();
    }

    private void ejemploDelayElements() {
        Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));

        rango.blockLast();
    }

    private void ejemploInterval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> log.info(i.toString())).blockLast();
    }

    private void ejemploZipWithRangos() {
        Flux<Integer> rangos = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4).map(i -> (i * 2))
                .zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
                .subscribe(log::info);
    }

    private void ejemploUsuarioComentariosZipWithForma2() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(this::getUsuario);

        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(this::getComentarios);

        Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono).map(tuple -> {
            Usuario u = tuple.getT1();
            Comentarios c = tuple.getT2();
            return new UsuarioComentarios(u, c);
        });

        usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
    }

    private void ejemploUsuarioComentariosZipWith() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(this::getUsuario);

        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(this::getComentarios);

        Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono,
                                                                             (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));

        usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
    }

    private Comentarios getComentarios() {
        Comentarios comentarios = new Comentarios();
        comentarios.addComentario("Un esquema básico.");
        comentarios.addComentario("Un sistema para hacer el comentario paso a paso.");
        comentarios.addComentario("Algunos «trucos».");
        return comentarios;
    }

    private Usuario getUsuario() {
        return new Usuario("Noelia", "Iglesias");
    }

    private void ejemploUsuarioComentariosFlatMap() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> getUsuario());

        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            return getComentarios();
        });

        Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
                .flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)));
        usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
    }

    private void ejemploCollectList() {
        /*Flux<String> flux = Flux.just("Hola", "Mundo", "Spring", "Boot");
        List<String> list = flux.collectList().block();
        log.info("list: " + list);*/

        List<Usuario> usuariosList = getUsuarios();
        Flux.fromIterable(usuariosList)
                .collectList()
                .subscribe(lista ->
                                   log.info("usuarios: " + lista.toString()));
    }

    private void getEjemploFluxToString() {
        Flux.fromIterable(getUsuarios())
                .map(usuario -> usuario.getNombre().concat(" ").concat(usuario.getApellido()))
                .flatMap(nombre -> {
                    if (nombre.contains("Pepe")) {
                        return Mono.just(nombre);
                    } else {
                        return Mono.empty();
                    }
                })
                .subscribe(u -> log.info("Nombre: " + u));
    }

    private void ejemploFluxConFlatMap() {
        Flux.fromIterable(getStrings())
                .map(usuario -> new Usuario(usuario.split(" ")[0].toUpperCase(), usuario.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> {
                    if (usuario.getNombre().equalsIgnoreCase("pepe")) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .subscribe(u -> log.info("Apellido: " + u.getApellido()));
    }

    private void ejemploIterablesStringFlux() {
        List<String> usuariosList = getStrings();

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

        nombres.subscribe(System.out::println);
    }

    private List<String> getStrings() {
        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Noelia Iglesias");
        usuariosList.add("Pedro Romero");
        usuariosList.add("Juan González");
        usuariosList.add("María López");
        usuariosList.add("Pepe Viyuela");
        usuariosList.add("Pepe Navarro");
        return usuariosList;
    }

    private List<Usuario> getUsuarios() {
        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Noelia", "Iglesias"));
        usuariosList.add(new Usuario("Pedro", "Romero"));
        usuariosList.add(new Usuario("Juan", "Gonzalez"));
        usuariosList.add(new Usuario("Maria", "Lopez"));
        usuariosList.add(new Usuario("Pepe", "Viyuela"));
        usuariosList.add(new Usuario("Pepe", "Navarro"));
        return usuariosList;
    }


    private void ejemploFlux() {
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

        users.subscribe(e -> log.info(e.getNombre()), error -> log.severe("Error: " + error.getMessage()), () -> log.info("Terminó ejemplosFlux"));
//        Los fluxes son inmutables, es decir, los originales no se modifican.
//        Los fluxes son observables, es decir, se pueden suscribir a ellos.
//        Los fluxes son flujos de datos, es decir, se pueden emitir datos.
//        Los fluxes son flujos de eventos, es decir, se pueden emitir eventos.
//        Los fluxes son flujos de acciones, es decir, se pueden ejecutar acciones.
        users.subscribe(System.out::println);
    }


}
