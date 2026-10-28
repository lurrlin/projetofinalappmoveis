package com.example.projetofinal.data;

import com.example.projetofinal.model.Book;
import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class BookCatalogTest {
    @Test public void everyBookHasUniqueIdAndCanBeOpened() {
        Set<String> ids = new HashSet<>();
        for (Book book : BookCatalog.getAll()) {
            assertTrue(ids.add(book.getId()));
            assertSame(book, BookCatalog.findById(book.getId()));
            assertFalse(book.getTitle().trim().isEmpty());
            assertFalse(book.getAuthor().trim().isEmpty());
            assertFalse(book.getSynopsis().trim().isEmpty());
        }
    }

    @Test public void everyMoodHasBooksForFutureRecommendations() {
        for (Book.Mood mood : Book.Mood.values()) {
            assertTrue(BookCatalog.getAll().stream().anyMatch(book -> book.getMood() == mood));
        }
    }

    @Test public void unknownBookDoesNotReturnWrongDetails() {
        assertNull(BookCatalog.findById("inexistente"));
        assertNull(BookCatalog.findById(null));
    }
}
