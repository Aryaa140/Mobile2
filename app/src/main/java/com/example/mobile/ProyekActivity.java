package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProyekActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    LinearLayout cardRiverside, cardHarmony, cardView, cardGardenResidence, cardResidence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_proyek);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        cardRiverside = findViewById(R.id.cardRiverside);
        cardHarmony = findViewById(R.id.cardHarmony);
        cardView = findViewById(R.id.cardView);
        cardGardenResidence = findViewById(R.id.cardGardenResidence);
        cardResidence = findViewById(R.id.cardResidence);

        cardRiverside.setOnClickListener(v -> {
            Intent intent = new Intent(ProyekActivity.this, DetailProyekRiversideActivity.class);
            startActivity(intent);
        });
        cardHarmony.setOnClickListener(v -> {
            Intent intent = new Intent(ProyekActivity.this, DetailProyekHarmonyActivity.class);
            startActivity(intent);
        });
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(ProyekActivity.this, DetailProyekViewActivity.class);
            startActivity(intent);
        });
        cardGardenResidence.setOnClickListener(v -> {
            Intent intent = new Intent(ProyekActivity.this, DetailProyekGardenActivity.class);
            startActivity(intent);
        });
        cardResidence.setOnClickListener(v -> {
            Intent intent = new Intent(ProyekActivity.this, DetailProyekResidenceActivity.class);
            startActivity(intent);
        });

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(ProyekActivity.this, NewBeranda.class);
            startActivity(intent);
            finish();
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                overridePendingTransition(0, 0);
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