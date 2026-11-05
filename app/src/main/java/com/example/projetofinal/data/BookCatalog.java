package com.example.projetofinal.data;

import com.example.projetofinal.model.Book;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class BookCatalog {
    private BookCatalog() { }

    private static final List<Book> BOOKS = Collections.unmodifiableList(Arrays.asList(
        new Book("harry-potter-1", "Harry Potter e a Pedra Filosofal", "J. K. Rowling",
            Book.Mood.LIGHT, "Um garoto descobre que é bruxo e entra em uma escola de magia. Entre amizades e desafios, encontra pistas sobre um segredo guardado no castelo."),
        new Book("o-hobbit", "O Hobbit", "J. R. R. Tolkien",
            Book.Mood.LIGHT, "Bilbo deixa o conforto de casa para acompanhar um grupo de anões. A jornada traz criaturas inesperadas, enigmas e a descoberta de uma coragem que ele não sabia ter."),
        new Book("sherlock", "As Aventuras de Sherlock Holmes", "Arthur Conan Doyle",
            Book.Mood.MEDIUM, "Holmes e Watson investigam casos em que detalhes aparentemente comuns mudam toda a explicação. Uma coleção de mistérios guiados pela observação e pelo raciocínio."),
        new Book("dom-casmurro", "Dom Casmurro", "Machado de Assis",
            Book.Mood.MEDIUM, "Bentinho revisita sua juventude e sua relação com Capitu. Memória, ciúme e dúvida se misturam em um relato que convida o leitor a questionar o narrador."),
        new Book("o-iluminado", "O Iluminado", "Stephen King",
            Book.Mood.DARK, "Uma família passa o inverno em um hotel isolado. Enquanto forças inquietantes se manifestam, o lugar intensifica tensões e transforma o isolamento em ameaça."),
        new Book("dracula", "Drácula", "Bram Stoker",
            Book.Mood.DARK, "Um encontro na Transilvânia revela uma presença ameaçadora. Cartas e diários acompanham pessoas que tentam compreender e enfrentar o conde Drácula.")
    ));

    public static List<Book> getAll() { return BOOKS; }

    public static Book findById(String id) {
        for (Book book : BOOKS) {
            if (book.getId().equals(id)) return book;
        }
        return null;
    }
}
