package com.example.projetofinal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.projetofinal.data.BookCatalog;
import com.example.projetofinal.model.Book;

public class BookDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_BOOK_ID = "book_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Book book = BookCatalog.findById(getIntent().getStringExtra(EXTRA_BOOK_ID));
        if (book == null) {
            Toast.makeText(this, R.string.book_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        ((TextView) findViewById(R.id.book_title)).setText(book.getTitle());
        ((TextView) findViewById(R.id.book_author)).setText(book.getAuthor());
        ((TextView) findViewById(R.id.book_mood)).setText(moodLabel(book.getMood()));
        ((TextView) findViewById(R.id.book_synopsis)).setText(book.getSynopsis());
        findViewById(R.id.back_button).setOnClickListener(view -> finish());
    }

    static int moodLabel(Book.Mood mood) {
        switch (mood) {
            case LIGHT: return R.string.light;
            case MEDIUM: return R.string.medium;
            default: return R.string.dark;
        }
    }
}
