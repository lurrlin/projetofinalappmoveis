package com.example.projetofinal;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.model.Book;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BancoLivros banco;
    private LinearLayout listaLivros;
    private MaterialCheckBox filtroFavoritos;
    private TextView estadoCatalogo;
    private int versaoConsulta;

    @Override protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        banco = BancoLivros.obterInstancia(this);
        listaLivros = findViewById(R.id.lista_livros);
        filtroFavoritos = findViewById(R.id.filtro_favoritos);
        estadoCatalogo = findViewById(R.id.estado_catalogo);
        if (estadoSalvo != null) {
            filtroFavoritos.setChecked(estadoSalvo.getBoolean("somente_favoritos"));
        }
        filtroFavoritos.setOnCheckedChangeListener((botao, marcado) -> carregarLivros());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars());
            tela.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return margens;
        });
    }

    @Override protected void onResume() {
        super.onResume();
        carregarLivros();
    }

    @Override protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean("somente_favoritos", filtroFavoritos.isChecked());
        super.onSaveInstanceState(estado);
    }

    private void carregarLivros() {
        int consultaAtual = ++versaoConsulta;
        boolean somenteFavoritos = filtroFavoritos.isChecked();
        estadoCatalogo.setVisibility(View.VISIBLE);
        estadoCatalogo.setText(R.string.carregando_livros);
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                List<Book> livros = banco.listar(somenteFavoritos);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing() || consultaAtual != versaoConsulta) return;
                    exibirLivros(livros);
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing() || consultaAtual != versaoConsulta) return;
                    listaLivros.removeAllViews();
                    estadoCatalogo.setText(R.string.erro_banco);
                    Toast.makeText(this, R.string.erro_banco, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void exibirLivros(List<Book> livros) {
        listaLivros.removeAllViews();
        estadoCatalogo.setText(R.string.nenhum_favorito);
        estadoCatalogo.setVisibility(livros.isEmpty() ? View.VISIBLE : View.GONE);
        for (Book livro : livros) {
            View item = getLayoutInflater().inflate(R.layout.item_book, listaLivros, false);
            ((TextView) item.findViewById(R.id.titulo_livro)).setText(livro.getTitle());
            ((TextView) item.findViewById(R.id.autor_livro)).setText(livro.getAuthor());
            ((TextView) item.findViewById(R.id.clima_livro)).setText(
                    BookDetailsActivity.rotuloClima(livro.getMood()));
            View detalhes = item.findViewById(R.id.botao_detalhes);
            detalhes.setContentDescription(getString(R.string.details_for, livro.getTitle()));
            detalhes.setOnClickListener(tela -> {
                Intent destino = new Intent(this, BookDetailsActivity.class);
                destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, livro.getId());
                startActivity(destino);
            });
            listaLivros.addView(item);
        }
    }
}
