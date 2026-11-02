package com.example.projetofinal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
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
import com.example.projetofinal.luminosidade.ClassificadorLuminosidade;
import com.example.projetofinal.luminosidade.SensorLuminosidade;
import com.example.projetofinal.view.MedidorLuminosidade;
import java.util.List;
import com.example.projetofinal.ui.CarregadorCapas;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BancoLivros banco;
    private LinearLayout listaLivros;
    private MaterialCheckBox filtroFavoritos;
    private TextView estadoCatalogo;
    private int versaoConsulta;
    private SensorLuminosidade sensorLuminosidade;
    private MedidorLuminosidade medidor;
    private MaterialCheckBox modoManual;
    private MaterialCheckBox filtroClima;
    private SeekBar controleLuminosidade;
    private TextView origemLuminosidade;
    private TextView leituraLuminosidade;
    private Book.Mood climaAtual;
    private Float ultimaLuminosidade;
    private List<Book> livrosCarregados;
    private boolean emPrimeiroPlano;
    private final Handler temporizador = new Handler(Looper.getMainLooper());
    private final Runnable avisoSemLeitura = () -> {
        if (climaAtual == null && emPrimeiroPlano && !modoManual.isChecked()) {
            origemLuminosidade.setText(R.string.sensor_sem_leitura);
        }
    };

    @Override protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        banco = BancoLivros.obterInstancia(this);
        listaLivros = findViewById(R.id.lista_livros);
        filtroFavoritos = findViewById(R.id.filtro_favoritos);
        estadoCatalogo = findViewById(R.id.estado_catalogo);
        findViewById(R.id.botao_ajustar_luz).setOnClickListener(tela -> {
            View ajustes = findViewById(R.id.painel_ajustes_luz);
            ajustes.setVisibility(ajustes.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        BottomNavigationView navegacao = findViewById(R.id.navegacao);
        navegacao.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_historico) {
                startActivity(new Intent(this, HistoricoActivity.class));
                return false;
            }
            boolean favoritos = item.getItemId() == R.id.menu_favoritos;
            if (favoritos) filtroClima.setChecked(false);
            filtroFavoritos.setChecked(favoritos);
            ((TextView) findViewById(R.id.titulo_inicio)).setText(favoritos
                    ? R.string.favoritos_titulo : R.string.titulo_descobrir);
            return true;
        });
        medidor = findViewById(R.id.medidor_luminosidade);
        modoManual = findViewById(R.id.modo_manual);
        filtroClima = findViewById(R.id.filtro_clima);
        controleLuminosidade = findViewById(R.id.controle_luminosidade);
        origemLuminosidade = findViewById(R.id.origem_luminosidade);
        leituraLuminosidade = findViewById(R.id.leitura_luminosidade);
        sensorLuminosidade = new SensorLuminosidade(this, lux -> {
            if (emPrimeiroPlano && !modoManual.isChecked()) receberLuminosidade(lux, false);
        });
        modoManual.setChecked(!sensorLuminosidade.disponivel()
                || (estadoSalvo != null && estadoSalvo.getBoolean("modo_manual")));
        modoManual.setEnabled(sensorLuminosidade.disponivel());
        controleLuminosidade.setProgress(estadoSalvo == null ? 200
                : estadoSalvo.getInt("lux_manual", 200));
        filtroClima.setChecked(estadoSalvo == null || estadoSalvo.getBoolean("filtrar_clima", true));
        if (estadoSalvo != null) {
            filtroFavoritos.setChecked(estadoSalvo.getBoolean("somente_favoritos"));
            findViewById(R.id.painel_ajustes_luz).setVisibility(
                    estadoSalvo.getBoolean("ajustes_abertos") ? View.VISIBLE : View.GONE);
        }
        filtroFavoritos.setOnCheckedChangeListener((botao, marcado) -> carregarLivros());
        filtroClima.setOnCheckedChangeListener((botao, marcado) -> atualizarCatalogo());
        modoManual.setOnCheckedChangeListener((botao, marcado) -> configurarLuminosidade());
        controleLuminosidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar barra, int valor, boolean peloUsuario) {
                if (modoManual.isChecked() && emPrimeiroPlano) receberLuminosidade(valor, true);
            }
            @Override public void onStartTrackingTouch(SeekBar barra) { }
            @Override public void onStopTrackingTouch(SeekBar barra) { }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars());
            tela.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return margens;
        });
    }

    @Override protected void onResume() {
        super.onResume();
        emPrimeiroPlano = true;
        configurarLuminosidade();
        carregarLivros();
    }

    @Override protected void onSaveInstanceState(Bundle estado) {
        estado.putBoolean("somente_favoritos", filtroFavoritos.isChecked());
        estado.putBoolean("ajustes_abertos", findViewById(R.id.painel_ajustes_luz).getVisibility() == View.VISIBLE);
        estado.putBoolean("modo_manual", modoManual.isChecked());
        estado.putBoolean("filtrar_clima", filtroClima.isChecked());
        estado.putInt("lux_manual", controleLuminosidade.getProgress());
        super.onSaveInstanceState(estado);
    }

    @Override protected void onPause() {
        emPrimeiroPlano = false;
        sensorLuminosidade.parar();
        temporizador.removeCallbacks(avisoSemLeitura);
        super.onPause();
    }

    private void configurarLuminosidade() {
        sensorLuminosidade.parar();
        temporizador.removeCallbacks(avisoSemLeitura);
        climaAtual = null;
        ultimaLuminosidade = null;
        medidor.definirClima(null);
        leituraLuminosidade.setText(R.string.medidor_aguardando);
        controleLuminosidade.setVisibility(modoManual.isChecked() ? View.VISIBLE : View.GONE);
        atualizarCatalogo();
        if (!emPrimeiroPlano) return;
        if (modoManual.isChecked()) {
            origemLuminosidade.setText(sensorLuminosidade.disponivel()
                    ? R.string.origem_manual : R.string.sensor_indisponivel);
            receberLuminosidade(controleLuminosidade.getProgress(), true);
        } else {
            origemLuminosidade.setText(R.string.origem_sensor);
            if (sensorLuminosidade.iniciar()) {
                temporizador.postDelayed(avisoSemLeitura, 5000);
            } else {
                modoManual.setChecked(true);
                origemLuminosidade.setText(R.string.sensor_falhou);
            }
        }
    }

    private void receberLuminosidade(float lux, boolean simulada) {
        ultimaLuminosidade = lux;
        temporizador.removeCallbacks(avisoSemLeitura);
        Book.Mood novoClima = simulada ? ClassificadorLuminosidade.classificar(lux)
                : ClassificadorLuminosidade.classificarSensor(lux, climaAtual);
        if (!simulada) origemLuminosidade.setText(R.string.origem_sensor);
        leituraLuminosidade.setText(getString(simulada ? R.string.leitura_simulada
                : R.string.leitura_sensor, lux, getString(BookDetailsActivity.rotuloClima(novoClima))));
        if (novoClima != climaAtual) {
            climaAtual = novoClima;
            medidor.definirClima(climaAtual);
            atualizarCatalogo();
        }
    }

    private void atualizarCatalogo() {
        if (livrosCarregados == null) return;
        List<Book> livros = filtroClima.isChecked()
                ? ClassificadorLuminosidade.recomendar(livrosCarregados, climaAtual) : livrosCarregados;
        exibirLivros(livros);
    }

    private void carregarLivros() {
        int consultaAtual = ++versaoConsulta;
        boolean somenteFavoritos = filtroFavoritos.isChecked();
        livrosCarregados = null;
        listaLivros.removeAllViews();
        estadoCatalogo.setVisibility(View.VISIBLE);
        estadoCatalogo.setText(R.string.carregando_livros);
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                List<Book> livros = banco.listar(somenteFavoritos);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing() || consultaAtual != versaoConsulta) return;
                    livrosCarregados = livros;
                    atualizarCatalogo();
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
        estadoCatalogo.setText(filtroClima.isChecked()
                ? (climaAtual == null ? R.string.aguardando_recomendacoes : R.string.sem_livros_clima)
                : R.string.nenhum_favorito);
        estadoCatalogo.setVisibility(livros.isEmpty() ? View.VISIBLE : View.GONE);
        for (Book livro : livros) {
            View item = getLayoutInflater().inflate(R.layout.item_book, listaLivros, false);
            CarregadorCapas.carregar(item.findViewById(R.id.capa_livro), livro.getUrlCapa());
            ((TextView) item.findViewById(R.id.titulo_livro)).setText(livro.getTitle());
            ((TextView) item.findViewById(R.id.autor_livro)).setText(livro.getAuthor());
            ((TextView) item.findViewById(R.id.clima_livro)).setText(
                    BookDetailsActivity.rotuloClima(livro.getMood()));
            View detalhes = item.findViewById(R.id.botao_detalhes);
            detalhes.setContentDescription(getString(R.string.details_for, livro.getTitle()));
            detalhes.setOnClickListener(tela -> {
                Intent destino = new Intent(this, BookDetailsActivity.class);
                destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, livro.getId());
                if (ultimaLuminosidade != null && climaAtual != null) {
                    destino.putExtra(SalvarHistoricoActivity.EXTRA_LUX, ultimaLuminosidade.floatValue());
                    destino.putExtra(SalvarHistoricoActivity.EXTRA_CLIMA, climaAtual.name());
                    destino.putExtra(SalvarHistoricoActivity.EXTRA_SIMULADA, modoManual.isChecked());
                }
                startActivity(destino);
            });
            listaLivros.addView(item);
        }
    }
}
