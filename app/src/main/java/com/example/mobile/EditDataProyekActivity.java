package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataProyekActivity extends AppCompatActivity {
    private static final String TAG = "EditDataProyekActivity";

    private Button btnUbah, btnBatal;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaProyek;

    private int idProyek;
    private String oldNamaProyek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_proyek);

        initViews();
        setupNavigation();
        setupButtons();
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
        editTextNamaProyek = findViewById(R.id.editTextNamaProyek);
    }

    private void loadProyekData() {
        // Ambil data dari intent
        Intent intent = getIntent();
        if (intent != null) {
            idProyek = intent.getIntExtra("ID_PROYEK", -1);
            oldNamaProyek = intent.getStringExtra("NAMA_PROYEK");

            if (idProyek != -1 && oldNamaProyek != null) {
                // Set nilai lama ke EditText
                editTextNamaProyek.setText(oldNamaProyek);
                Log.d(TAG, "Loaded proyek data - ID: " + idProyek + ", Name: " + oldNamaProyek);
            } else {
                Toast.makeText(this, "Error: Data proyek tidak valid", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Error: Tidak ada data proyek", Toast.LENGTH_SHORT).show();
            finish();
        }
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
            }  else if (id == R.id.nav_news) {
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

    private void setupButtons() {
        btnUbah.setOnClickListener(v -> {
            updateProyek();
        });

        btnBatal.setOnClickListener(v -> {
            navigateToHome();
        });
    }

    private void updateProyek() {
        String newNamaProyek = editTextNamaProyek.getText().toString().trim();

        // Validasi
        if (newNamaProyek.isEmpty()) {
            editTextNamaProyek.setError("Nama proyek harus diisi");
            return;
        }

        if (newNamaProyek.equals(oldNamaProyek)) {
            editTextNamaProyek.setError("Nama proyek baru harus berbeda dengan nama lama");
            return;
        }

        // Tampilkan loading
        btnUbah.setEnabled(false);
        btnUbah.setText("Mengupdate...");

        // Panggil API update
        callApiUpdateProyek(newNamaProyek);
    }

    private void callApiUpdateProyek(String newNamaProyek) {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<BasicResponse> call = apiService.updateProyek(
                    idProyek,
                    oldNamaProyek,
                    newNamaProyek
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    btnUbah.setEnabled(true);
                    btnUbah.setText("Ubah");

                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse updateResponse = response.body();
                        if (updateResponse.isSuccess()) {
                            Toast.makeText(EditDataProyekActivity.this,
                                    "Proyek berhasil diupdate", Toast.LENGTH_SHORT).show();

                            // Kembali ke halaman sebelumnya
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(EditDataProyekActivity.this,
                                    "Gagal: " + updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditDataProyekActivity.this,
                                "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    btnUbah.setEnabled(true);
                    btnUbah.setText("Ubah");
                    Toast.makeText(EditDataProyekActivity.this,
                            "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            btnUbah.setEnabled(true);
            btnUbah.setText("Ubah");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(EditDataProyekActivity.this, LihatDataProyekActivity.class);
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