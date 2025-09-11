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

public class UnitRiversideActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;

    LinearLayout cardClusterA, cardClusterB, cardCamelia, cardOrchid, cardIrish,
            cardNeoGladial, cardLily, cardRaflesia, cardRukoTulip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unit_riverside);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

        cardClusterA = findViewById(R.id.cardClusterA);
        cardClusterB = findViewById(R.id.cardClusterB);
        cardCamelia = findViewById(R.id.cardCamelia);
        cardOrchid = findViewById(R.id.cardOrchid);
        cardIrish = findViewById(R.id.cardIrish);
        cardNeoGladial = findViewById(R.id.cardNeoGladial);
        cardLily = findViewById(R.id.cardLily);
        cardRaflesia = findViewById(R.id.cardRaflesia);
        cardRukoTulip = findViewById(R.id.cardRukoTulip);

        cardClusterA.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitClusterHerittageAActivity.class);
            startActivity(intent);
        });
        cardClusterB.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitClusterHerittageBActivity.class);
            startActivity(intent);
        });
        cardCamelia.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitCameliaActivity.class);
            startActivity(intent);
        });
        cardOrchid.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitOrchidActivity.class);
            startActivity(intent);
        });
        cardIrish.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitIrishActivity.class);
            startActivity(intent);
        });
        cardNeoGladial.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitNeoGladiolActivity.class);
            startActivity(intent);
        });
        cardLily.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitLilyActivity.class);
            startActivity(intent);
        });
        cardRaflesia.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitRaffflesiaActivity.class);
            startActivity(intent);
        });
        cardRukoTulip.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitRukoTulipActivity.class);
            startActivity(intent);
        });

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailProyekRiversideActivity.class);
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