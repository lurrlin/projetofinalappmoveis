package com.example.projetofinal.luminosidade;

import com.example.projetofinal.data.BookCatalog;
import com.example.projetofinal.model.Book;
import org.junit.Test;
import java.util.Collections;
import static org.junit.Assert.*;

public class ClassificadorLuminosidadeTest {
    @Test public void limitesDasFaixas() {
        assertEquals(Book.Mood.DARK, ClassificadorLuminosidade.classificar(0));
        assertEquals(Book.Mood.DARK, ClassificadorLuminosidade.classificar(49.9f));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificar(50));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificar(299.9f));
        assertEquals(Book.Mood.LIGHT, ClassificadorLuminosidade.classificar(300));
        assertEquals(Book.Mood.LIGHT, ClassificadorLuminosidade.classificar(100000));
    }

    @Test public void oscilacoesPequenasNaoTrocamRecomendacoes() {
        assertEquals(Book.Mood.DARK, ClassificadorLuminosidade.classificarSensor(59, Book.Mood.DARK));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificarSensor(60, Book.Mood.DARK));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificarSensor(40, Book.Mood.MEDIUM));
        assertEquals(Book.Mood.DARK, ClassificadorLuminosidade.classificarSensor(39, Book.Mood.MEDIUM));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificarSensor(329, Book.Mood.MEDIUM));
        assertEquals(Book.Mood.LIGHT, ClassificadorLuminosidade.classificarSensor(330, Book.Mood.MEDIUM));
        assertEquals(Book.Mood.LIGHT, ClassificadorLuminosidade.classificarSensor(270, Book.Mood.LIGHT));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificarSensor(269, Book.Mood.LIGHT));
        assertEquals(Book.Mood.DARK, ClassificadorLuminosidade.classificarSensor(0, Book.Mood.LIGHT));
        assertEquals(Book.Mood.LIGHT, ClassificadorLuminosidade.classificarSensor(1000, Book.Mood.DARK));
        assertEquals(Book.Mood.MEDIUM, ClassificadorLuminosidade.classificarSensor(100, null));
    }

    @Test public void rejeitaLeiturasInvalidas() {
        for (float lux : new float[]{-1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertFalse(ClassificadorLuminosidade.leituraValida(lux));
            assertThrows(IllegalArgumentException.class, () -> ClassificadorLuminosidade.classificar(lux));
        }
    }

    @Test public void recomendaSomenteLivrosDoClimaSemAlterarCatalogo() {
        for (Book.Mood clima : Book.Mood.values()) {
            assertEquals(2, ClassificadorLuminosidade.recomendar(BookCatalog.getAll(), clima).size());
            for (Book livro : ClassificadorLuminosidade.recomendar(BookCatalog.getAll(), clima)) {
                assertEquals(clima, livro.getMood());
            }
        }
        assertEquals(6, BookCatalog.getAll().size());
        assertTrue(ClassificadorLuminosidade.recomendar(BookCatalog.getAll(), null).isEmpty());
        assertTrue(ClassificadorLuminosidade.recomendar(Collections.emptyList(), Book.Mood.DARK).isEmpty());
    }
}
