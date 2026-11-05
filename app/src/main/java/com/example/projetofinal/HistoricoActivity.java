package com.example.projetofinal;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.model.RegistroHistorico;
import com.example.projetofinal.model.Book;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class HistoricoActivity extends com.example.projetofinal.ui.AtividadeTematica {
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historico);
        configurarBarraHistorico();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars());
            tela.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return margens;
        });
        findViewById(R.id.botao_voltar).setOnClickListener(tela -> finish());
        TextView situacao = findViewById(R.id.estado_historico);
        BancoLivros banco = BancoLivros.obterInstancia(this);
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                List<RegistroHistorico> registros = banco.listarHistorico();
                java.util.Map<String, Book> livros = new java.util.HashMap<>();
                for (RegistroHistorico registro : registros) {
                    if (!livros.containsKey(registro.idLivro)) livros.put(registro.idLivro, banco.buscar(registro.idLivro));
                }
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    situacao.setText(registros.isEmpty() ? getString(R.string.historico_vazio)
                            : getString(R.string.historico_total, registros.size()));
                    LinearLayout lista = findViewById(R.id.lista_historico);
                    for (RegistroHistorico registro : registros) {
                        android.view.View item = getLayoutInflater().inflate(R.layout.item_historico, lista, false);
                        String data = new java.text.SimpleDateFormat("dd MMM yyyy • HH:mm", java.util.Locale.forLanguageTag("pt-BR"))
                                .format(new Date(registro.data));
                        String luz = registro.luminosidade == null ? getString(R.string.sem_leitura_registro)
                                : getString(registro.simulada ? R.string.leitura_simulada : R.string.leitura_sensor,
                                        registro.luminosidade,
                                        getString(BookDetailsActivity.rotuloClima(Book.Mood.valueOf(registro.clima))));
                        String local = registro.latitude == null ? getString(R.string.registro_sem_local)
                                : getString(R.string.coordenadas_registro, registro.latitude,
                                        registro.longitude, registro.precisao);
                        ((TextView) item.findViewById(R.id.titulo_livro)).setText(registro.titulo);
                        ((TextView) item.findViewById(R.id.data_registro)).setText(data);
                        ((TextView) item.findViewById(R.id.luz_registro)).setText(luz);
                        TextView coordenadas = item.findViewById(R.id.local_registro);
                        coordenadas.setText(local);
                        android.view.View botaoLocal = item.findViewById(R.id.botao_local_registro);
                        botaoLocal.setVisibility(registro.latitude == null ? android.view.View.GONE : android.view.View.VISIBLE);
                        botaoLocal.setOnClickListener(vista -> coordenadas.setVisibility(
                                coordenadas.getVisibility() == android.view.View.VISIBLE ? android.view.View.GONE : android.view.View.VISIBLE));
                        Book livro = livros.get(registro.idLivro);
                        if (livro != null) com.example.projetofinal.ui.CarregadorCapas.carregar(item.findViewById(R.id.capa_livro), livro.getUrlCapa());
                        item.findViewById(R.id.botao_detalhes).setOnClickListener(vista -> {
                            android.content.Intent destino = new android.content.Intent(this, BookDetailsActivity.class);
                            destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, registro.idLivro);
                            startActivity(destino);
                        });
                        lista.addView(item);
                    }
                    aplicarCores();
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && !isFinishing()) situacao.setText(R.string.erro_historico);
                });
            }
        });
    }
}
