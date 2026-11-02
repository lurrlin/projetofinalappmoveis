package com.example.projetofinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.model.Book;
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
        // Aguarda a composição do quadro, além da fila de eventos da interface.
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

    @Test public void simulacaoMudaLivrosEPreservaEstadoAoRecriar() throws Exception {
        try (ActivityScenario<MainActivity> cenario = ActivityScenario.launch(MainActivity.class)) {
            aguardarBanco();
            cenario.onActivity(tela -> {
                ((MaterialCheckBox) tela.findViewById(R.id.modo_manual)).setChecked(true);
                ((SeekBar) tela.findViewById(R.id.controle_luminosidade)).setProgress(0);
                LinearLayout lista = tela.findViewById(R.id.lista_livros);
                assertEquals(2, lista.getChildCount());
                assertEquals("O Iluminado", ((TextView) lista.getChildAt(0)
                        .findViewById(R.id.titulo_livro)).getText().toString());
            });
            capturar("inicio");
            cenario.recreate();
            aguardarBanco();
            cenario.onActivity(tela -> {
                assertTrue(((MaterialCheckBox) tela.findViewById(R.id.modo_manual)).isChecked());
                assertEquals(0, ((SeekBar) tela.findViewById(R.id.controle_luminosidade)).getProgress());
                ((SeekBar) tela.findViewById(R.id.controle_luminosidade)).setProgress(1000);
                LinearLayout lista = tela.findViewById(R.id.lista_livros);
                assertEquals(2, lista.getChildCount());
                assertEquals("Harry Potter e a Pedra Filosofal", ((TextView) lista.getChildAt(0)
                        .findViewById(R.id.titulo_livro)).getText().toString());
                ((MaterialCheckBox) tela.findViewById(R.id.filtro_clima)).setChecked(false);
                assertEquals(6, lista.getChildCount());
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

    @Test public void salvaSemLocalizacaoESemDuplicarAoRecriar() throws Exception {
        Intent destino = new Intent(contexto, SalvarHistoricoActivity.class);
        destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, "o-hobbit");
        destino.putExtra(SalvarHistoricoActivity.EXTRA_LUX, 500f);
        destino.putExtra(SalvarHistoricoActivity.EXTRA_CLIMA, Book.Mood.LIGHT.name());
        destino.putExtra(SalvarHistoricoActivity.EXTRA_SIMULADA, true);
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
