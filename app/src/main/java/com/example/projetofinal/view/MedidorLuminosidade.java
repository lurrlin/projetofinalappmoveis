package com.example.projetofinal.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.example.projetofinal.R;
import com.example.projetofinal.model.Book;

/** View própria: desenha três arcos e um ponteiro para o clima de leitura atual. */
public final class MedidorLuminosidade extends View {
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arco = new RectF();
    private final int[] cores = {Color.rgb(132, 112, 187), Color.rgb(67, 151, 141),
            Color.rgb(222, 175, 70)};
    private Book.Mood clima;

    public MedidorLuminosidade(Context contexto, AttributeSet atributos) {
        super(contexto, atributos);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription(contexto.getString(R.string.medidor_aguardando));
    }

    public void definirClima(Book.Mood novoClima) {
        clima = novoClima;
        int rotulo = clima == null ? R.string.medidor_aguardando
                : clima == Book.Mood.DARK ? R.string.dark
                : clima == Book.Mood.MEDIUM ? R.string.medium : R.string.light;
        setContentDescription(getContext().getString(R.string.descricao_medidor,
                getContext().getString(rotulo)));
        invalidate();
    }

    @Override protected void onDraw(Canvas tela) {
        super.onDraw(tela);
        float largura = getWidth() - getPaddingLeft() - getPaddingRight();
        float altura = getHeight() - getPaddingTop() - getPaddingBottom();
        // A folga acompanha o raio para não cortar o traço em telas de alta densidade.
        float raio = Math.min(largura / 2.2f, altura / 1.2f);
        if (raio <= 0) return;
        float centroX = getPaddingLeft() + largura / 2f;
        float centroY = getPaddingTop() + altura - raio * 0.1f;
        arco.set(centroX - raio, centroY - raio, centroX + raio, centroY + raio);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(raio * 0.13f);
        pincel.setStrokeCap(Paint.Cap.ROUND);
        for (int indice = 0; indice < cores.length; indice++) {
            pincel.setColor(cores[indice]);
            tela.drawArc(arco, 184 + indice * 60, 52, false, pincel);
        }
        if (clima == null) return;
        int indice = clima == Book.Mood.DARK ? 0 : clima == Book.Mood.MEDIUM ? 1 : 2;
        double angulo = Math.toRadians(210 + indice * 60);
        pincel.setColor(cores[indice]);
        pincel.setStrokeWidth(raio * 0.045f);
        tela.drawLine(centroX, centroY,
                centroX + (float) Math.cos(angulo) * raio * 0.78f,
                centroY + (float) Math.sin(angulo) * raio * 0.78f, pincel);
        pincel.setStyle(Paint.Style.FILL);
        tela.drawCircle(centroX, centroY, raio * 0.07f, pincel);
    }
}
