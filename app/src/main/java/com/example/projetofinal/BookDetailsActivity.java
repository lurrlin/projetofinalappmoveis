package com.example.projetofinal;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.ui.CarregadorCapas;

public class BookDetailsActivity extends com.example.projetofinal.ui.AtividadeTematica {
    public static final String EXTRA_ID_LIVRO = "book_id";
    private BancoLivros banco;
    private MaterialButton botaoFavorito;
    private boolean favorito;
    private String identificador;

    @Override protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars());
            tela.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return margens;
        });
        findViewById(R.id.botao_voltar).setOnClickListener(tela -> finish());
        botaoFavorito = findViewById(R.id.botao_favorito);
        botaoFavorito.setEnabled(false);
        banco = BancoLivros.obterInstancia(this);
        identificador = getIntent().getStringExtra(EXTRA_ID_LIVRO);
        if (identificador == null) {
            informarErro(R.string.book_not_found);
            finish();
            return;
        }
        botaoFavorito.setOnClickListener(tela -> salvarFavorito());
        findViewById(R.id.botao_registrar).setOnClickListener(tela -> {
            Intent destino = new Intent(this, SalvarHistoricoActivity.class);
            destino.putExtra(EXTRA_ID_LIVRO, identificador);
            startActivity(destino);
        });
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                Book livro = banco.buscar(identificador);
                boolean favoritoSalvo = banco.ehFavorito(identificador);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    if (livro == null) {
                        informarErro(R.string.book_not_found);
                        finish();
                        return;
                    }
                    ((TextView) findViewById(R.id.titulo_livro)).setText(livro.getTitle());
                    CarregadorCapas.carregar(findViewById(R.id.capa_livro), livro.getUrlCapa());
                    ((TextView) findViewById(R.id.autor_livro)).setText(livro.getAuthor());
                    ((TextView) findViewById(R.id.clima_livro)).setText(livro.getGenero());
                    ((TextView) findViewById(R.id.sinopse_livro)).setText(livro.getSynopsis());
                    if (!livro.getChaveOpenLibrary().isEmpty()) {
                        findViewById(R.id.botao_fonte).setVisibility(android.view.View.VISIBLE);
                        findViewById(R.id.botao_fonte).setOnClickListener(vista -> {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(
                                        "https://openlibrary.org" + livro.getChaveOpenLibrary())));
                            } catch (android.content.ActivityNotFoundException erro) {
                                informarErro(R.string.sem_navegador);
                            }
                        });
                    }
                    favorito = favoritoSalvo;
                    atualizarBotao();
                    findViewById(R.id.botao_registrar).setEnabled(true);
                    aplicarCores();
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    informarErro(R.string.erro_banco);
                    finish();
                });
            }
        });
    }

    private void salvarFavorito() {
        boolean novoFavorito = !favorito;
        botaoFavorito.setEnabled(false);
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                banco.definirFavorito(identificador, novoFavorito);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    favorito = novoFavorito;
                    atualizarBotao();
                    Toast.makeText(this, favorito ? R.string.favorito_salvo
                            : R.string.favorito_removido, Toast.LENGTH_SHORT).show();
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    atualizarBotao();
                    informarErro(R.string.erro_banco);
                });
            }
        });
    }

    private void atualizarBotao() {
        botaoFavorito.setText(favorito ? R.string.remover_favorito : R.string.adicionar_favorito);
        botaoFavorito.setEnabled(true);
        ((com.example.projetofinal.view.CapaLivroView) findViewById(R.id.capa_livro)).definirFavorito(favorito);
    }

    private void informarErro(int mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

}
