package com.example.projetofinal.data;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BancoLivrosTest {
    @Test public void favoritosPersistemAoReabrirBanco() {
        Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String nomeBanco = "teste_favoritos_" + System.nanoTime() + ".db";
        try {
            try (BancoLivros banco = new BancoLivros(contexto, nomeBanco)) {
                assertEquals(70, banco.listar(false).size());
                assertTrue(banco.listar(true).isEmpty());
                banco.definirFavorito("o-hobbit", true);
                banco.definirFavorito("o-hobbit", true);
                assertEquals(1, banco.listar(true).size());
            }
            try (BancoLivros banco = new BancoLivros(contexto, nomeBanco)) {
                assertEquals(70, banco.listar(false).size());
                assertTrue(banco.ehFavorito("o-hobbit"));
                assertEquals("o-hobbit", banco.listar(true).get(0).getId());
                assertNotNull(banco.buscar("o-hobbit"));
                assertNull(banco.buscar("inexistente"));
                banco.definirFavorito("o-hobbit", false);
            }
            try (BancoLivros banco = new BancoLivros(contexto, nomeBanco)) {
                assertFalse(banco.ehFavorito("o-hobbit"));
                assertTrue(banco.listar(true).isEmpty());
                assertEquals(70, banco.listar(false).size());
            }
        } finally {
            contexto.deleteDatabase(nomeBanco);
        }
    }
}
