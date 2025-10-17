package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class InputHunianActivity extends AppCompatActivity {

    private static final String TAG = "InputHunianActivity";

    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaHunian;
    private Spinner spinnerProyek;
    private ApiService apiService;
    private List<String> proyekList = new ArrayList<>();
    private ArrayAdapter<String> proyekAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_hunian);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        initViews();
        setupNavigation();
        setupButtonListeners();
        loadProyekData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        editTextNamaHunian = findViewById(R.id.editTextNamaHunian);
        spinnerProyek = findViewById(R.id.spinnerRoleProyek);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup spinner adapter
        proyekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proyekList);
        proyekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(proyekAdapter);
    }

    private void setupNavigation() {
        // Navigasi toolbar
        topAppBar.setNavigationOnClickListener(v -> {
            navigateToHome();
        });

        // Bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupButtonListeners() {
        // Simpan data hunian
        btnSimpan.setOnClickListener(v -> simpanDataHunian());

        // Batal input
        btnBatal.setOnClickListener(v -> {
            navigateToHome();
        });
    }

    private void loadProyekData() {
        Log.d(TAG, "Loading data proyek...");

        Call<ProyekResponse> call = apiService.getProyekData("getProyek");
        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProyekResponse proyekResponse = response.body();
                    if (proyekResponse.isSuccess()) {
                        proyekList.clear();
                        for (Proyek proyek : proyekResponse.getData()) {
                            proyekList.add(proyek.getNamaProyek());
                        }
                        proyekAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Proyek data loaded: " + proyekList.size() + " items");
                    } else {
                        Toast.makeText(InputHunianActivity.this,
                                "Gagal memuat data proyek: " + proyekResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputHunianActivity.this,
                            "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Toast.makeText(InputHunianActivity.this,
                        "Gagal memuat data proyek: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Load proyek error: " + t.getMessage());
            }
        });
    }

    private void simpanDataHunian() {
        // Validasi input
        String namaHunian = editTextNamaHunian.getText().toString().trim();
        String namaProyek = spinnerProyek.getSelectedItem() != null ?
                spinnerProyek.getSelectedItem().toString() : "";

        if (namaHunian.isEmpty()) {
            editTextNamaHunian.setError("Nama hunian harus diisi");
            editTextNamaHunian.requestFocus();
            return;
        }

        if (namaProyek.isEmpty()) {
            Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Debug log
        Log.d(TAG, "Mengirim data hunian: " + namaHunian + ", Proyek: " + namaProyek);

        // Kirim data ke server
        Call<BasicResponse> call = apiService.addHunian("addHunian", namaHunian, namaProyek);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    Log.d(TAG, "Response body - success: " + basicResponse.isSuccess());
                    Log.d(TAG, "Response body - message: " + basicResponse.getMessage());

                    if (basicResponse.isSuccess()) {
                        Toast.makeText(InputHunianActivity.this,
                                basicResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Kosongkan form
                        editTextNamaHunian.setText("");
                        spinnerProyek.setSelection(0);

                        // Redirect ke halaman beranda setelah delay singkat
                        new android.os.Handler().postDelayed(
                                () -> navigateToHome(),
                                1000
                        );
                    } else {
                        String errorMsg = basicResponse.getMessage();
                        Toast.makeText(InputHunianActivity.this,
                                "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Server error: " + errorMsg);
                    }
                } else {
                    // Handle error response
                    String errorMessage = "Gagal menyimpan data. ";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += "Error: " + response.errorBody().string();
                        } else {
                            errorMessage += "Code: " + response.code();
                        }
                    } catch (Exception e) {
                        errorMessage += "Code: " + response.code();
                    }

                    Log.e(TAG, "Response error - " + errorMessage);
                    Toast.makeText(InputHunianActivity.this,
                            errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(InputHunianActivity.this,
                        "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(InputHunianActivity.this, NewBeranda.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome();
    }
}