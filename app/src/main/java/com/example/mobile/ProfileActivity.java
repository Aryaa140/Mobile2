package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    CardView cardEditProfil, cardGantiPW, cardHapusAkun, cardLogout;
    private SharedPreferences sharedPreferences;

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        cardLogout = findViewById(R.id.card_logout);
        cardEditProfil = findViewById(R.id.cardEditProfil);
        cardGantiPW = findViewById(R.id.cardGantiPW);
        cardHapusAkun = findViewById(R.id.cardHapusAkun);

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        cardEditProfil.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        cardGantiPW.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, GantiPasswordActivity.class);
            startActivity(intent);
        });

        cardHapusAkun.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HapusAkunActivity.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            // Hapus status login dari SharedPreferences
            logout();

            // Tampilkan pesan logout berhasil
            Toast.makeText(ProfileActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();

            // Redirect ke MainActivity
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    // Method untuk logout
    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove("username");
        editor.remove("division");
        editor.remove("nip");
        editor.apply();
    }

    // Method untuk mendapatkan data user yang login
    public DatabaseHelper.User getLoggedInUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String username = sharedPreferences.getString("username", "");
        String division = sharedPreferences.getString("division", "");
        String nip = sharedPreferences.getString("nip", "");

        if (username.isEmpty()) {
            return null; // User belum login
        }

        DatabaseHelper.User user = new DatabaseHelper.User();
        user.setUsername(username);
        user.setDivision(division);
        user.setNip(nip);

        return user;
    }
}