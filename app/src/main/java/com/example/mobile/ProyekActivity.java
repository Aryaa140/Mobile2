package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProyekActivity extends AppCompatActivity {

    private static final String TAG = "ProyekActivity";

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerViewProyek;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private ApiService apiService;
    private ProyekAdapterDetail proyekAdapterDetail;
    private List<Proyek> proyekList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyek);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        initViews();
        setupRecyclerView();
        setupNavigation();
        loadDataProyek(); // LANGSUNG LOAD DARI DATABASE
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerViewProyek = findViewById(R.id.recyclerViewProyek);
        progressBar = findViewById(R.id.progressBar);
        textEmpty = findViewById(R.id.textEmpty);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        proyekList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewProyek.setLayoutManager(layoutManager);
        proyekAdapterDetail = new ProyekAdapterDetail(proyekList, this::onProyekItemClick);
        recyclerViewProyek.setAdapter(proyekAdapterDetail);
    }

    private void setupNavigation() {
        topAppBar.setNavigationOnClickListener(v -> navigateToHome());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
                finish();
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadDataProyek() {
        progressBar.setVisibility(View.VISIBLE);
        textEmpty.setVisibility(View.GONE);
        recyclerViewProyek.setVisibility(View.GONE);

        Log.d(TAG, "Mengambil data proyek dari database...");

        // LANGSUNG AMBIL DARI DATABASE - TIDAK PAKAI HARDCODE
        Call<List<Proyek>> call = apiService.getAllProyek("getProyek");
        call.enqueue(new Callback<List<Proyek>>() {
            @Override
            public void onResponse(Call<List<Proyek>> call, Response<List<Proyek>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    proyekList.clear();
                    proyekList.addAll(response.body());
                    proyekAdapterDetail.notifyDataSetChanged();

                    Log.d(TAG, "Data dari database: " + proyekList.size() + " proyek");

                    for (Proyek proyek : proyekList) {
                        Log.d(TAG, "Proyek: " + proyek.getNamaProyek() +
                                ", Logo ada: " + (proyek.getLogoBase64() != null && !proyek.getLogoBase64().isEmpty()));
                    }

                    if (proyekList.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                        recyclerViewProyek.setVisibility(View.GONE);
                        textEmpty.setText("Tidak ada data proyek");
                    } else {
                        textEmpty.setVisibility(View.GONE);
                        recyclerViewProyek.setVisibility(View.VISIBLE);
                        Toast.makeText(ProyekActivity.this,
                                "Data proyek loaded: " + proyekList.size() + " items",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    textEmpty.setVisibility(View.VISIBLE);
                    recyclerViewProyek.setVisibility(View.GONE);
                    textEmpty.setText("Gagal memuat data");
                    Log.e(TAG, "Response error: " + response.code());
                    Toast.makeText(ProyekActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Proyek>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
                recyclerViewProyek.setVisibility(View.GONE);
                textEmpty.setText("Error: " + t.getMessage());
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(ProyekActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onProyekItemClick(Proyek proyek) {
        Intent intent = new Intent(ProyekActivity.this, DetailProyekActivity.class);
        // HANYA KIRIM ID, jangan kirim gambar base64
        intent.putExtra("ID_PROYEK", proyek.getIdProyek());
        startActivity(intent);
    }
    private void navigateToHome() {
        Intent intent = new Intent(ProyekActivity.this, NewBeranda.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataProyek();
    }
}