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

public class UnitRiversideActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;

    CardView cardClusterA, cardClusterB, cardCamelia, cardOrchid, cardIrish,
            cardNeoGladial, cardLily, cardRaflesia, cardRukoTulip;
    Button btnClusterA, btnClusterB, btnCamelia, btnOrchid, btnIrish,
            btnNeoGladial, btnLily, btnRaflesia, btnRukoTulip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unit_riverside);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        cardClusterA = findViewById(R.id.cardClusterA);
        cardClusterB = findViewById(R.id.cardClusterB);
        cardCamelia = findViewById(R.id.cardCamelia);
        cardOrchid = findViewById(R.id.cardOrchid);
        cardIrish = findViewById(R.id.cardIrish);
        cardNeoGladial = findViewById(R.id.cardNeoGladial);
        cardLily = findViewById(R.id.cardLily);
        cardRaflesia = findViewById(R.id.cardRaflesia);
        cardRukoTulip = findViewById(R.id.cardRukoTulip);

        btnClusterA = findViewById(R.id.btnClusterA);
        btnClusterB = findViewById(R.id.btnClusterB);
        btnCamelia = findViewById(R.id.btnCamelia);
        btnOrchid = findViewById(R.id.btnOrchid);
        btnIrish = findViewById(R.id.btnIrish);
        btnNeoGladial = findViewById(R.id.btnNeoGladiol);
        btnLily = findViewById(R.id.btnlily);
        btnRaflesia = findViewById(R.id.btnRaflesia );
        btnRukoTulip = findViewById(R.id.btnRukoTulip);

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



        btnClusterA.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitClusterHerittageAActivity.class);
            startActivity(intent);
        });
        btnClusterB.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitClusterHerittageBActivity.class);
            startActivity(intent);
        });
        btnCamelia.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitCameliaActivity.class);
            startActivity(intent);
        });
        btnOrchid.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitOrchidActivity.class);
            startActivity(intent);
        });
        btnIrish.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitIrishActivity.class);
            startActivity(intent);
        });
        btnNeoGladial.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitNeoGladiolActivity.class);
            startActivity(intent);
        });
        btnLily.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitLilyActivity.class);
            startActivity(intent);
        });
        btnRaflesia.setOnClickListener(v -> {
            Intent intent = new Intent(UnitRiversideActivity.this, DetailUnitRaffflesiaActivity.class);
            startActivity(intent);
        });
        btnRukoTulip.setOnClickListener(v -> {
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