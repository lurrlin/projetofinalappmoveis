package com.example.projetofinal.model;

/** Retrato da escolha do usuário, com localização opcional. */
public final class RegistroHistorico {
    public final String identificador;
    public final String idLivro;
    public final String titulo;
    public final long data;
    public final Float luminosidade;
    public final String clima;
    public final boolean simulada;
    public final Double latitude;
    public final Double longitude;
    public final Float precisao;

    public RegistroHistorico(String identificador, String idLivro, String titulo, long data,
            Float luminosidade, String clima, boolean simulada,
            Double latitude, Double longitude, Float precisao) {
        this.identificador = identificador;
        this.idLivro = idLivro;
        this.titulo = titulo;
        this.data = data;
        this.luminosidade = luminosidade;
        this.clima = clima;
        this.simulada = simulada;
        this.latitude = latitude;
        this.longitude = longitude;
        this.precisao = precisao;
    }
}
