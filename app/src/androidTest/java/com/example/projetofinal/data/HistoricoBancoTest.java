package com.example.projetofinal.data;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.model.RegistroHistorico;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class HistoricoBancoTest {
    private final Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test public void historicoPersisteComESemCoordenadasSemDuplicar() {
        String nome = "teste_historico_" + System.nanoTime() + ".db";
        try {
            try (BancoLivros banco = new BancoLivros(contexto, nome)) {
                assertTrue(banco.listarHistorico().isEmpty());
                RegistroHistorico semLocal = new RegistroHistorico("registro-1", "o-hobbit", "", 1000,
                        500f, Book.Mood.LIGHT.name(), true, null, null, null);
                banco.salvarRegistro(semLocal);
                banco.salvarRegistro(semLocal);
                banco.salvarRegistro(new RegistroHistorico("registro-2", "dracula", "", 2000,
                        10f, Book.Mood.DARK.name(), false, -23.55, -46.63, 20f));
            }
            try (BancoLivros banco = new BancoLivros(contexto, nome)) {
                List<RegistroHistorico> registros = banco.listarHistorico();
                assertEquals(2, registros.size());
                assertEquals("registro-2", registros.get(0).identificador);
                assertEquals("Drácula", registros.get(0).titulo);
                assertEquals(-23.55, registros.get(0).latitude, 0.00001);
                assertEquals(-46.63, registros.get(0).longitude, 0.00001);
                assertEquals(20f, registros.get(0).precisao, 0.01f);
                assertFalse(registros.get(0).simulada);
                assertTrue(registros.get(1).simulada);
                assertNull(registros.get(1).latitude);
                banco.salvarRegistro(new RegistroHistorico("registro-3", "o-hobbit", "", 3000,
                        null, null, false, null, null, null));
                assertNull(banco.listarHistorico().get(0).luminosidade);
                assertTrue(banco.existeRegistro("registro-1"));
            }
        } finally {
            contexto.deleteDatabase(nome);
        }
    }

    @Test public void migracaoDaVersaoUmPreservaLivrosEFavoritos() {
        String nome = "teste_migracao_" + System.nanoTime() + ".db";
        try {
            try (SQLiteDatabase anterior = contexto.openOrCreateDatabase(nome, Context.MODE_PRIVATE, null)) {
                anterior.execSQL("CREATE TABLE livros (id TEXT PRIMARY KEY, titulo TEXT NOT NULL, "
                        + "autor TEXT NOT NULL, clima TEXT NOT NULL, sinopse TEXT NOT NULL, "
                        + "favorito INTEGER NOT NULL DEFAULT 0 CHECK(favorito IN (0, 1)))");
                for (Book livro : BookCatalog.getAll()) {
                    ContentValues valores = new ContentValues();
                    valores.put("id", livro.getId());
                    valores.put("titulo", livro.getTitle());
                    valores.put("autor", livro.getAuthor());
                    valores.put("clima", livro.getMood().name());
                    valores.put("sinopse", livro.getSynopsis());
                    valores.put("favorito", livro.getId().equals("o-hobbit") ? 1 : 0);
                    anterior.insertOrThrow("livros", null, valores);
                }
                anterior.setVersion(1);
            }
            try (BancoLivros banco = new BancoLivros(contexto, nome)) {
                assertEquals(70, banco.listar(false).size());
                assertTrue(banco.ehFavorito("o-hobbit"));
                assertEquals(4, banco.getReadableDatabase().getVersion());
                assertTrue(banco.buscar("o-hobbit").getUrlCapa().startsWith("https://covers.openlibrary.org/"));
                assertTrue(banco.listarHistorico().isEmpty());
                banco.salvarRegistro(new RegistroHistorico("apos-migracao", "o-hobbit", "", 1000,
                        null, null, false, null, null, null));
                assertEquals(1, banco.listarHistorico().size());
            }
        } finally {
            contexto.deleteDatabase(nome);
        }
    }

    @Test public void rejeitaCoordenadasIncompletasELivroInexistente() {
        String nome = "teste_validacao_" + System.nanoTime() + ".db";
        try (BancoLivros banco = new BancoLivros(contexto, nome)) {
            assertThrows(IllegalArgumentException.class, () -> banco.salvarRegistro(
                    new RegistroHistorico("incompleto", "o-hobbit", "", 1000,
                            null, null, false, -23.55, null, null)));
            assertThrows(android.database.sqlite.SQLiteConstraintException.class, () -> banco.salvarRegistro(
                    new RegistroHistorico("inexistente", "nao-existe", "", 1000,
                            null, null, false, null, null, null)));
            assertTrue(banco.listarHistorico().isEmpty());
        } finally {
            contexto.deleteDatabase(nome);
        }
    }
}
