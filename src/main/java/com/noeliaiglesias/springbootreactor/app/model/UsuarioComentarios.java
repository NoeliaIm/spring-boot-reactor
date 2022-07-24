package com.noeliaiglesias.springbootreactor.app.model;

public class UsuarioComentarios {
    private final Usuario usuario;
    private final Comentarios comentarios;

    public UsuarioComentarios(Usuario usuario, Comentarios comentarios) {
        this.usuario = usuario;
        this.comentarios = comentarios;
    }

    @Override
    public String toString() {
        return "UsuarioComentarios [usuario=" + usuario + ", comentarios=" + comentarios + "]";
    }
}
