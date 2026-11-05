package com.example.projetofinal;

import android.content.Context;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.data.CatalogoOpenLibrary;
import com.example.projetofinal.model.Book;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

/** Teste opcional de integração real: habilitar com -e verificar_rede true. */
@RunWith(AndroidJUnit4.class)
public class OpenLibraryRedeTest {
    @Test public void consultaApiRealImportaEmBancoIsoladoEAbreSeletor() throws Exception {
        assumeTrue("true".equals(InstrumentationRegistry.getArguments().getString("verificar_rede")));
        List<Book> livros = CatalogoOpenLibrary.buscar("Ursula Le Guin");
        assertFalse(livros.isEmpty());
        Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String nome = "teste_api_real_" + System.nanoTime() + ".db";
        try (BancoLivros banco = new BancoLivros(contexto, nome)) {
            assertTrue(banco.importarLivros(livros) > 0);
            assertEquals(0, banco.importarLivros(livros));
            assertTrue(banco.listar(false).size() > 70);
        } finally { contexto.deleteDatabase(nome); }
        try (ActivityScenario<MainActivity> cenario = ActivityScenario.launch(MainActivity.class)) {
            BancoLivros.EXECUTOR.submit(() -> { }).get(10, TimeUnit.SECONDS);
            cenario.onActivity(tela -> ((BottomNavigationView) tela.findViewById(R.id.navegacao)).setSelectedItemId(R.id.menu_biblioteca));
            BancoLivros.EXECUTOR.submit(() -> { }).get(10, TimeUnit.SECONDS);
            cenario.onActivity(tela -> {
                ((TextView) tela.findViewById(R.id.busca_livros)).setText("Ursula Le Guin");
                tela.findViewById(R.id.botao_buscar_online).performClick();
            });
            CatalogoOpenLibrary.EXECUTOR.submit(() -> { }).get(45, TimeUnit.SECONDS);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            onView(withText(R.string.escolher_importacao)).check(matches(isDisplayed()));
        }
    }
}
