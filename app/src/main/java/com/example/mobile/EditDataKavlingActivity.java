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

public class EditDataKavlingActivity extends AppCompatActivity {
    private Button btnUbah, btnBatal;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextKavling;
    private Spinner spinnerRoleProyek, spinnerRoleHunian;

    private KavlingWithInfo kavlingData;
    private List<Proyek> proyekList = new ArrayList<>();
    private List<HunianWithInfo> hunianList = new ArrayList<>();
    private ArrayAdapter<String> proyekAdapter;
    private ArrayAdapter<String> hunianAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_kavling);

        initializeViews();
        setupAdapters();
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

    private void initializeViews() {
        btnUbah = findViewById(R.id.btnUbah);
        btnBatal = findViewById(R.id.btnBatal);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editTextKavling = findViewById(R.id.editTextKavling);
        spinnerRoleProyek = findViewById(R.id.spinnerRoleProyek);
        spinnerRoleHunian = findViewById(R.id.spinnerRoleHunian);
    }

    private void setupAdapters() {
        // Adapter untuk proyek
        proyekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        proyekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleProyek.setAdapter(proyekAdapter);

        // Adapter untuk hunian
        hunianAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        hunianAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleHunian.setAdapter(hunianAdapter);

        // Setup listener untuk spinner proyek
        spinnerRoleProyek.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && proyekList.size() > position) {
                    String selectedProyek = proyekList.get(position).getNamaProyek();
                    loadHunianByProyek(selectedProyek);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("KAVLING_DATA")) {
            kavlingData = (KavlingWithInfo) intent.getSerializableExtra("KAVLING_DATA");
            if (kavlingData != null) {
                // Set data lama ke form
                editTextKavling.setText(kavlingData.getTipeHunian());
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

                    // Set selected item berdasarkan data kavling
                    if (kavlingData != null) {
                        int position = proyekNames.indexOf(kavlingData.getProyek());
                        if (position >= 0) {
                            spinnerRoleProyek.setSelection(position);
                            // Load hunian berdasarkan proyek yang dipilih
                            loadHunianByProyek(kavlingData.getProyek());
                        }
                    }
                } else {
                    Toast.makeText(EditDataKavlingActivity.this, "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Toast.makeText(EditDataKavlingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHunianByProyek(String namaProyek) {
        // Gunakan API yang sudah ada untuk mendapatkan semua hunian
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<HunianWithInfoResponse> call = apiService.getHunianWithInfo();

        call.enqueue(new Callback<HunianWithInfoResponse>() {
            @Override
            public void onResponse(Call<HunianWithInfoResponse> call, Response<HunianWithInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<HunianWithInfo> allHunian = response.body().getData();

                    // Filter hunian berdasarkan proyek yang dipilih
                    hunianList.clear();
                    List<String> hunianNames = new ArrayList<>();

                    for (HunianWithInfo hunian : allHunian) {
                        if (hunian.getNamaProyek().equals(namaProyek)) {
                            hunianList.add(hunian);
                            hunianNames.add(hunian.getNamaHunian());
                        }
                    }

                    hunianAdapter.clear();
                    hunianAdapter.addAll(hunianNames);
                    hunianAdapter.notifyDataSetChanged();

                    // Set selected item berdasarkan data kavling
                    if (kavlingData != null && kavlingData.getProyek().equals(namaProyek)) {
                        int position = hunianNames.indexOf(kavlingData.getHunian());
                        if (position >= 0) {
                            spinnerRoleHunian.setSelection(position);
                        }
                    }
                } else {
                    Toast.makeText(EditDataKavlingActivity.this, "Gagal memuat data hunian", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HunianWithInfoResponse> call, Throwable t) {
                Toast.makeText(EditDataKavlingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonListeners() {
        btnUbah.setOnClickListener(v -> updateKavling());
        btnBatal.setOnClickListener(v -> navigateToHome());
    }

    private void updateKavling() {
        String tipeHunianBaru = editTextKavling.getText().toString().trim();
        String proyekTerpilih = (String) spinnerRoleProyek.getSelectedItem();
        String hunianTerpilih = (String) spinnerRoleHunian.getSelectedItem();

        // Validasi input
        if (tipeHunianBaru.isEmpty()) {
            editTextKavling.setError("Tipe hunian tidak boleh kosong");
            return;
        }

        if (proyekTerpilih == null || proyekTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hunianTerpilih == null || hunianTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih hunian terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (kavlingData == null) {
            Toast.makeText(this, "Data kavling tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Jika tidak ada perubahan
        if (tipeHunianBaru.equals(kavlingData.getTipeHunian()) &&
                proyekTerpilih.equals(kavlingData.getProyek()) &&
                hunianTerpilih.equals(kavlingData.getHunian())) {
            Toast.makeText(this, "Tidak ada perubahan data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Panggil API untuk update - HANYA update tipe hunian, hunian, dan proyek
        // Status penjualan TIDAK diubah karena ada relationship dengan tabel ketersediaan
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updateKavling(
                kavlingData.getIdKavling(),
                kavlingData.getTipeHunian(), // old_tipe_hunian
                tipeHunianBaru,              // new_tipe_hunian
                kavlingData.getHunian(),     // old_hunian
                hunianTerpilih,              // new_hunian
                kavlingData.getProyek(),     // old_proyek
                proyekTerpilih               // new_proyek
                // Status penjualan TIDAK diubah
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        Toast.makeText(EditDataKavlingActivity.this,
                                "Data kavling berhasil diubah", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Toast.makeText(EditDataKavlingActivity.this,
                                "Gagal: " + updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataKavlingActivity.this,
                            "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(EditDataKavlingActivity.this,
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
        Intent intent = new Intent(EditDataKavlingActivity.this, LihatDataKavlingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}