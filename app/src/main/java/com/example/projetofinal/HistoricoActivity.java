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

public class HistoricoActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historico);
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
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    situacao.setText(registros.isEmpty() ? R.string.historico_vazio : R.string.historico_limite);
                    LinearLayout lista = findViewById(R.id.lista_historico);
                    for (RegistroHistorico registro : registros) {
                        TextView item = (TextView) getLayoutInflater().inflate(R.layout.item_historico, lista, false);
                        String data = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                .format(new Date(registro.data));
                        String luz = registro.luminosidade == null ? getString(R.string.sem_leitura_registro)
                                : getString(registro.simulada ? R.string.leitura_simulada : R.string.leitura_sensor,
                                        registro.luminosidade,
                                        getString(BookDetailsActivity.rotuloClima(Book.Mood.valueOf(registro.clima))));
                        String local = registro.latitude == null ? getString(R.string.registro_sem_local)
                                : getString(R.string.coordenadas_registro, registro.latitude,
                                        registro.longitude, registro.precisao);
                        item.setText(getString(R.string.item_historico_texto, registro.titulo, data, luz, local));
                        lista.addView(item);
                    }
                });
            } catch (RuntimeException erro) {
                runOnUiThread(() -> {
                    if (!isDestroyed() && !isFinishing()) situacao.setText(R.string.erro_historico);
                });
            }
        });
    }
}
