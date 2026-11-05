package com.example.projetofinal.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CarregadorCapas {
    private static final ExecutorService FILA = Executors.newSingleThreadExecutor();
    private static final Handler INTERFACE = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> MEMORIA = new LruCache<String, Bitmap>(4 * 1024 * 1024) {
        @Override protected int sizeOf(String chave, Bitmap imagem) { return imagem.getByteCount(); }
    };
    private static long ultimaRequisicao;
    private CarregadorCapas() { }

    public static void carregar(ImageView imagem, String endereco) {
        imagem.setImageDrawable(null);
        imagem.setTag(endereco);
        if (endereco == null || endereco.isEmpty()) return;
        Bitmap existente = MEMORIA.get(endereco);
        if (existente != null) { imagem.setImageBitmap(existente); return; }
        WeakReference<ImageView> referencia = new WeakReference<>(imagem);
        File arquivo = new File(imagem.getContext().getCacheDir(), "capa_" + Integer.toHexString(endereco.hashCode()) + ".jpg");
        FILA.execute(() -> {
            ImageView solicitante = referencia.get();
            if (solicitante == null || !endereco.equals(solicitante.getTag())) return;
            Bitmap capa = MEMORIA.get(endereco);
            if (capa == null && arquivo.isFile()) capa = BitmapFactory.decodeFile(arquivo.getAbsolutePath());
            if (capa == null) {
                HttpURLConnection conexao = null;
                try {
                    URL url = new URL(endereco);
                    if (!"https".equals(url.getProtocol()) || !"covers.openlibrary.org".equals(url.getHost())) return;
                    long espera = 1100 - (android.os.SystemClock.elapsedRealtime() - ultimaRequisicao);
                    if (espera > 0) Thread.sleep(espera);
                    ultimaRequisicao = android.os.SystemClock.elapsedRealtime();
                    conexao = (HttpURLConnection) url.openConnection();
                    conexao.setConnectTimeout(15000);
                    conexao.setReadTimeout(15000);
                    conexao.setRequestProperty("User-Agent", "LumiBooks/1.0 (Android educational project)");
                    if (conexao.getResponseCode() != 200) return;
                    ByteArrayOutputStream dados = new ByteArrayOutputStream();
                    try (InputStream entrada = conexao.getInputStream()) {
                        byte[] trecho = new byte[8192];
                        int quantidade;
                        while ((quantidade = entrada.read(trecho)) != -1) {
                            if (dados.size() + quantidade > 2 * 1024 * 1024) return;
                            dados.write(trecho, 0, quantidade);
                        }
                    }
                    byte[] bytes = dados.toByteArray();
                    BitmapFactory.Options medidas = new BitmapFactory.Options();
                    medidas.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, medidas);
                    medidas.inSampleSize = 1;
                    while (medidas.outWidth / medidas.inSampleSize > 600 || medidas.outHeight / medidas.inSampleSize > 900) medidas.inSampleSize *= 2;
                    medidas.inJustDecodeBounds = false;
                    capa = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, medidas);
                    if (capa != null) {
                        try (FileOutputStream saida = new FileOutputStream(arquivo)) {
                            capa.compress(Bitmap.CompressFormat.JPEG, 90, saida);
                        }
                    }
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception erro) {
                    android.util.Log.w("LumiBooksCapas", "Não foi possível carregar a capa", erro);
                } finally {
                    if (conexao != null) conexao.disconnect();
                }
            }
            if (capa == null) return;
            MEMORIA.put(endereco, capa);
            Bitmap resultado = capa;
            INTERFACE.post(() -> {
                ImageView destino = referencia.get();
                if (destino != null && endereco.equals(destino.getTag())) destino.setImageBitmap(resultado);
            });
        });
    }
}
