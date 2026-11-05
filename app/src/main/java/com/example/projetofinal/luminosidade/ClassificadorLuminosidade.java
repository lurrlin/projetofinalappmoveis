package com.example.projetofinal.luminosidade;

import com.example.projetofinal.model.Book;
import java.util.ArrayList;
import java.util.List;

public final class ClassificadorLuminosidade {
    private ClassificadorLuminosidade() { }

    public static boolean leituraValida(float lux) {
        return !Float.isNaN(lux) && !Float.isInfinite(lux) && lux >= 0;
    }

    public static Book.Mood classificar(float lux) {
        if (!leituraValida(lux)) throw new IllegalArgumentException("Luminosidade inválida.");
        if (lux < 50) return Book.Mood.DARK;
        if (lux < 300) return Book.Mood.MEDIUM;
        return Book.Mood.LIGHT;
    }

    public static Book.Mood classificarSensor(float lux, Book.Mood anterior) {
        Book.Mood inicial = classificar(lux);
        if (anterior == null) return inicial;
        if (anterior == Book.Mood.DARK && lux < 60) return anterior;
        if (anterior == Book.Mood.MEDIUM && lux >= 40 && lux < 330) return anterior;
        if (anterior == Book.Mood.LIGHT && lux >= 270) return anterior;
        return inicial;
    }

    public static List<Book> recomendar(List<Book> livros, Book.Mood clima) {
        List<Book> recomendados = new ArrayList<>();
        for (Book livro : livros) {
            if (livro.getMood() == clima) recomendados.add(livro);
        }
        return recomendados;
    }
}
