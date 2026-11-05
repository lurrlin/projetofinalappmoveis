package com.example.projetofinal.data;

import android.content.Context;
import com.example.projetofinal.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CatalogoOpenLibrary {
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static long ultimaConsulta;
    private static String ultimaBusca = "";
    private static List<Book> ultimoResultado;
    private CatalogoOpenLibrary() { }

    public static List<Book> lerCatalogoInicial(Context contexto) {
        try (InputStream entrada = contexto.getAssets().open("catalogo_open_library.json")) {
            JSONArray itens = new JSONObject(lerTexto(entrada)).getJSONArray("livros");
            List<Book> livros = new ArrayList<>();
            for (int indice = 0; indice < itens.length(); indice++) {
                JSONObject item = itens.getJSONObject(indice);
                livros.add(new Book(item.getString("id"), item.getString("titulo"), item.getString("autor"),
                        Book.Mood.valueOf(item.getString("clima")), item.getString("sinopse"),
                        item.getString("url_capa"), item.getString("genero"), item.getString("chave_open_library")));
            }
            return livros;
        } catch (Exception erro) {
            throw new IllegalStateException("Não foi possível ler o catálogo incluído no aplicativo.", erro);
        }
    }

    public static synchronized List<Book> buscar(String termo) throws Exception {
        long agora = android.os.SystemClock.elapsedRealtime();
        if (termo.equals(ultimaBusca) && ultimoResultado != null && agora - ultimaConsulta < 300000) {
            return new ArrayList<>(ultimoResultado);
        }
        long espera = 1100 - (agora - ultimaConsulta);
        if (espera > 0) Thread.sleep(espera);
        ultimaConsulta = android.os.SystemClock.elapsedRealtime();
        String endereco = "https://openlibrary.org/search.json?limit=12&lang=pt&fields="
                + "key,title,author_name,cover_i,first_publish_year,subject,editions,editions.title&q="
                + URLEncoder.encode(termo, "UTF-8");
        HttpURLConnection conexao = (HttpURLConnection) new URL(endereco).openConnection();
        try {
            conexao.setConnectTimeout(15000);
            conexao.setReadTimeout(20000);
            conexao.setRequestProperty("User-Agent", "LumiBooks/1.0 (Android educational project)");
            if (conexao.getResponseCode() != 200) throw new java.io.IOException("Serviço indisponível.");
            List<Book> livros;
            try (InputStream entrada = conexao.getInputStream()) {
                livros = interpretarBusca(lerTexto(entrada));
            }
            ultimaBusca = termo;
            ultimoResultado = livros;
            return new ArrayList<>(livros);
        } finally {
            conexao.disconnect();
        }
    }

    public static List<Book> interpretarBusca(String resposta) throws Exception {
        JSONArray documentos = new JSONObject(resposta).getJSONArray("docs");
        List<Book> livros = new ArrayList<>();
        java.util.Set<String> chaves = new java.util.HashSet<>();
        for (int indice = 0; indice < documentos.length(); indice++) {
            JSONObject item = documentos.getJSONObject(indice);
            String chave = item.optString("key");
            String tituloOriginal = item.optString("title").trim();
            if (!chave.matches("/works/OL[0-9]+W") || tituloOriginal.isEmpty() || !chaves.add(chave)) continue;
            String titulo = tituloOriginal;
            JSONObject edicoes = item.optJSONObject("editions");
            JSONArray listaEdicoes = edicoes == null ? null : edicoes.optJSONArray("docs");
            if (listaEdicoes != null && listaEdicoes.length() > 0) {
                titulo = listaEdicoes.getJSONObject(0).optString("title", titulo);
            }
            JSONArray autores = item.optJSONArray("author_name");
            String autor = autores == null || autores.length() == 0 ? "Autoria não informada" : autores.getString(0);
            String assuntos = item.optJSONArray("subject") == null ? "" : item.getJSONArray("subject").toString().toLowerCase(Locale.ROOT);
            String genero = classificarGenero(assuntos);
            Book.Mood clima = genero.equals("Terror") || genero.equals("Suspense") ? Book.Mood.DARK
                    : genero.equals("Fantasia") || genero.equals("Romance") || genero.equals("Infantil") ? Book.Mood.LIGHT : Book.Mood.MEDIUM;
            int capa = item.optInt("cover_i", 0);
            String id = idOriginal(tituloOriginal, chave.substring(7));
            livros.add(new Book(id, titulo, autor, clima,
                    genero + " • Primeira publicação: " + item.optString("first_publish_year", "não informada")
                    + ". Sinopse não disponível. Saiba mais na ficha do livro.",
                    capa <= 0 ? "" : "https://covers.openlibrary.org/b/id/" + capa + "-M.jpg?default=false", genero, chave));
        }
        return livros;
    }

    private static String idOriginal(String titulo, String alternativa) {
        switch (titulo.toLowerCase(Locale.ROOT)) {
            case "the hobbit": return "o-hobbit";
            case "harry potter and the philosopher's stone": return "harry-potter-1";
            case "the adventures of sherlock holmes": return "sherlock";
            case "adventures of sherlock holmes": return "sherlock";
            case "dom casmurro": return "dom-casmurro";
            case "the shining": return "o-iluminado";
            case "dracula": return "dracula";
            default: return alternativa;
        }
    }

    private static String classificarGenero(String assuntos) {
        if (assuntos.contains("horror") || assuntos.contains("terror")) return "Terror";
        if (assuntos.contains("thriller")) return "Suspense";
        if (assuntos.contains("science fiction")) return "Ficção científica";
        if (assuntos.contains("fantasy")) return "Fantasia";
        if (assuntos.contains("mystery") || assuntos.contains("detective")) return "Mistério";
        if (assuntos.contains("romance") || assuntos.contains("love stories")) return "Romance";
        if (assuntos.contains("philosophy")) return "Filosofia";
        if (assuntos.contains("history")) return "História";
        if (assuntos.contains("children")) return "Infantil";
        return "Literatura";
    }

    private static String lerTexto(InputStream entrada) throws Exception {
        ByteArrayOutputStream dados = new ByteArrayOutputStream();
        byte[] trecho = new byte[8192];
        int quantidade;
        while ((quantidade = entrada.read(trecho)) != -1) {
            if (dados.size() + quantidade > 4 * 1024 * 1024) throw new java.io.IOException("Resposta muito grande.");
            dados.write(trecho, 0, quantidade);
        }
        return dados.toString(StandardCharsets.UTF_8.name());
    }
}
