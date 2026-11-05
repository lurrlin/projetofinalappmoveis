package com.example.projetofinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.projetofinal.data.BancoLivros;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FluxoAplicativoTest {
    private final Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private void aguardarBanco() throws Exception {
        BancoLivros.EXECUTOR.submit(() -> { }).get(10, TimeUnit.SECONDS);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private void capturar(String nome) throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        android.os.SystemClock.sleep(250);
        Bitmap imagem = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(imagem);
        try (FileOutputStream arquivo = new FileOutputStream(
                new File(contexto.getExternalFilesDir(null), nome + ".png"))) {
            imagem.compress(Bitmap.CompressFormat.PNG, 100, arquivo);
        } finally {
            imagem.recycle();
        }
    }

    @Test public void bibliotecaCompletaEPreservaAbaAoRecriar() throws Exception {
        try (ActivityScenario<MainActivity> cenario = ActivityScenario.launch(MainActivity.class)) {
            aguardarBanco();
            cenario.onActivity(tela -> ((com.google.android.material.bottomnavigation.BottomNavigationView)
                    tela.findViewById(R.id.navegacao)).setSelectedItemId(R.id.menu_biblioteca));
            aguardarBanco();
            cenario.onActivity(tela -> assertTrue(((androidx.recyclerview.widget.RecyclerView) tela.findViewById(R.id.lista_livros)).getAdapter().getItemCount() >= 70));
            capturar("inicio");
            cenario.recreate();
            aguardarBanco();
            cenario.onActivity(tela -> {
                assertEquals(R.id.menu_biblioteca, ((com.google.android.material.bottomnavigation.BottomNavigationView)
                        tela.findViewById(R.id.navegacao)).getSelectedItemId());
                assertTrue(((androidx.recyclerview.widget.RecyclerView) tela.findViewById(R.id.lista_livros)).getAdapter().getItemCount() >= 70);
            });
        }
    }

    @Test public void detalhesMostramLivroSelecionado() throws Exception {
        Intent destino = new Intent(contexto, BookDetailsActivity.class);
        destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, "dracula");
        try (ActivityScenario<BookDetailsActivity> cenario = ActivityScenario.launch(destino)) {
            aguardarBanco();
            cenario.onActivity(tela -> {
                assertEquals("Drácula", ((TextView) tela.findViewById(R.id.titulo_livro)).getText().toString());
                assertEquals("Bram Stoker", ((TextView) tela.findViewById(R.id.autor_livro)).getText().toString());
                assertTrue(tela.findViewById(R.id.botao_registrar).isEnabled());
            });
            capturar("detalhes");
        }
    }

    @Test public void buscaLocalPorTemaEAutorPreservaTextoAoGirar() throws Exception {
        try (ActivityScenario<MainActivity> cenario = ActivityScenario.launch(MainActivity.class)) {
            aguardarBanco();
            cenario.onActivity(tela -> ((com.google.android.material.bottomnavigation.BottomNavigationView)
                    tela.findViewById(R.id.navegacao)).setSelectedItemId(R.id.menu_biblioteca));
            aguardarBanco();
            cenario.onActivity(tela -> {
                ((TextView) tela.findViewById(R.id.busca_livros)).setText("filosofia");
                assertTrue(((androidx.recyclerview.widget.RecyclerView) tela.findViewById(R.id.lista_livros)).getAdapter().getItemCount() >= 5);
                ((TextView) tela.findViewById(R.id.busca_livros)).setText("Machado de Assis");
                assertEquals(2, ((androidx.recyclerview.widget.RecyclerView) tela.findViewById(R.id.lista_livros)).getAdapter().getItemCount());
            });
            cenario.recreate();
            aguardarBanco();
            cenario.onActivity(tela -> assertEquals("Machado de Assis", ((TextView) tela.findViewById(R.id.busca_livros)).getText().toString()));
        }
    }

    @Test public void salvaSemLocalizacaoESemDuplicarAoRecriar() throws Exception {
        Intent destino = new Intent(contexto, SalvarHistoricoActivity.class);
        destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, "o-hobbit");
        String[] identificador = new String[1];
        try {
            try (ActivityScenario<SalvarHistoricoActivity> cenario = ActivityScenario.launch(destino)) {
                aguardarBanco();
                cenario.onActivity(tela -> {
                    Bundle estado = new Bundle();
                    tela.onSaveInstanceState(estado);
                    identificador[0] = estado.getString("id_registro");
                    assertFalse(((MaterialCheckBox) tela.findViewById(R.id.incluir_localizacao)).isChecked());
                    assertTrue(tela.findViewById(R.id.botao_salvar_historico).isEnabled());
                    tela.findViewById(R.id.botao_salvar_historico).performClick();
                });
                aguardarBanco();
                assertTrue(BancoLivros.obterInstancia(contexto).existeRegistro(identificador[0]));
                assertNull(BancoLivros.obterInstancia(contexto).listarHistorico().stream()
                        .filter(registro -> registro.identificador.equals(identificador[0])).findFirst().get().luminosidade);
                capturar("registro");
                cenario.recreate();
                aguardarBanco();
                cenario.onActivity(tela -> {
                    assertFalse(tela.findViewById(R.id.botao_salvar_historico).isEnabled());
                    assertEquals(tela.getString(R.string.registro_ja_salvo),
                            ((TextView) tela.findViewById(R.id.estado_registro)).getText().toString());
                });
            }
            try (ActivityScenario<HistoricoActivity> cenario = ActivityScenario.launch(HistoricoActivity.class)) {
                aguardarBanco();
                cenario.onActivity(tela -> assertTrue(
                        ((LinearLayout) tela.findViewById(R.id.lista_historico)).getChildCount() > 0));
                capturar("historico");
            }
        } finally {
            if (identificador[0] != null) {
                BancoLivros.EXECUTOR.submit(() -> BancoLivros.obterInstancia(contexto)
                        .getWritableDatabase().delete("historico", "id = ?",
                                new String[]{identificador[0]})).get(10, TimeUnit.SECONDS);
            }
        }
    }
}
