package com.example.projetofinal.model;

/** Livro do catálogo. O identificador permanece estável entre as telas. */
public final class Book {
    public enum Mood { LIGHT, MEDIUM, DARK }

    private final String id;
    private final String title;
    private final String author;
    private final Mood mood;
    private final String synopsis;

    public Book(String id, String title, String author, Mood mood, String synopsis) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.mood = mood;
        this.synopsis = synopsis;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Mood getMood() { return mood; }
    public String getSynopsis() { return synopsis; }
}
