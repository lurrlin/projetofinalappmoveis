package com.example.projetofinal;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.projetofinal.data.BookCatalog;
import com.example.projetofinal.model.Book;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        LinearLayout catalog = findViewById(R.id.book_list);
        for (Book book : BookCatalog.getAll()) {
            View item = getLayoutInflater().inflate(R.layout.item_book, catalog, false);
            ((TextView) item.findViewById(R.id.book_title)).setText(book.getTitle());
            ((TextView) item.findViewById(R.id.book_author)).setText(book.getAuthor());
            ((TextView) item.findViewById(R.id.book_mood)).setText(
                    BookDetailsActivity.moodLabel(book.getMood()));
            View details = item.findViewById(R.id.details_button);
            details.setContentDescription(getString(R.string.details_for, book.getTitle()));
            details.setOnClickListener(view -> {
                Intent intent = new Intent(this, BookDetailsActivity.class);
                intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, book.getId());
                startActivity(intent);
            });
            catalog.addView(item);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
