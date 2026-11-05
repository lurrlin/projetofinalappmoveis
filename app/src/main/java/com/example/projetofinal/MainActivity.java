package com.example.projetofinal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.luminosidade.ClassificadorLuminosidade;
import com.example.projetofinal.luminosidade.SensorLuminosidade;
import com.example.projetofinal.view.MedidorLuminosidade;
import com.example.projetofinal.ui.CarregadorCapas;
import com.example.projetofinal.ui.AtividadeTematica;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class MainActivity extends AtividadeTematica {
    private BancoLivros banco;
    private LinearLayout listaLivros;
    private TextView estadoCatalogo;
    private int versaoConsulta;
    private int abaAtual = R.id.menu_descobrir;
    private SensorLuminosidade sensorLuminosidade;
    private MedidorLuminosidade medidor;
    private TextView leituraLuminosidade;
    private Book.Mood climaAtual;
    private Float ultimaLuminosidade;
    private List<Book> livrosCarregados;
    private final Handler temporizador = new Handler(Looper.getMainLooper());
    private final Runnable avisoSemLeitura = () -> {
        if (climaAtual == null) leituraLuminosidade.setText(R.string.sem_sensor_interface);
    };

    @Override protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        banco = BancoLivros.obterInstancia(this);
        listaLivros = findViewById(R.id.lista_livros);
        estadoCatalogo = findViewById(R.id.estado_catalogo);
        medidor = findViewById(R.id.medidor_luminosidade);
        leituraLuminosidade = findViewById(R.id.leitura_luminosidade);
        sensorLuminosidade = new SensorLuminosidade(this, this::receberLuminosidade);
        abaAtual = estadoSalvo == null ? getIntent().getIntExtra("aba", R.id.menu_descobrir)
                : estadoSalvo.getInt("aba", R.id.menu_descobrir);
        BottomNavigationView navegacao = findViewById(R.id.navegacao);
        navegacao.setSelectedItemId(abaAtual);
        navegacao.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_historico) {
                startActivity(new Intent(this, HistoricoActivity.class));
                return false;
            }
            abaAtual = item.getItemId();
            carregarLivros();
            return true;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars());
            tela.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return margens;
        });
    }

    @Override protected void onNewIntent(Intent destino) {
        super.onNewIntent(destino);
        setIntent(destino);
        ((BottomNavigationView) findViewById(R.id.navegacao)).setSelectedItemId(
                destino.getIntExtra("aba", R.id.menu_descobrir));
    }

    @Override protected void onResume() {
        super.onResume();
        climaAtual = null;
        ultimaLuminosidade = null;
        medidor.definirClima(null);
        leituraLuminosidade.setText(R.string.medidor_aguardando);
        if (sensorLuminosidade.iniciar()) temporizador.postDelayed(avisoSemLeitura, 5000);
        else leituraLuminosidade.setText(R.string.sem_sensor_interface);
        carregarLivros();
    }

    @Override protected void onPause() {
        sensorLuminosidade.parar();
        temporizador.removeCallbacks(avisoSemLeitura);
        super.onPause();
    }

    @Override protected void onSaveInstanceState(Bundle estado) {
        estado.putInt("aba", abaAtual);
        super.onSaveInstanceState(estado);
    }

    private void receberLuminosidade(float lux) {
        ultimaLuminosidade = lux;
        temporizador.removeCallbacks(avisoSemLeitura);
        Book.Mood novo = ClassificadorLuminosidade.classificarSensor(lux, climaAtual);
        leituraLuminosidade.setText(getString(R.string.leitura_sensor, lux,
                getString(BookDetailsActivity.rotuloClima(novo))));
        if (novo != climaAtual) {
            climaAtual = novo;
            medidor.definirClima(novo);
            atualizarCatalogo();
        }
    }

    private void carregarLivros() {
        boolean favoritos = abaAtual == R.id.menu_favoritos;
        boolean biblioteca = abaAtual == R.id.menu_biblioteca;
        ((TextView) findViewById(R.id.titulo_inicio)).setText(favoritos ? R.string.titulo_favoritos_novo
                : biblioteca ? R.string.titulo_biblioteca : R.string.titulo_descobrir);
        ((TextView) findViewById(R.id.subtitulo_inicio)).setText(favoritos ? R.string.subtitulo_favoritos
                : biblioteca ? R.string.subtitulo_biblioteca : R.string.subtitulo_descobrir);
        findViewById(R.id.cartao_ambiente).setVisibility(!favoritos && !biblioteca ? View.VISIBLE : View.GONE);
        int consulta = ++versaoConsulta;
        livrosCarregados = null;
        listaLivros.removeAllViews();
        estadoCatalogo.setVisibility(View.VISIBLE);
        estadoCatalogo.setText(R.string.carregando_livros);
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                List<Book> livros = banco.listar(favoritos);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing() || consulta != versaoConsulta) return;
                    livrosCarregados = livros;
                    atualizarCatalogo();
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && consulta == versaoConsulta) estadoCatalogo.setText(R.string.erro_banco);
                });
            }
        });
    }

    private void atualizarCatalogo() {
        if (livrosCarregados == null) return;
        List<Book> livros = abaAtual == R.id.menu_descobrir && climaAtual != null
                ? ClassificadorLuminosidade.recomendar(livrosCarregados, climaAtual) : livrosCarregados;
        listaLivros.removeAllViews();
        estadoCatalogo.setText(R.string.nenhum_favorito);
        estadoCatalogo.setVisibility(livros.isEmpty() ? View.VISIBLE : View.GONE);
        for (Book livro : livros) {
            View item = getLayoutInflater().inflate(R.layout.item_book, listaLivros, false);
            CarregadorCapas.carregar(item.findViewById(R.id.capa_livro), livro.getUrlCapa());
            ((TextView) item.findViewById(R.id.titulo_livro)).setText(livro.getTitle());
            ((TextView) item.findViewById(R.id.autor_livro)).setText(livro.getAuthor());
            ((TextView) item.findViewById(R.id.clima_livro)).setText(BookDetailsActivity.rotuloClima(livro.getMood()));
            item.findViewById(R.id.botao_detalhes).setOnClickListener(tela -> {
                Intent destino = new Intent(this, BookDetailsActivity.class);
                destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, livro.getId());
                if (ultimaLuminosidade != null && climaAtual != null) {
                    destino.putExtra(SalvarHistoricoActivity.EXTRA_LUX, ultimaLuminosidade.floatValue());
                    destino.putExtra(SalvarHistoricoActivity.EXTRA_CLIMA, climaAtual.name());
                }
                startActivity(destino);
            });
            listaLivros.addView(item);
        }
        aplicarCores();
    }
}
