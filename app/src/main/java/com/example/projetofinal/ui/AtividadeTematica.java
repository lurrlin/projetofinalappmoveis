package com.example.projetofinal.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.projetofinal.R;
import com.example.projetofinal.MainActivity;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.luminosidade.SensorLuminosidade;
import com.example.projetofinal.luminosidade.ClassificadorLuminosidade;

public abstract class AtividadeTematica extends AppCompatActivity {
    private static Book.Mood clima;
    private SensorLuminosidade sensorTema;

    @Override protected void onResume() {
        super.onResume();
        androidx.core.view.WindowInsetsControllerCompat barras =
                androidx.core.view.WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        barras.setAppearanceLightStatusBars(false);
        barras.setAppearanceLightNavigationBars(false);
        aplicarCores();
        sensorTema = new SensorLuminosidade(this, lux -> {
            Book.Mood novo = ClassificadorLuminosidade.classificarSensor(lux, clima);
            if (novo != clima) {
                clima = novo;
                aplicarCores();
            }
        });
        sensorTema.iniciar();
    }

    @Override protected void onPause() {
        if (sensorTema != null) sensorTema.parar();
        super.onPause();
    }

    public static int corAtual() {
        return clima == Book.Mood.DARK ? Color.rgb(255,123,131)
                : clima == Book.Mood.LIGHT ? Color.rgb(114,214,160) : Color.rgb(241,204,104);
    }

    protected void aplicarCores() {
        colorir(findViewById(android.R.id.content), corAtual());
        TextView tituloMarca = findViewById(R.id.titulo_marca);
        if (tituloMarca != null) {
            tituloMarca.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    clima == Book.Mood.DARK ? R.drawable.marca_lumibooks_vermelho
                            : clima == Book.Mood.LIGHT ? R.drawable.marca_lumibooks_verde : R.drawable.marca_lumibooks_amarelo,
                    0, 0, 0);
        }
    }

    private void colorir(View vista, int cor) {
        if (vista instanceof android.widget.TextView && "realce".equals(vista.getTag())) {
            ((android.widget.TextView) vista).setTextColor(cor);
        }
        if (vista instanceof com.example.projetofinal.view.CapaLivroView) {
            ((com.example.projetofinal.view.CapaLivroView) vista).definirRealce(cor);
        }
        if (vista instanceof com.google.android.material.textfield.TextInputLayout) {
            ((com.google.android.material.textfield.TextInputLayout) vista).setBoxStrokeColor(cor);
        }
        if (vista instanceof com.google.android.material.checkbox.MaterialCheckBox) {
            ((com.google.android.material.checkbox.MaterialCheckBox) vista).setButtonTintList(
                    new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                            new int[]{cor, Color.rgb(174,181,190)}));
        }
        if (vista instanceof MaterialButton) {
            MaterialButton botao = (MaterialButton) vista;
            int fundo = botao.getBackgroundTintList() == null ? Color.TRANSPARENT
                    : botao.getBackgroundTintList().getDefaultColor();
            boolean contorno = botao.getStrokeWidth() > 0 || Color.alpha(fundo) == 0;
            ColorStateList realce = new ColorStateList(
                    new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
                    new int[]{Color.rgb(88,94,105), cor});
            if (contorno) {
                botao.setTextColor(realce);
                if (botao.getStrokeWidth() > 0) botao.setStrokeColor(realce);
            } else {
                botao.setBackgroundTintList(realce);
                botao.setTextColor(Color.rgb(17,20,26));
            }
            botao.setRippleColor(ColorStateList.valueOf((cor & 0x00ffffff) | 0x33000000));
        }
        if (vista instanceof BottomNavigationView) {
            BottomNavigationView barra = (BottomNavigationView) vista;
            ColorStateList cores = new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{cor, Color.rgb(151,158,170)});
            barra.setItemIconTintList(cores);
            barra.setItemTextColor(cores);
            barra.setItemActiveIndicatorColor(ColorStateList.valueOf((cor & 0x00ffffff) | 0x25000000));
        }
        if (vista instanceof ViewGroup) {
            ViewGroup grupo = (ViewGroup) vista;
            for (int indice = 0; indice < grupo.getChildCount(); indice++) colorir(grupo.getChildAt(indice), cor);
        }
    }

    protected void configurarBarraHistorico() {
        BottomNavigationView barra = findViewById(R.id.navegacao);
        barra.setSelectedItemId(R.id.menu_historico);
        barra.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_historico) return true;
            Intent destino = new Intent(this, MainActivity.class);
            destino.putExtra("aba", item.getItemId());
            destino.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(destino);
            finish();
            return false;
        });
    }
}
