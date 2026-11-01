package com.example.projetofinal;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.example.projetofinal.data.BancoLivros;
import com.example.projetofinal.localizacao.Localizador;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.model.RegistroHistorico;
import java.util.UUID;

public class SalvarHistoricoActivity extends AppCompatActivity {
    public static final String EXTRA_LUX = "luminosidade";
    public static final String EXTRA_CLIMA = "clima";
    public static final String EXTRA_SIMULADA = "simulada";
    private BancoLivros banco;
    private Localizador localizador;
    private MaterialButton botaoSalvar;
    private MaterialCheckBox incluirLocal;
    private TextView situacao;
    private String idRegistro;
    private String idLivro;
    private boolean aguardandoLocal;
    private boolean salvo;
    private final ActivityResultLauncher<String[]> pedirPermissao = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), permissoes -> {
                if (!aguardandoLocal || isFinishing() || isDestroyed()) return;
                if (localizador.temPermissao()) buscarLocal();
                else gravar(null, R.string.salvo_permissao_negada);
            });

    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salvar_historico);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.principal), (tela, margens) -> {
            Insets barras = margens.getInsets(WindowInsetsCompat.Type.systemBars());
            tela.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return margens;
        });
        idRegistro = estado == null ? UUID.randomUUID().toString() : estado.getString("id_registro");
        idLivro = getIntent().getStringExtra(BookDetailsActivity.EXTRA_ID_LIVRO);
        banco = BancoLivros.obterInstancia(this);
        localizador = new Localizador(this);
        botaoSalvar = findViewById(R.id.botao_salvar_historico);
        incluirLocal = findViewById(R.id.incluir_localizacao);
        situacao = findViewById(R.id.estado_registro);
        TextView contextoRegistro = findViewById(R.id.contexto_registro);
        String clima = getIntent().getStringExtra(EXTRA_CLIMA);
        if (getIntent().hasExtra(EXTRA_LUX) && clima != null) {
            contextoRegistro.setText(getString(getIntent().getBooleanExtra(EXTRA_SIMULADA, false)
                    ? R.string.leitura_simulada : R.string.leitura_sensor,
                    getIntent().getFloatExtra(EXTRA_LUX, 0),
                    getString(BookDetailsActivity.rotuloClima(Book.Mood.valueOf(clima)))));
        } else {
            contextoRegistro.setText(R.string.sem_leitura_registro);
        }
        findViewById(R.id.botao_voltar).setOnClickListener(tela -> finish());
        findViewById(R.id.botao_ver_historico).setOnClickListener(tela ->
                startActivity(new Intent(this, HistoricoActivity.class)));
        botaoSalvar.setOnClickListener(tela -> iniciarRegistro());
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                Book livro = idLivro == null ? null : banco.buscar(idLivro);
                boolean existente = banco.existeRegistro(idRegistro);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    if (livro == null) {
                        situacao.setText(R.string.book_not_found);
                        return;
                    }
                    ((TextView) findViewById(R.id.titulo_livro)).setText(livro.getTitle());
                    salvo = existente;
                    botaoSalvar.setEnabled(!salvo);
                    incluirLocal.setEnabled(!salvo);
                    situacao.setText(salvo ? R.string.registro_ja_salvo : R.string.explicacao_registro);
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && !isFinishing()) situacao.setText(R.string.erro_historico);
                });
            }
        });
    }

    private void iniciarRegistro() {
        if (salvo) return;
        botaoSalvar.setEnabled(false);
        incluirLocal.setEnabled(false);
        if (!incluirLocal.isChecked()) {
            gravar(null, R.string.salvo_sem_local);
        } else {
            aguardandoLocal = true;
            if (localizador.temPermissao()) buscarLocal();
            else pedirPermissao.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void buscarLocal() {
        situacao.setText(R.string.buscando_localizacao);
        localizador.buscar(local -> gravar(local, local == null
                ? R.string.salvo_local_indisponivel : R.string.salvo_com_local));
    }

    private void gravar(Location local, int mensagemSucesso) {
        aguardandoLocal = false;
        situacao.setText(R.string.salvando_historico);
        Intent origem = getIntent();
        Float lux = origem.hasExtra(EXTRA_LUX) ? origem.getFloatExtra(EXTRA_LUX, 0) : null;
        RegistroHistorico registro = new RegistroHistorico(idRegistro, idLivro, "", System.currentTimeMillis(),
                lux, origem.getStringExtra(EXTRA_CLIMA), origem.getBooleanExtra(EXTRA_SIMULADA, false),
                local == null ? null : local.getLatitude(), local == null ? null : local.getLongitude(),
                local == null ? null : local.getAccuracy());
        BancoLivros.EXECUTOR.execute(() -> {
            try {
                banco.salvarRegistro(registro);
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    salvo = true;
                    situacao.setText(mensagemSucesso);
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    situacao.setText(R.string.erro_historico);
                    botaoSalvar.setEnabled(true);
                    incluirLocal.setEnabled(true);
                });
            }
        });
    }

    @Override protected void onSaveInstanceState(Bundle estado) {
        estado.putString("id_registro", idRegistro);
        super.onSaveInstanceState(estado);
    }

    @Override protected void onStop() {
        localizador.cancelar();
        if (aguardandoLocal) {
            aguardandoLocal = false;
            situacao.setText(R.string.localizacao_cancelada);
            botaoSalvar.setEnabled(true);
            incluirLocal.setEnabled(true);
        }
        super.onStop();
    }
}
