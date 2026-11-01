package com.example.projetofinal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.model.RegistroHistorico;
import com.example.projetofinal.luminosidade.ClassificadorLuminosidade;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Acesso local, serializado fora da interface. A instância acompanha a aplicação. */
public final class BancoLivros extends SQLiteOpenHelper {
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static BancoLivros instancia;

    public static synchronized BancoLivros obterInstancia(Context contexto) {
        if (instancia == null) instancia = new BancoLivros(contexto, "lumibooks.db");
        return instancia;
    }

    // Nome separado permite testar sem alterar o banco usado pelo usuário.
    public BancoLivros(Context contexto, String nomeBanco) {
        super(contexto.getApplicationContext(), nomeBanco, null, 2);
    }

    @Override public void onConfigure(SQLiteDatabase banco) {
        banco.setForeignKeyConstraintsEnabled(true);
    }

    @Override public void onCreate(SQLiteDatabase banco) {
        banco.execSQL("CREATE TABLE livros (id TEXT PRIMARY KEY, titulo TEXT NOT NULL, "
                + "autor TEXT NOT NULL, clima TEXT NOT NULL, sinopse TEXT NOT NULL, "
                + "favorito INTEGER NOT NULL DEFAULT 0 CHECK(favorito IN (0, 1)))");
        for (Book livro : BookCatalog.getAll()) {
            ContentValues valores = new ContentValues();
            valores.put("id", livro.getId());
            valores.put("titulo", livro.getTitle());
            valores.put("autor", livro.getAuthor());
            valores.put("clima", livro.getMood().name());
            valores.put("sinopse", livro.getSynopsis());
            banco.insertOrThrow("livros", null, valores);
        }
        criarHistorico(banco);
    }

    @Override public void onUpgrade(SQLiteDatabase banco, int versaoAntiga, int versaoNova) {
        if (versaoAntiga < 2) criarHistorico(banco);
    }

    private void criarHistorico(SQLiteDatabase banco) {
        banco.execSQL("CREATE TABLE historico (id TEXT PRIMARY KEY, id_livro TEXT NOT NULL "
                + "REFERENCES livros(id), data INTEGER NOT NULL, luminosidade REAL, clima TEXT, "
                + "simulada INTEGER NOT NULL, latitude REAL, longitude REAL, precisao REAL)");
        banco.execSQL("CREATE INDEX indice_historico_data ON historico(data DESC)");
    }

    public boolean existeRegistro(String identificador) {
        try (Cursor cursor = getReadableDatabase().query("historico", new String[]{"id"},
                "id = ?", new String[]{identificador}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public void salvarRegistro(RegistroHistorico registro) {
        if (registro.luminosidade != null) {
            if (!ClassificadorLuminosidade.leituraValida(registro.luminosidade)
                    || registro.clima == null) throw new IllegalArgumentException("Leitura inválida.");
            Book.Mood.valueOf(registro.clima);
        } else if (registro.clima != null) {
            throw new IllegalArgumentException("Clima sem leitura.");
        }
        if ((registro.latitude == null) != (registro.longitude == null)
                || (registro.latitude == null) != (registro.precisao == null)) {
            throw new IllegalArgumentException("Localização incompleta.");
        }
        if (registro.latitude != null && (!Double.isFinite(registro.latitude)
                || !Double.isFinite(registro.longitude) || Math.abs(registro.latitude) > 90
                || Math.abs(registro.longitude) > 180
                || !Float.isFinite(registro.precisao) || registro.precisao < 0)) {
            throw new IllegalArgumentException("Localização inválida.");
        }
        SQLiteDatabase banco = getWritableDatabase();
        banco.beginTransaction();
        try {
            // O mesmo formulário conserva seu ID após uma rotação: não duplica a gravação.
            if (!existeRegistro(registro.identificador)) {
                ContentValues valores = new ContentValues();
                valores.put("id", registro.identificador);
                valores.put("id_livro", registro.idLivro);
                valores.put("data", registro.data);
                valores.put("luminosidade", registro.luminosidade);
                valores.put("clima", registro.clima);
                valores.put("simulada", registro.simulada ? 1 : 0);
                valores.put("latitude", registro.latitude);
                valores.put("longitude", registro.longitude);
                valores.put("precisao", registro.precisao);
                banco.insertOrThrow("historico", null, valores);
            }
            banco.setTransactionSuccessful();
        } finally {
            banco.endTransaction();
        }
    }

    /** Exibe os últimos 100 registros; os demais continuam armazenados. */
    public List<RegistroHistorico> listarHistorico() {
        List<RegistroHistorico> registros = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT h.id, h.id_livro, l.titulo, h.data, h.luminosidade, h.clima, "
                + "h.simulada, h.latitude, h.longitude, h.precisao FROM historico h "
                + "JOIN livros l ON l.id = h.id_livro ORDER BY h.data DESC, h.rowid DESC LIMIT 100", null)) {
            while (cursor.moveToNext()) {
                registros.add(new RegistroHistorico(cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), cursor.getLong(3),
                        cursor.isNull(4) ? null : cursor.getFloat(4), cursor.getString(5),
                        cursor.getInt(6) == 1, cursor.isNull(7) ? null : cursor.getDouble(7),
                        cursor.isNull(8) ? null : cursor.getDouble(8),
                        cursor.isNull(9) ? null : cursor.getFloat(9)));
            }
        }
        return registros;
    }

    public List<Book> listar(boolean somenteFavoritos) {
        List<Book> livros = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("livros", null,
                somenteFavoritos ? "favorito = 1" : null, null, null, null, "rowid ASC")) {
            while (cursor.moveToNext()) livros.add(lerLivro(cursor));
        }
        return livros;
    }

    public Book buscar(String identificador) {
        try (Cursor cursor = getReadableDatabase().query("livros", null, "id = ?",
                new String[]{identificador}, null, null, null)) {
            return cursor.moveToFirst() ? lerLivro(cursor) : null;
        }
    }

    public boolean ehFavorito(String identificador) {
        try (Cursor cursor = getReadableDatabase().query("livros", new String[]{"favorito"},
                "id = ?", new String[]{identificador}, null, null, null)) {
            return cursor.moveToFirst() && cursor.getInt(0) == 1;
        }
    }

    public void definirFavorito(String identificador, boolean favorito) {
        ContentValues valores = new ContentValues();
        valores.put("favorito", favorito ? 1 : 0);
        int alterados = getWritableDatabase().update("livros", valores, "id = ?",
                new String[]{identificador});
        if (alterados != 1) throw new IllegalArgumentException("Livro não encontrado.");
    }

    private Book lerLivro(Cursor cursor) {
        return new Book(cursor.getString(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                cursor.getString(cursor.getColumnIndexOrThrow("autor")),
                Book.Mood.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("clima"))),
                cursor.getString(cursor.getColumnIndexOrThrow("sinopse")));
    }
}
