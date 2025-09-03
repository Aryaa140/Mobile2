package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HapusAkunActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    Button btnHapus, btnKembali;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hapus_akun);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        btnHapus = findViewById(R.id.btnHapus);
        btnKembali = findViewById(R.id.btnKembali);

        btnHapus.setOnClickListener(v -> {
            Intent intent = new Intent(HapusAkunActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnKembali.setOnClickListener(v -> {
                Intent intent = new Intent(HapusAkunActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Setup TopAppBar navigation
        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(HapusAkunActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup bottom navigation
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
            } else if (id == R.id.nav_profile) {
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