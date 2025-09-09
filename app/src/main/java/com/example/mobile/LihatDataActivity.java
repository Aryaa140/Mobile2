package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class LihatDataActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    MaterialCardView cardFasilitas, cardProyek, cardUserpM, cardProspek, cardPemohonM;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        cardFasilitas = findViewById(R.id.cardFasilitas);
        cardProyek = findViewById(R.id.cardProyek);
        cardProspek = findViewById(R.id.cardProspek);
        cardUserpM = findViewById(R.id.cardUserpM);
        cardPemohonM = findViewById(R.id.cardPemohonM);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        cardProspek.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataProspekActivity.class);
            startActivity(intent);
        });

        cardUserpM.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataUserpActivity.class);
            startActivity(intent);
        });

        cardPemohonM.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataPemohonActivity.class);
            startActivity(intent);
        });


        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, BerandaActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}