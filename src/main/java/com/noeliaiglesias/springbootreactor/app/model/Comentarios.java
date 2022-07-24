package com.noeliaiglesias.springbootreactor.app.model;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {
    private final List<String> comentarios;

    public Comentarios() {
        this.comentarios = new ArrayList<>();
    }

    public void addComentario(String comentario) {
        this.comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "comentarios: " + comentarios;
    }
}
