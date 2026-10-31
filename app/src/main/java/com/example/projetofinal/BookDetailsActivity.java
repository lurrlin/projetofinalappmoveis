package com.example.projetofinal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.model.Book;

public class BookDetailsActivity extends AppCompatActivity {
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
                    ((TextView) findViewById(R.id.autor_livro)).setText(livro.getAuthor());
                    ((TextView) findViewById(R.id.clima_livro)).setText(rotuloClima(livro.getMood()));
                    ((TextView) findViewById(R.id.sinopse_livro)).setText(livro.getSynopsis());
                    favorito = favoritoSalvo;
                    atualizarBotao();
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
    }

    private void informarErro(int mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    static int rotuloClima(Book.Mood clima) {
        switch (clima) {
            case LIGHT: return R.string.light;
            case MEDIUM: return R.string.medium;
            default: return R.string.dark;
        }
    }
}
