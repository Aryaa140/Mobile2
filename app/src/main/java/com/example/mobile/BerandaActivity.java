package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class BerandaActivity extends AppCompatActivity {

    Button button2, button3, button4, button5, button6, button7;
    BottomNavigationView bottomNavigationView;
    MaterialCardView cardWelcome, cardProspekM, cardLihatDataM, cardFasilitasM, cardProyekM, cardBookingM, cardInputDataM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        cardWelcome = findViewById(R.id.cardWelcome);
        cardProspekM = findViewById(R.id.cardProspekM);
        cardLihatDataM = findViewById(R.id.cardLihatDataM);
        cardFasilitasM = findViewById(R.id.cardFasilitasM);
        cardProyekM = findViewById(R.id.cardProyekM);
        cardBookingM = findViewById(R.id.cardBookingM);
        cardInputDataM = findViewById(R.id.cardInputDataM);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        cardWelcome.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        cardProspekM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, TambahProspekActivity.class);
            startActivity(intent);
        });
        cardLihatDataM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, LihatDataActivity.class);
            startActivity(intent);
        });
        cardFasilitasM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, FasilitasUmumActivity.class);
            startActivity(intent);
        });
        cardProyekM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, ProyekActivity.class);
            startActivity(intent);
        });
        cardBookingM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, BookingActivity.class);
            startActivity(intent);
        });
        cardInputDataM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, InputActivity.class);
            startActivity(intent);
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