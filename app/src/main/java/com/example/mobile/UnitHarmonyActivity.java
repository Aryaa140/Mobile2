package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.cardview.widget.CardView;

public class UnitHarmonyActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    CardView cardOsaka, cardNarita, cardYokohama;
    Button btnOsaka, btnNarita, btnYokohama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unit_harmony);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

        cardOsaka = findViewById(R.id.cardOsaka);
        cardNarita = findViewById(R.id.cardNarita);
        cardYokohama = findViewById(R.id.cardYokohama);
        btnOsaka = findViewById(R.id.btnOsaka);
        btnNarita = findViewById(R.id.btnNarita);
        btnYokohama = findViewById(R.id.btnYokohama);

        cardOsaka.setOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailUnitOsakaActivity.class);
            startActivity(intent);
        });

        cardNarita.setOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailUnitNaritaActivity.class);
            startActivity(intent);
        });

        cardYokohama.setOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailUnitYokohamaActivity.class);
            startActivity(intent);
        });


        btnOsaka.setOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailUnitOsakaActivity.class);
            startActivity(intent);
        });

        btnNarita.setOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailUnitNaritaActivity.class);
            startActivity(intent);
        });

        btnYokohama.setOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailUnitYokohamaActivity.class);
            startActivity(intent);
        });

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(UnitHarmonyActivity.this, DetailProyekHarmonyActivity.class);
            startActivity(intent);
            finish();
        });

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