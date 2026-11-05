package com.example.projetofinal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.data.CatalogoOpenLibrary;
import com.example.projetofinal.luminosidade.ClassificadorLuminosidade;
import com.example.projetofinal.luminosidade.SensorLuminosidade;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.ui.AtividadeTematica;
import com.example.projetofinal.ui.EstanteAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AtividadeTematica {
    private BancoLivros banco;
    private RecyclerView listaLivros;
    private EstanteAdapter estante;
    private TextView estadoCatalogo;
    private TextInputEditText busca;
    private int versaoConsulta;
    private int abaAtual = R.id.menu_descobrir;
    private SensorLuminosidade sensorLuminosidade;
    private TextView leituraLuminosidade;
    private Book.Mood climaAtual;
    private List<Book> livrosCarregados;
    private boolean buscandoOnline;
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
        int colunas = getResources().getConfiguration().fontScale > 1.35f ? 1
                : getResources().getConfiguration().screenWidthDp >= 600 ? 3 : 2;
        listaLivros.setLayoutManager(new GridLayoutManager(this, colunas));
        estante = new EstanteAdapter(this::abrirLivro);
        listaLivros.setAdapter(estante);
        estadoCatalogo = findViewById(R.id.estado_catalogo);
        leituraLuminosidade = findViewById(R.id.leitura_luminosidade);
        busca = findViewById(R.id.busca_livros);
        if (estadoSalvo != null) busca.setText(estadoSalvo.getString("busca", ""));
        busca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence texto, int inicio, int quantidade, int depois) { }
            @Override public void onTextChanged(CharSequence texto, int inicio, int antes, int quantidade) { atualizarCatalogo(); }
            @Override public void afterTextChanged(Editable texto) { }
        });
        findViewById(R.id.botao_buscar_online).setOnClickListener(vista -> buscarOnline());
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
            busca.setText("");
            listaLivros.scrollToPosition(0);
            carregarLivros();
            return true;
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
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
        estado.putString("busca", busca.getText() == null ? "" : busca.getText().toString());
        super.onSaveInstanceState(estado);
    }

    private void receberLuminosidade(float lux) {
        temporizador.removeCallbacks(avisoSemLeitura);
        Book.Mood novo = ClassificadorLuminosidade.classificarSensor(lux, climaAtual);
        leituraLuminosidade.setText(novo == Book.Mood.DARK ? R.string.ambiente_escuro
                : novo == Book.Mood.LIGHT ? R.string.ambiente_claro : R.string.ambiente_medio);
        if (novo != climaAtual) {
            climaAtual = novo;
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
        leituraLuminosidade.setVisibility(!favoritos && !biblioteca ? View.VISIBLE : View.GONE);
        findViewById(R.id.botao_buscar_online).setVisibility(biblioteca ? View.VISIBLE : View.GONE);
        int consulta = ++versaoConsulta;
        livrosCarregados = null;
        estante.atualizar(new ArrayList<>(), favoritos);
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
        List<Book> selecao = abaAtual == R.id.menu_descobrir && climaAtual != null
                ? ClassificadorLuminosidade.recomendar(livrosCarregados, climaAtual) : livrosCarregados;
        String termo = normalizar(busca.getText() == null ? "" : busca.getText().toString().trim());
        List<Book> livros = new ArrayList<>();
        for (Book livro : selecao) {
            if (normalizar(livro.getTitle() + " " + livro.getAuthor() + " " + livro.getGenero()).contains(termo)) livros.add(livro);
        }
        estante.atualizar(livros, abaAtual == R.id.menu_favoritos);
        estadoCatalogo.setText(livros.isEmpty() ? getString(abaAtual == R.id.menu_favoritos && termo.isEmpty()
                ? R.string.nenhum_favorito : R.string.busca_vazia) : getString(R.string.total_livros, livros.size()));
        aplicarCores();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private void abrirLivro(Book livro) {
        Intent destino = new Intent(this, BookDetailsActivity.class);
        destino.putExtra(BookDetailsActivity.EXTRA_ID_LIVRO, livro.getId());
        startActivity(destino);
    }

    private void buscarOnline() {
        if (buscandoOnline) return;
        String termo = busca.getText() == null ? "" : busca.getText().toString().trim();
        if (termo.length() < 3) {
            busca.setError(getString(R.string.busca_minima));
            busca.requestFocus();
            return;
        }
        buscandoOnline = true;
        atualizarBotaoBusca();
        CatalogoOpenLibrary.EXECUTOR.execute(() -> {
            try {
                List<Book> encontrados = CatalogoOpenLibrary.buscar(termo);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    buscandoOnline = false;
                    atualizarBotaoBusca();
                    if (encontrados.isEmpty()) {
                        Toast.makeText(this, R.string.busca_online_vazia, Toast.LENGTH_LONG).show();
                        return;
                    }
                    mostrarImportacao(encontrados);
                });
            } catch (Exception erro) {
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    buscandoOnline = false;
                    atualizarBotaoBusca();
                    Toast.makeText(this, R.string.erro_open_library, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void atualizarBotaoBusca() {
        com.google.android.material.button.MaterialButton botao = findViewById(R.id.botao_buscar_online);
        botao.setEnabled(!buscandoOnline);
        botao.setText(buscandoOnline ? R.string.buscando_open_library : R.string.buscar_open_library);
    }

    private void mostrarImportacao(List<Book> encontrados) {
        String[] titulos = new String[encontrados.size()];
        boolean[] escolhidos = new boolean[encontrados.size()];
        for (int indice = 0; indice < encontrados.size(); indice++) {
            Book livro = encontrados.get(indice);
            titulos[indice] = livro.getTitle() + " — " + livro.getAuthor();
        }
        androidx.appcompat.app.AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.escolher_importacao)
                .setMultiChoiceItems(titulos, escolhidos, (janela, indice, marcado) -> escolhidos[indice] = marcado)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.adicionar_biblioteca, (janela, botao) -> {
                    List<Book> selecionados = new ArrayList<>();
                    for (int indice = 0; indice < escolhidos.length; indice++) {
                        if (escolhidos[indice]) selecionados.add(encontrados.get(indice));
                    }
                    if (selecionados.isEmpty()) return;
                    BancoLivros.EXECUTOR.execute(() -> {
                        try {
                            int quantidade = banco.importarLivros(selecionados);
                            runOnUiThread(() -> {
                                if (isDestroyed() || isFinishing()) return;
                                busca.setText("");
                                carregarLivros();
                                Toast.makeText(this, getString(R.string.livros_importados, quantidade), Toast.LENGTH_LONG).show();
                            });
                        } catch (RuntimeException erro) {
                            runOnUiThread(() -> {
                                if (!isDestroyed() && !isFinishing()) Toast.makeText(this, R.string.erro_banco, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                }).create();
        dialogo.setOnShowListener(janela -> {
            dialogo.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(corAtual());
            dialogo.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(corAtual());
        });
        dialogo.show();
    }
}
