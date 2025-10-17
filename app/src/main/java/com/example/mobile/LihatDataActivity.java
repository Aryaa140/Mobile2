package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LihatDataActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    MaterialCardView cardProspek, cardBooking;
    BottomNavigationView bottomNavigationView;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lihat_data);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        cardProspek = findViewById(R.id.cardProspek);
        cardBooking = findViewById(R.id.cardBooking);


        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, NewBeranda.class);
            startActivity(intent);
            finish();
        });

        cardProspek.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataProspekActivity.class);
            startActivity(intent);
        });

        cardBooking.setOnClickListener(v -> {
            Intent intent = new Intent(LihatDataActivity.this, LihatDataUserpActivity.class);
            startActivity(intent);
        });


        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

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
    private void checkAccountExpiry() {
        String dateOutStr = sharedPreferences.getString("date_out", null);

        if (dateOutStr != null && !dateOutStr.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dateOut = dateFormat.parse(dateOutStr);
                Date today = new Date();

                if (today.after(dateOut)) {
                    Toast.makeText(this, "Akun telah expired. Silakan hubungi administrator.", Toast.LENGTH_LONG).show();
                    MainActivity.logout(this);
                }
            } catch (ParseException e) {
                Log.e("NewBeranda", "Error parsing date_out: " + e.getMessage());
            }
        }
    }
}