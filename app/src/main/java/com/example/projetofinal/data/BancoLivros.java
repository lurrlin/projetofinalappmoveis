package com.example.projetofinal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.projetofinal.model.Book;
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
        super(contexto.getApplicationContext(), nomeBanco, null, 1);
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
    }

    @Override public void onUpgrade(SQLiteDatabase banco, int versaoAntiga, int versaoNova) {
        // A versão 1 é a primeira. Futuras versões deverão migrar sem apagar favoritos.
        throw new IllegalStateException("Migração de banco ainda não definida.");
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
