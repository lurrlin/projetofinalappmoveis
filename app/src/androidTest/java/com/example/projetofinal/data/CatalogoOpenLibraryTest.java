package com.example.projetofinal.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.model.RegistroHistorico;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CatalogoOpenLibraryTest {
    private final Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test public void catalogoIncluidoTemObrasReaisUnicasETemasVariados() {
        List<Book> livros = CatalogoOpenLibrary.lerCatalogoInicial(contexto);
        assertEquals(64, livros.size());
        Set<String> chaves = new HashSet<>();
        Set<String> temas = new HashSet<>();
        Set<Book.Mood> climas = new HashSet<>();
        for (Book livro : livros) {
            assertTrue(livro.getChaveOpenLibrary().matches("/works/OL[0-9]+W"));
            assertTrue(chaves.add(livro.getChaveOpenLibrary()));
            assertTrue(livro.getUrlCapa().startsWith("https://covers.openlibrary.org/b/id/"));
            assertFalse(livro.getTitle().trim().isEmpty());
            temas.add(livro.getGenero());
            climas.add(livro.getMood());
        }
        assertTrue(temas.size() >= 8);
        assertEquals(3, climas.size());
    }

    @Test public void buscaAceitaCamposAusentesEIgnoraDuplicatasEChavesInvalidas() throws Exception {
        String resposta = "{\"docs\":[{\"key\":\"/works/OL123W\",\"title\":\"Original\","
                + "\"editions\":{\"docs\":[{\"title\":\"Edição em português\"}]},\"subject\":[\"Horror\"]},"
                + "{\"key\":\"/works/OL123W\",\"title\":\"Repetido\"},"
                + "{\"key\":\"https://outro.site\",\"title\":\"Inválido\"},"
                + "{\"key\":\"/works/OL27482W\",\"title\":\"The Hobbit\"}]}";
        List<Book> livros = CatalogoOpenLibrary.interpretarBusca(resposta);
        assertEquals(2, livros.size());
        assertEquals("Edição em português", livros.get(0).getTitle());
        assertEquals(Book.Mood.DARK, livros.get(0).getMood());
        assertEquals("", livros.get(0).getUrlCapa());
        assertEquals("Autoria não informada", livros.get(0).getAuthor());
        assertEquals("o-hobbit", livros.get(1).getId());
    }

    @Test public void importarDuasVezesNaoDuplicaNemApagaFavoritosOuHistorico() {
        String nome = "teste_importacao_" + System.nanoTime() + ".db";
        try (BancoLivros banco = new BancoLivros(contexto, nome)) {
            List<Book> livros = CatalogoOpenLibrary.lerCatalogoInicial(contexto);
            Book livro = livros.get(0);
            banco.definirFavorito(livro.getId(), true);
            banco.salvarRegistro(new RegistroHistorico("registro", livro.getId(), "", 1000,
                    null, null, false, null, null, null));
            assertEquals(0, banco.importarLivros(livros));
            assertEquals(0, banco.importarLivros(livros));
            assertEquals(70, banco.listar(false).size());
            assertTrue(banco.ehFavorito(livro.getId()));
            assertEquals(1, banco.listarHistorico().size());
        } finally { contexto.deleteDatabase(nome); }
    }

    @Test public void migracaoDaVersaoTresAmpliaSemPerderDados() {
        String nome = "teste_migracao_v3_" + System.nanoTime() + ".db";
        try {
            try (SQLiteDatabase anterior = contexto.openOrCreateDatabase(nome, Context.MODE_PRIVATE, null)) {
                anterior.execSQL("CREATE TABLE livros (id TEXT PRIMARY KEY, titulo TEXT NOT NULL, autor TEXT NOT NULL, "
                        + "clima TEXT NOT NULL, sinopse TEXT NOT NULL, favorito INTEGER NOT NULL DEFAULT 0, url_capa TEXT NOT NULL DEFAULT '')");
                anterior.execSQL("INSERT INTO livros VALUES ('o-hobbit', 'O Hobbit', 'J. R. R. Tolkien', 'LIGHT', 'Sinopse existente', 1, '')");
                anterior.execSQL("CREATE TABLE historico (id TEXT PRIMARY KEY, id_livro TEXT NOT NULL REFERENCES livros(id), "
                        + "data INTEGER NOT NULL, luminosidade REAL, clima TEXT, simulada INTEGER NOT NULL, latitude REAL, longitude REAL, precisao REAL)");
                anterior.execSQL("INSERT INTO historico VALUES ('antigo', 'o-hobbit', 1000, 500, 'LIGHT', 0, -23.55, -46.63, 20)");
                anterior.setVersion(3);
            }
            try (BancoLivros banco = new BancoLivros(contexto, nome)) {
                assertEquals(65, banco.listar(false).size());
                assertTrue(banco.ehFavorito("o-hobbit"));
                assertEquals("Sinopse existente", banco.buscar("o-hobbit").getSynopsis());
                assertEquals("Fantasia", banco.buscar("o-hobbit").getGenero());
                assertEquals(-23.55, banco.listarHistorico().get(0).latitude, .00001);
                assertEquals(5, banco.getReadableDatabase().getVersion());
            }
        } finally { contexto.deleteDatabase(nome); }
    }
}
