package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

public class BerandaActivity extends AppCompatActivity {

    MaterialCardView cardWelcome, cardProspekM, cardLihatDataM, cardFasilitasM, cardProyekM, cardUserpM, cardInputPromoM;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    TextView tvUserName; // TextView untuk menampilkan username
    private SharedPreferences sharedPreferences;

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DIVISION = "division";

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inisialisasi view
        cardWelcome = findViewById(R.id.cardWelcome);
        cardProspekM = findViewById(R.id.cardProspekM);
        cardLihatDataM = findViewById(R.id.cardLihatDataM);
        cardFasilitasM = findViewById(R.id.cardFasilitasM);
        cardProyekM = findViewById(R.id.cardProyekM);
        cardUserpM = findViewById(R.id.cardUserpM);
        cardInputPromoM = findViewById(R.id.cardInputPromoM);
        tvUserName = findViewById(R.id.tvUserName);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);

        // TAMPILKAN USERNAME - Prioritaskan dari SharedPreferences
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String division = sharedPreferences.getString(KEY_DIVISION, "");

        if (!username.isEmpty()) {
            tvUserName.setText(username); // Set text username dari SharedPreferences
        } else {
            // Fallback: Ambil data username dari Intent (jika ada)
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                tvUserName.setText(username); // Set text username
            }
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


        topAppBar.setNavigationOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
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
                }else if (id == R.id.nav_exit) {
                    logout();

                    // Tampilkan pesan logout berhasil
                    Toast.makeText(BerandaActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();

                    // Redirect ke MainActivity
                    Intent intent = new Intent(BerandaActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    overridePendingTransition(0, 0);
                    return true;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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

        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String division = sharedPreferences.getString(KEY_DIVISION, "");
        String nip = sharedPreferences.getString("nip", ""); // Ganti "nip" dengan key yang sesuai

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