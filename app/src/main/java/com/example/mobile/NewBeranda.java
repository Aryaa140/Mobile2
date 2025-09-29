package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class NewBeranda extends AppCompatActivity implements PromoAdapter.OnPromoActionListener {
    MaterialCardView cardWelcome, cardProspekM, cardFasilitasM, cardProyekM, cardUserpM, cardInputPromoM;
    MaterialCardView cardInputNIP, cardStatusAkun;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    TextView tvUserName, tvMenuData, tvMenu2, tvMenu, tvPromo;
    private RecyclerView recyclerPromo;
    private PromoAdapter promoAdapter;
    private List<Promo> promoList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_DIVISION = "division";

    // SHARED PREFERENCES UNTUK NEWS
    private SharedPreferences newsPrefs;
    private static final String NEWS_PREFS_NAME = "NewsUpdates";

    // Variabel untuk menyimpan level user
    private String userLevel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_beranda);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        newsPrefs = getSharedPreferences(NEWS_PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupUserInfo();
        setupRecyclerView();
        loadPromoData();
        setupClickListeners();
        setupNavigation();
        setupAccessBasedOnLevel();

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

        // INISIALISASI ELEMEN BARU
        cardInputNIP = findViewById(R.id.cardInputNIP);
        cardStatusAkun = findViewById(R.id.cardStatusAkun);
        tvMenu2 = findViewById(R.id.tvMenu2);
        tvMenu = findViewById(R.id.tvMenu);
        tvPromo = findViewById(R.id.tvPromo);

        tvUserName = findViewById(R.id.tvUserName);
        tvMenuData = findViewById(R.id.tvMenuData);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        recyclerPromo = findViewById(R.id.recyclerPromo);

        Log.d("BerandaActivity", "Init views completed");
        Log.d("BerandaActivity", "cardInputPromoM: " + (cardInputPromoM != null));
        Log.d("BerandaActivity", "cardInputNIP: " + (cardInputNIP != null));
        Log.d("BerandaActivity", "cardStatusAkun: " + (cardStatusAkun != null));
        Log.d("BerandaActivity", "tvMenu2: " + (tvMenu2 != null));
        Log.d("BerandaActivity", "tvMenuData: " + (tvMenuData != null));
        Log.d("BerandaActivity", "tvMenu: " + (tvMenu != null));
        Log.d("BerandaActivity", "tvPromo: " + (tvPromo != null));
    }

    private void setupUserInfo() {
        // Ambil data user dari SharedPreferences
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        userLevel = sharedPreferences.getString(KEY_LEVEL, "Operator");

        // DEBUG: Tampilkan semua data yang tersimpan
        Log.d("BerandaActivity", "=== DEBUG USER DATA ===");
        Log.d("BerandaActivity", "Username: " + username);
        Log.d("BerandaActivity", "Level: " + userLevel);
        Log.d("BerandaActivity", "All keys in SharedPreferences: " + sharedPreferences.getAll().toString());

        if (!username.isEmpty()) {
            tvUserName.setText(username);
        } else {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                tvUserName.setText(username);
            }
        }

        Log.d("BerandaActivity", "Final User Level: " + userLevel);
    }

    private void setupAccessBasedOnLevel() {
        Log.d("BerandaActivity", "=== SETUP ACCESS FOR LEVEL: " + userLevel + " ===");

        // Jika user adalah Operator, sembunyikan menu tertentu
        if ("Operator".equals(userLevel)) {
            Log.d("BerandaActivity", "Hiding admin features for Operator");

            // Sembunyikan button tambah promo
            if (cardInputPromoM != null) {
                cardInputPromoM.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputPromoM");
            }

            // tvMenuData (Menu Input Data) TETAP TAMPIL untuk semua level
            if (tvMenuData != null) {
                tvMenuData.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "tvMenuData remains visible for Operator");
            }

            // Sembunyikan menu pengelolaan akun
            if (tvMenu2 != null) {
                tvMenu2.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden tvMenu2");
            }

            // Sembunyikan card input NIP
            if (cardInputNIP != null) {
                cardInputNIP.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardInputNIP");
            }

            // Sembunyikan card status akun
            if (cardStatusAkun != null) {
                cardStatusAkun.setVisibility(View.GONE);
                Log.d("BerandaActivity", "Hidden cardStatusAkun");
            }

            // Sembunyikan menu admin di navigation drawer
            hideAdminNavigationMenus();

        } else {
            // Untuk Admin atau level lainnya, tampilkan semua menu
            Log.d("BerandaActivity", "Showing all features for Admin/Other level");

            // Tampilkan button tambah promo
            if (cardInputPromoM != null) {
                cardInputPromoM.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputPromoM");
            }

            // tvMenuData (Menu Input Data) TETAP TAMPIL untuk semua level
            if (tvMenuData != null) {
                tvMenuData.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown tvMenuData");
            }

            // Tampilkan menu pengelolaan akun
            if (tvMenu2 != null) {
                tvMenu2.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown tvMenu2");
            }

            // Tampilkan card input NIP
            if (cardInputNIP != null) {
                cardInputNIP.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardInputNIP");
            }

            // Tampilkan card status akun
            if (cardStatusAkun != null) {
                cardStatusAkun.setVisibility(View.VISIBLE);
                Log.d("BerandaActivity", "Shown cardStatusAkun");
            }

            // Tampilkan menu admin di navigation drawer
            showAdminNavigationMenus();
        }

        // PASTIKAN SEMUA TEXTVIEW DAN RECYCLERVIEW TAMPIL UNTUK SEMUA LEVEL
        if (tvMenu != null) {
            tvMenu.setVisibility(View.VISIBLE); // Menu Informasi tetap tampil
            Log.d("BerandaActivity", "tvMenu remains visible for all levels");
        }

        if (tvPromo != null) {
            tvPromo.setVisibility(View.VISIBLE); // Promo Terbaru tetap tampil
            Log.d("BerandaActivity", "tvPromo remains visible for all levels");
        }

        if (recyclerPromo != null) {
            recyclerPromo.setVisibility(View.VISIBLE);
            Log.d("BerandaActivity", "RecyclerView is visible for all levels");
        }
    }

    private void hideAdminNavigationMenus() {
        if (navigationView != null) {
            // Sembunyikan section "Menu Pengelolaan Akun" dan item-itemnya
            hideMenuByTitle("Menu Pengelolaan Akun");
            hideMenuByTitle("Input NIP");
            hideMenuByTitle("Aktivasi Akun");
            hideMenuByTitle("Kelola Akun");
            hideMenuByTitle("Manage Accounts");

            Log.d("NavigationMenu", "Admin menus hidden for Operator");
        }
    }

    private void showAdminNavigationMenus() {
        if (navigationView != null) {
            // Tampilkan section "Menu Pengelolaan Akun" dan item-itemnya
            showMenuByTitle("Menu Pengelolaan Akun");
            showMenuByTitle("Input NIP");
            showMenuByTitle("Aktivasi Akun");
            showMenuByTitle("Kelola Akun");
            showMenuByTitle("Manage Accounts");

            Log.d("NavigationMenu", "Admin menus shown for Admin");
        }
    }

    private void hideMenuByTitle(String title) {
        if (navigationView != null) {
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                if (navigationView.getMenu().getItem(i).getTitle().toString().equalsIgnoreCase(title)) {
                    navigationView.getMenu().getItem(i).setVisible(false);
                    Log.d("NavigationMenu", "Hidden menu: " + title);
                    break;
                }
            }
        }
    }

    private void showMenuByTitle(String title) {
        if (navigationView != null) {
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                if (navigationView.getMenu().getItem(i).getTitle().toString().equalsIgnoreCase(title)) {
                    navigationView.getMenu().getItem(i).setVisible(true);
                    Log.d("NavigationMenu", "Shown menu: " + title);
                    break;
                }
            }
        }
    }

    private void setupRecyclerView() {
        recyclerPromo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        promoAdapter = new PromoAdapter(this, promoList);
        promoAdapter.setUserLevel(userLevel);
        promoAdapter.setOnPromoActionListener(this);
        recyclerPromo.setAdapter(promoAdapter);

        Log.d("BerandaActivity", "RecyclerView setup completed with level: " + userLevel);
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
                        Toast.makeText(NewBeranda.this, "Gagal memuat promo: " + promoResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(NewBeranda.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PromoResponse> call, Throwable t) {
                Toast.makeText(NewBeranda.this, "Gagal memuat promo: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("BerandaActivity", "Load promo error: " + t.getMessage());
            }
        });
    }

    // IMPLEMENTASI METHOD DARI INTERFACE - YANG INI SUDAH ADA DAN TIDAK DIUBAH
    @Override
    public void onPromoUpdated(int promoId, String updatedImage) {
        Log.d("BerandaActivity", "Promo updated - ID: " + promoId);

        // Update item di adapter
        if (promoAdapter != null) {
            promoAdapter.updatePromoItem(promoId, updatedImage);
            Toast.makeText(this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();
        }

        // SIMPAN INFO UPDATE UNTUK NEWS ACTIVITY
        savePromoUpdateForNews(promoId, "Diubah", updatedImage);
    }

    // IMPLEMENTASI METHOD BARU YANG DIBUTUHKAN
    @Override
    public void onPromoDeleted(String promoTitle, String penginput) {
        Log.d("BerandaActivity", "Promo deleted: " + promoTitle);
        Toast.makeText(this, "Promo '" + promoTitle + "' dihapus", Toast.LENGTH_SHORT).show();

        // SIMPAN INFO DELETE UNTUK NEWS ACTIVITY
        savePromoDeleteForNews(promoTitle, penginput);
    }

    // METHOD UNTUK SIMPAN INFO UPDATE PROMO - TIDAK DIUBAH
    private void savePromoUpdateForNews(int promoId, String status, String updatedImage) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putInt("last_updated_promo_id", promoId);
        editor.putString("last_updated_status", status);
        editor.putString("last_updated_image", updatedImage != null ? updatedImage : "");
        editor.putLong("last_update_time", System.currentTimeMillis());
        editor.apply();

        Log.d("BerandaActivity", "Saved update info for NewsActivity - Promo ID: " + promoId);
    }

    // METHOD UNTUK SIMPAN INFO DELETE PROMO - TIDAK DIUBAH
    private void savePromoDeleteForNews(String promoTitle, String penginput) {
        SharedPreferences.Editor editor = newsPrefs.edit();
        editor.putString("last_deleted_title", promoTitle);
        editor.putString("last_deleted_inputter", penginput);
        editor.putString("last_deleted_status", "Dihapus");
        editor.putLong("last_delete_time", System.currentTimeMillis());
        editor.apply();

        Log.d("BerandaActivity", "Saved delete info for NewsActivity - Title: " + promoTitle);
    }

    // HANDLE ACTIVITY RESULT - TIDAK DIUBAH
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

        Log.d("BerandaActivity", "Handle edit result - ID: " + updatedPromoId);

        if (updatedPromoId != -1) {
            // Panggil method update melalui interface
            onPromoUpdated(updatedPromoId, updatedImage);
        } else {
            Log.w("BerandaActivity", "Invalid update data, refreshing from server");
            loadPromoData();
        }
    }

    private void setupClickListeners() {
        cardWelcome.setOnClickListener(v -> {
            Intent profileIntent = new Intent(NewBeranda.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        cardProspekM.setOnClickListener(v -> {
            Intent intentProspek = new Intent(NewBeranda.this, TambahProspekActivity.class);
            startActivity(intentProspek);
        });

        cardFasilitasM.setOnClickListener(v -> {
            Intent intentFasilitas = new Intent(NewBeranda.this, FasilitasActivity.class);
            startActivity(intentFasilitas);
        });

        cardProyekM.setOnClickListener(v -> {
            Intent intentProyek = new Intent(NewBeranda.this, ProyekActivity.class);
            startActivity(intentProyek);
        });

        cardUserpM.setOnClickListener(v -> {
            Intent intentUserp = new Intent(NewBeranda.this, TambahUserpActivity.class);
            startActivity(intentUserp);
        });

        cardInputPromoM.setOnClickListener(v -> {
            // Cek level user untuk akses input promo
            if ("Operator".equals(userLevel)) {
                Toast.makeText(this, "Hanya Admin yang dapat menambah promo", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(NewBeranda.this, InputPromoActivity.class);
            startActivity(intent);
        });

        // TAMBAHKAN CLICK LISTENER UNTUK MENU ADMIN - DENGAN INTENT YANG BENAR
        if (cardInputNIP != null) {
            cardInputNIP.setOnClickListener(v -> {
                if ("Operator".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Input NIP", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke InputNipActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, InputNipActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening InputNipActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening InputNipActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Input NIP", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardStatusAkun != null) {
            cardStatusAkun.setOnClickListener(v -> {
                if ("Operator".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses Aktivasi Akun", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Intent ke StatusAkunActivity
                try {
                    Intent intent = new Intent(NewBeranda.this, StatusAkunActivity.class);
                    startActivity(intent);
                    Log.d("BerandaActivity", "Opening StatusAkunActivity");
                } catch (Exception e) {
                    Log.e("BerandaActivity", "Error opening StatusAkunActivity: " + e.getMessage());
                    Toast.makeText(NewBeranda.this, "Gagal membuka Aktivasi Akun", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
            String title = item.getTitle().toString();
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
            } else if (title.equalsIgnoreCase("Input NIP") ||
                    title.equalsIgnoreCase("Aktivasi Akun") ||
                    title.contains("Pengelolaan Akun")) {
                // Menu admin - cek level user
                if ("Operator".equals(userLevel)) {
                    Toast.makeText(this, "Hanya Admin yang dapat mengakses menu ini", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // Handle menu admin di sini
                handleAdminMenu(title);
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

    private void handleAdminMenu(String menuTitle) {
        try {
            if (menuTitle.equalsIgnoreCase("Input NIP")) {
                Intent intent = new Intent(this, InputNipActivity.class);
                startActivity(intent);
            } else if (menuTitle.equalsIgnoreCase("Aktivasi Akun")) {
                Intent intent = new Intent(this, StatusAkunActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Membuka: " + menuTitle, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("BerandaActivity", "Error handling admin menu: " + e.getMessage());
            Toast.makeText(this, "Gagal membuka menu: " + menuTitle, Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove("username");
        editor.remove("division");
        editor.remove("nip");
        editor.remove(KEY_LEVEL);
        editor.apply();

        Toast.makeText(NewBeranda.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(NewBeranda.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPromoData();

        // Refresh access control setiap resume
        setupUserInfo();
        setupAccessBasedOnLevel();

        Log.d("BerandaActivity", "onResume completed - Level: " + userLevel);
    }
}