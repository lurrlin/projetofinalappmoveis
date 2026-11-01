package com.example.projetofinal.luminosidade;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/** Registra somente TYPE_LIGHT e permite desligar a leitura ao sair da tela. */
public final class SensorLuminosidade implements SensorEventListener {
    public interface AoLerLuminosidade {
        void receber(float lux);
    }

    private final SensorManager gerenciador;
    private final Sensor sensor;
    private final AoLerLuminosidade aoLer;
    private boolean ativo;

    public SensorLuminosidade(Context contexto, AoLerLuminosidade aoLer) {
        gerenciador = (SensorManager) contexto.getSystemService(Context.SENSOR_SERVICE);
        sensor = gerenciador == null ? null : gerenciador.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.aoLer = aoLer;
    }

    public boolean disponivel() { return sensor != null; }

    public boolean iniciar() {
        parar();
        if (!disponivel()) return false;
        ativo = gerenciador.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return ativo;
    }

    public void parar() {
        ativo = false;
        if (gerenciador != null) gerenciador.unregisterListener(this);
    }

    @Override public void onSensorChanged(SensorEvent evento) {
        if (ativo && evento.values.length > 0
                && ClassificadorLuminosidade.leituraValida(evento.values[0])) {
            aoLer.receber(evento.values[0]);
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int precisao) { }
}
