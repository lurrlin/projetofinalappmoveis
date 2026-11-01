package com.example.projetofinal.localizacao;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import android.os.CancellationSignal;
import androidx.core.util.Consumer;
import java.util.ArrayList;
import java.util.List;

/** Busca pontual em primeiro plano; termina na primeira posição recente ou após 20 s. */
public final class Localizador {
    private final Context contexto;
    private final LocationManager gerenciador;
    private final Handler temporizador = new Handler(Looper.getMainLooper());
    private final List<CancellationSignal> sinais = new ArrayList<>();
    private Consumer<Location> retorno;
    private final Runnable limite = () -> concluir(null);

    public Localizador(Context contexto) {
        this.contexto = contexto;
        gerenciador = (LocationManager) contexto.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean temPermissao() {
        return ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void buscar(Consumer<Location> aoConcluir) {
        cancelar();
        retorno = aoConcluir;
        if (!temPermissao() || gerenciador == null) {
            concluir(null);
            return;
        }
        temporizador.postDelayed(limite, 20000);
        solicitar(LocationManager.NETWORK_PROVIDER);
        if (retorno != null && ContextCompat.checkSelfPermission(contexto,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            solicitar(LocationManager.GPS_PROVIDER);
        }
        if (sinais.isEmpty() && retorno != null) concluir(null);
    }

    private void solicitar(String provedor) {
        if (retorno == null) return;
        CancellationSignal sinal = new CancellationSignal();
        try {
            if (!gerenciador.isProviderEnabled(provedor)) return;
            sinais.add(sinal);
            LocationManagerCompat.getCurrentLocation(gerenciador, provedor, sinal,
                    ContextCompat.getMainExecutor(contexto), local -> {
                        if (retorno == null || sinal.isCanceled() || local == null) return;
                        long idade = SystemClock.elapsedRealtimeNanos() - local.getElapsedRealtimeNanos();
                        if (idade >= 0 && idade <= 30_000_000_000L && local.hasAccuracy()) concluir(local);
                    });
        } catch (SecurityException | IllegalArgumentException erro) {
            sinais.remove(sinal);
            sinal.cancel();
        }
    }

    private void concluir(Location local) {
        Consumer<Location> resposta = retorno;
        cancelar();
        if (resposta != null) resposta.accept(local);
    }

    public void cancelar() {
        retorno = null;
        temporizador.removeCallbacks(limite);
        for (CancellationSignal sinal : sinais) sinal.cancel();
        sinais.clear();
    }
}
