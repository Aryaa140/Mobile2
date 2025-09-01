package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class InputActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    MaterialCardView cardFasilitas, cardBooking, cardProyek, cardPromo, cardProspek, cardUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input);

        TopAppBar = findViewById(R.id.topAppBar);

        cardFasilitas = findViewById(R.id.cardFasilitas);
        cardBooking = findViewById(R.id.cardBooking);
        cardProyek = findViewById(R.id.cardProyek);
        cardPromo = findViewById(R.id.cardPromo);
        cardProspek = findViewById(R.id.cardProspek);
        cardUnit = findViewById(R.id.cardUnit);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(InputActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        cardFasilitas.setOnClickListener(v -> {
            Intent intent = new Intent(InputActivity.this, InputDataFasilitasActivity.class);
            startActivity(intent);
        });

        cardBooking.setOnClickListener(v -> {
            Intent intent = new Intent(InputActivity.this, BookingActivity.class);
            startActivity(intent);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}