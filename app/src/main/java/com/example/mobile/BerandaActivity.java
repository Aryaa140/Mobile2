package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class BerandaActivity extends AppCompatActivity {

    MaterialCardView cardWelcome, cardProspekM, cardLihatDataM, cardFasilitasM, cardProyekM, cardUserpM;
    BottomNavigationView bottomNavigationView;
    TextView tvUserName; // TextView untuk menampilkan username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        // Inisialisasi view
        cardWelcome = findViewById(R.id.cardWelcome);
        cardProspekM = findViewById(R.id.cardProspekM);
        cardLihatDataM = findViewById(R.id.cardLihatDataM);
        cardFasilitasM = findViewById(R.id.cardFasilitasM);
        cardProyekM = findViewById(R.id.cardProyekM);
        cardUserpM = findViewById(R.id.cardUserpM);
        tvUserName = findViewById(R.id.tvUserName); // Inisialisasi TextView username
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // TAMPILKAN USERNAME - TANPA MENGUBAH INTENT YANG SUDAH ADA
        // Ambil data username dari Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME")) {
            String username = intent.getStringExtra("USERNAME");
            tvUserName.setText(username); // Set text username
        }

        // JANGAN UBAH INTENT YANG SUDAH ADA - biarkan seperti semula
        cardWelcome.setOnClickListener(v -> {
            Intent profileIntent = new Intent(BerandaActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });
        cardProspekM.setOnClickListener(v -> {
            Intent intentProspek = new Intent(BerandaActivity.this, TambahProspekActivity.class);
            startActivity(intentProspek);
        });
        cardLihatDataM.setOnClickListener(v -> {
            Intent intentLihatData = new Intent(BerandaActivity.this, LihatDataActivity.class);
            startActivity(intentLihatData);
        });
        cardFasilitasM.setOnClickListener(v -> {
            Intent intentFasilitas = new Intent(BerandaActivity.this, FasilitasActivity.class);
            startActivity(intentFasilitas);
        });
        cardProyekM.setOnClickListener(v -> {
            Intent intentProyek = new Intent(BerandaActivity.this, ProyekActivity.class);
            startActivity(intentProyek);
        });
        cardUserpM.setOnClickListener(v -> {
            Intent intentUserp = new Intent(BerandaActivity.this, TambahUserpActivity.class);
            startActivity(intentUserp);
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
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