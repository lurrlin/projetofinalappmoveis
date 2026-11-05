package com.example.projetofinal.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

/** Capa própria: recorte arredondado, lombada, alternativa desenhada e marcador de favorito. */
public final class CapaLivroView extends AppCompatImageView {
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path caminho = new Path();
    private final RectF limites = new RectF();
    private int corRealce = Color.rgb(241, 204, 104);
    private boolean favorito;

    public CapaLivroView(Context contexto, AttributeSet atributos) {
        super(contexto, atributos);
        setScaleType(ScaleType.FIT_CENTER);
    }

    public void definirRealce(int cor) { corRealce = cor; invalidate(); }
    public void definirFavorito(boolean valor) { favorito = valor; invalidate(); }

    @Override protected void onMeasure(int largura, int altura) {
        super.onMeasure(largura, altura);
        if (MeasureSpec.getMode(altura) != MeasureSpec.EXACTLY) {
            setMeasuredDimension(getMeasuredWidth(), resolveSize(Math.round(getMeasuredWidth() * 1.45f), altura));
        }
    }

    @Override protected void onDraw(Canvas tela) {
        float densidade = getResources().getDisplayMetrics().density;
        float raio = 8 * densidade;
        float largura = getWidth(), altura = getHeight();
        limites.set(0, 0, largura, altura);
        caminho.reset();
        caminho.addRoundRect(limites, raio, raio, Path.Direction.CW);
        int estado = tela.save();
        tela.clipPath(caminho);
        tela.drawColor(Color.rgb(35, 39, 44));
        if (getDrawable() == null) {
            // Desenho original, visível offline e durante o carregamento.
            pincel.setColor((corRealce & 0xffffff) | 0x33000000);
            tela.drawRect(largura * .08f, 0, largura * .12f, altura, pincel);
            pincel.setColor(corRealce);
            pincel.setStrokeWidth(2 * densidade);
            float esquerda = largura * .28f, direita = largura * .78f;
            for (int linha = 0; linha < 3; linha++) {
                float y = altura * (.40f + linha * .07f);
                tela.drawLine(esquerda, y, direita - linha * largura * .06f, y, pincel);
            }
            pincel.setColor(Color.rgb(170, 176, 183));
            tela.drawLine(esquerda, altura * .70f, largura * .53f, altura * .70f, pincel);
        } else {
            super.onDraw(tela);
        }
        pincel.setColor(0x22000000);
        tela.drawRect(0, 0, 4 * densidade, altura, pincel);
        if (favorito) {
            pincel.setColor(corRealce);
            caminho.reset();
            caminho.moveTo(largura - 28 * densidade, 0);
            caminho.lineTo(largura - 10 * densidade, 0);
            caminho.lineTo(largura - 10 * densidade, 30 * densidade);
            caminho.lineTo(largura - 19 * densidade, 24 * densidade);
            caminho.lineTo(largura - 28 * densidade, 30 * densidade);
            caminho.close();
            tela.drawPath(caminho, pincel);
        }
        tela.restoreToCount(estado);
    }
}
