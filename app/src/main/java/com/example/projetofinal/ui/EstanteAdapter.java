package com.example.projetofinal.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetofinal.R;
import com.example.projetofinal.model.Book;
import com.example.projetofinal.view.CapaLivroView;
import java.util.ArrayList;
import java.util.List;

public final class EstanteAdapter extends RecyclerView.Adapter<EstanteAdapter.Cartao> {
    public interface AoAbrir { void abrir(Book livro); }
    private final AoAbrir aoAbrir;
    private final List<Book> livros = new ArrayList<>();
    private boolean favoritos;
    public EstanteAdapter(AoAbrir aoAbrir) { this.aoAbrir = aoAbrir; }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void atualizar(List<Book> novos, boolean favoritos) {
        this.favoritos = favoritos;
        livros.clear();
        livros.addAll(novos);
        notifyDataSetChanged();
    }

    @NonNull @Override public Cartao onCreateViewHolder(@NonNull ViewGroup grupo, int tipo) {
        return new Cartao(LayoutInflater.from(grupo.getContext()).inflate(R.layout.item_book, grupo, false));
    }

    @Override public void onBindViewHolder(@NonNull Cartao cartao, int posicao) {
        Book livro = livros.get(posicao);
        cartao.titulo.setText(livro.getTitle());
        cartao.autor.setText(livro.getAuthor());
        cartao.genero.setText(livro.getGenero());
        cartao.capa.definirRealce(AtividadeTematica.corAtual());
        cartao.capa.definirFavorito(favoritos);
        CarregadorCapas.carregar(cartao.capa, livro.getUrlCapa());
        cartao.itemView.setContentDescription(livro.getTitle() + ", " + livro.getAuthor() + ", " + livro.getGenero());
        cartao.itemView.setOnClickListener(vista -> aoAbrir.abrir(livro));
    }

    @Override public int getItemCount() { return livros.size(); }

    static final class Cartao extends RecyclerView.ViewHolder {
        final CapaLivroView capa;
        final TextView titulo, autor, genero;
        Cartao(View vista) {
            super(vista);
            capa = vista.findViewById(R.id.capa_livro);
            titulo = vista.findViewById(R.id.titulo_livro);
            autor = vista.findViewById(R.id.autor_livro);
            genero = vista.findViewById(R.id.clima_livro);
        }
    }
}
