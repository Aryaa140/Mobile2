package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataHunianActivity extends AppCompatActivity {
    private Button btnUbah, btnBatal;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaHunian;
    private Spinner spinnerRoleProyek;

    private HunianWithInfo hunianData;
    private List<Proyek> proyekList = new ArrayList<>();
    private ArrayAdapter<String> proyekAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_hunian);

        initViews();
        setupNavigation();
        setupButtonListeners();
        loadIntentData();
        loadProyekData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnUbah = findViewById(R.id.btnUbah);
        btnBatal = findViewById(R.id.btnBatal);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editTextNamaHunian = findViewById(R.id.editTextNamaHunian);
        spinnerRoleProyek = findViewById(R.id.spinnerRoleProyek);

        // Setup adapter untuk spinner
        proyekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        proyekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleProyek.setAdapter(proyekAdapter);
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("HUNIAN_DATA")) {
            hunianData = (HunianWithInfo) intent.getSerializableExtra("HUNIAN_DATA");
            if (hunianData != null) {
                editTextNamaHunian.setText(hunianData.getNamaHunian());
            }
        }
    }

    private void loadProyekData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProyekResponse> call = apiService.getProyek();

        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    proyekList = response.body().getData();

                    List<String> proyekNames = new ArrayList<>();
                    for (Proyek proyek : proyekList) {
                        proyekNames.add(proyek.getNamaProyek());
                    }

                    proyekAdapter.clear();
                    proyekAdapter.addAll(proyekNames);
                    proyekAdapter.notifyDataSetChanged();

                    // Set selected item berdasarkan data hunian
                    if (hunianData != null) {
                        int position = proyekNames.indexOf(hunianData.getNamaProyek());
                        if (position >= 0) {
                            spinnerRoleProyek.setSelection(position);
                        }
                    }
                } else {
                    Toast.makeText(EditDataHunianActivity.this, "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Toast.makeText(EditDataHunianActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonListeners() {
        btnUbah.setOnClickListener(v -> updateHunian());
        btnBatal.setOnClickListener(v -> navigateToHome());
    }

    private void updateHunian() {
        String namaHunianBaru = editTextNamaHunian.getText().toString().trim();
        String proyekTerpilih = (String) spinnerRoleProyek.getSelectedItem();

        if (namaHunianBaru.isEmpty()) {
            editTextNamaHunian.setError("Nama hunian tidak boleh kosong");
            return;
        }

        if (proyekTerpilih == null || proyekTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hunianData == null) {
            Toast.makeText(this, "Data hunian tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Jika tidak ada perubahan
        if (namaHunianBaru.equals(hunianData.getNamaHunian()) &&
                proyekTerpilih.equals(hunianData.getNamaProyek())) {
            Toast.makeText(this, "Tidak ada perubahan data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Panggil API untuk update
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updateHunian(
                hunianData.getIdHunian(),
                hunianData.getNamaHunian(), // old_nama_hunian
                namaHunianBaru,             // new_nama_hunian
                proyekTerpilih              // nama_proyek
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        Toast.makeText(EditDataHunianActivity.this,
                                "Data hunian berhasil diubah", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(EditDataHunianActivity.this,
                                "Gagal: " + updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataHunianActivity.this,
                            "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(EditDataHunianActivity.this,
                        "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        // Navigasi toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            navigateToHome();
        });

        // Bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);
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
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(EditDataHunianActivity.this, LihatDataHunianActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}