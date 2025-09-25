package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BerandaActivity extends AppCompatActivity implements PromoAdapter.OnPromoActionListener {

    MaterialCardView cardWelcome, cardProspekM, cardLihatDataM, cardFasilitasM, cardProyekM, cardUserpM, cardInputPromoM, cardTampilkanPromoM, cardNewBeranda;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    TextView tvUserName;
    private RecyclerView recyclerPromo;
    private PromoAdapter promoAdapter;
    private List<Promo> promoList = new ArrayList<>();
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupRecyclerView();
        loadPromoData();
        setupUserInfo();
        setupClickListeners();
        setupNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    
    private void initViews() {
        cardWelcome = findViewById(R.id.cardWelcome);
        cardProspekM = findViewById(R.id.cardProspekM);
        cardFasilitasM = findViewById(R.id.cardFasilitasM);
        cardProyekM = findViewById(R.id.cardProyekM);
        cardUserpM = findViewById(R.id.cardUserpM);
        cardInputPromoM = findViewById(R.id.cardInputPromoM);
        tvUserName = findViewById(R.id.tvUserName);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        recyclerPromo = findViewById(R.id.recyclerPromo);
    }

    private void setupRecyclerView() {
        recyclerPromo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        promoAdapter = new PromoAdapter(this, promoList);
        promoAdapter.setOnPromoActionListener(this); // PERBAIKAN: Set listener ke this activity
        recyclerPromo.setAdapter(promoAdapter);
    }

    private void loadPromoData() {
        Log.d("BerandaActivity", "Loading promo data...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<PromoResponse> call = apiService.getSemuaPromo();

        call.enqueue(new Callback<PromoResponse>() {
            @Override
            public void onResponse(Call<PromoResponse> call, Response<PromoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PromoResponse promoResponse = response.body();
                    if (promoResponse.isSuccess()) {
                        promoList.clear();
                        promoList.addAll(promoResponse.getData());
                        promoAdapter.notifyDataSetChanged();
                        Log.d("BerandaActivity", "Promo data loaded: " + promoList.size() + " items");
                    } else {
                        Toast.makeText(BerandaActivity.this, "Gagal memuat promo: " + promoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BerandaActivity.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Toast.makeText(BerandaActivity.this, "Gagal memuat promo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("BerandaActivity", "Load promo error: " + t.getMessage());
            }
        });
    }

    // PERBAIKAN: Implementasi interface methods yang LENGKAP
    @Override
    public void onEditPromo(Promo promo) {
        Log.d("BerandaActivity", "Edit promo requested: " + promo.getNamaPromo());
        // Biarkan adapter yang menangani melalui openEditActivity
    }

    @Override
    public void onDeletePromo(Promo promo) {
        Log.d("BerandaActivity", "Delete promo requested: " + promo.getNamaPromo());
        // Biarkan adapter yang menangani delete secara langsung
        // Atau jika ingin handle di activity, panggil method delete di adapter:
        // deletePromoDirectly(promo);
    }

    @Override
    public void onPromoUpdated(int promoId, String updatedImage) {
        Log.d("BerandaActivity", "Promo updated - ID: " + promoId + ", Image length: " +
                (updatedImage != null ? updatedImage.length() : "null"));

        // Update item di adapter
        if (promoAdapter != null) {
            promoAdapter.updatePromoItem(promoId, updatedImage);
            Toast.makeText(this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();
        }
    }

    // PERBAIKAN: Handle activity result untuk update promo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("BerandaActivity", "onActivityResult - Request: " + requestCode + ", Result: " + resultCode);

        if (requestCode == PromoAdapter.EDIT_PROMO_REQUEST && resultCode == RESULT_OK && data != null) {
            handleEditPromoResult(data);
        }
    }

    private void handleEditPromoResult(Intent data) {
        int updatedPromoId = data.getIntExtra("UPDATED_PROMO_ID", -1);
        String updatedImage = data.getStringExtra("UPDATED_IMAGE");

        Log.d("BerandaActivity", "Handle edit result - ID: " + updatedPromoId +
                ", Image: " + (updatedImage != null ? updatedImage.length() + " chars" : "null"));

        if (updatedPromoId != -1 && updatedImage != null && !updatedImage.isEmpty()) {
            // Panggil method update melalui interface
            onPromoUpdated(updatedPromoId, updatedImage);
        } else {
            Log.w("BerandaActivity", "Invalid update data, refreshing from server");
            loadPromoData();
        }
    }

    private void setupUserInfo() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!username.isEmpty()) {
            tvUserName.setText(username);
        } else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                tvUserName.setText(username);
            }
        }
    }

    private void setupClickListeners() {
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
        cardInputPromoM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, InputPromoActivity.class);
            startActivity(intent);
        });
        cardTampilkanPromoM.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, TampilPromoActivity.class);
            startActivity(intent);
        });
        cardNewBeranda.setOnClickListener(v -> {
            Intent intent = new Intent(BerandaActivity.this, NewBeranda.class);
            startActivity(intent);
        });
    }

    private void setupNavigation() {
        topAppBar.setNavigationOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_exit) {
                logout();
                return true;
            }
            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove("username");
        editor.remove("division");
        editor.remove("nip");
        editor.apply();

        Toast.makeText(BerandaActivity.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(BerandaActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika activity resume
        loadPromoData();
    }
}