package com.example.projetofinal.localizacao;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.core.util.Consumer;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BuscadorEndereco {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private final Context contexto;
    private final Handler principal = new Handler(Looper.getMainLooper());

    public BuscadorEndereco(Context contexto) {
        this.contexto = contexto.getApplicationContext();
    }

    public void buscar(double latitude, double longitude, Consumer<String> retorno) {
        AtomicBoolean concluido = new AtomicBoolean();
        Runnable limite = () -> {
            if (concluido.compareAndSet(false, true)) retorno.accept(null);
        };
        Consumer<String> concluir = endereco -> principal.post(() -> {
            if (concluido.compareAndSet(false, true)) {
                principal.removeCallbacks(limite);
                retorno.accept(endereco);
            }
        });
        principal.postDelayed(limite, 10000);
        EXECUTOR.execute(() -> {
            if (concluido.get()) return;
            try {
                if (!Geocoder.isPresent()) {
                    concluir.accept(null);
                    return;
                }
                Geocoder geocodificador = new Geocoder(contexto, Locale.forLanguageTag("pt-BR"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocodificador.getFromLocation(latitude, longitude, 1, new Geocoder.GeocodeListener() {
                        @Override public void onGeocode(List<Address> enderecos) {
                            concluir.accept(formatar(enderecos));
                        }

                        @Override public void onError(String mensagem) {
                            concluir.accept(null);
                        }
                    });
                } else {
                    concluir.accept(buscarCompativel(geocodificador, latitude, longitude));
                }
            } catch (IOException | RuntimeException erro) {
                concluir.accept(null);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static String buscarCompativel(Geocoder geocodificador, double latitude, double longitude)
            throws IOException {
        return formatar(geocodificador.getFromLocation(latitude, longitude, 1));
    }

    private static String formatar(List<Address> enderecos) {
        if (enderecos == null || enderecos.isEmpty()) return null;
        Address endereco = enderecos.get(0);
        String cidade = endereco.getLocality();
        if (cidade == null || cidade.trim().isEmpty()) cidade = endereco.getSubAdminArea();
        return FormatadorEndereco.formatar(endereco.getSubLocality(), cidade, endereco.getAdminArea());
    }
}
