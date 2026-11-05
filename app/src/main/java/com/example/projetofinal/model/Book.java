package com.example.projetofinal.model;

public final class Book {
    public enum Mood { LIGHT, MEDIUM, DARK }

    private final String id;
    private final String title;
    private final String author;
    private final Mood mood;
    private final String synopsis;
    private final String urlCapa;
    private final String genero;
    private final String chaveOpenLibrary;

    public Book(String id, String title, String author, Mood mood, String synopsis) {
        this(id, title, author, mood, synopsis, "");
    }

    public Book(String id, String title, String author, Mood mood, String synopsis, String urlCapa) {
        this(id, title, author, mood, synopsis, urlCapa, "Literatura", "");
    }

    public Book(String id, String title, String author, Mood mood, String synopsis, String urlCapa,
                String genero, String chaveOpenLibrary) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.mood = mood;
        this.synopsis = synopsis;
        this.urlCapa = urlCapa;
        this.genero = genero;
        this.chaveOpenLibrary = chaveOpenLibrary;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Mood getMood() { return mood; }
    public String getSynopsis() { return synopsis; }
    public String getUrlCapa() { return urlCapa; }
    public String getGenero() { return genero; }
    public String getChaveOpenLibrary() { return chaveOpenLibrary; }
}
