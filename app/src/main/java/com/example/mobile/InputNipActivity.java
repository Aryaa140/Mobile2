package com.example.mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputNipActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private EditText editTextNoNIP;
    private Button btnSimpan, btnBatal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_nip_activity);

        initViews();
        setupToolbar();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextNoNIP = findViewById(R.id.editTextNoNIP);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupClickListeners() {
        btnSimpan.setOnClickListener(v -> {
            simpanNIP();
        });

        btnBatal.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void simpanNIP() {
        String noNIP = editTextNoNIP.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(noNIP)) {
            editTextNoNIP.setError("No. NIP tidak boleh kosong");
            editTextNoNIP.requestFocus();
            return;
        }

        if (noNIP.length() < 3) {
            editTextNoNIP.setError("No. NIP minimal 3 digit");
            editTextNoNIP.requestFocus();
            return;
        }

        // Cek apakah NIP hanya berisi angka
        if (!noNIP.matches("\\d+")) {
            editTextNoNIP.setError("No. NIP harus berupa angka");
            editTextNoNIP.requestFocus();
            return;
        }

        // Tampilkan loading
        showLoading(true);

        // Kirim data ke server
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.inputNIP(noNIP);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Toast.makeText(InputNipActivity.this, "NIP berhasil disimpan", Toast.LENGTH_SHORT).show();
                        editTextNoNIP.setText(""); // Kosongkan field
                        editTextNoNIP.clearFocus();
                    } else {
                        Toast.makeText(InputNipActivity.this, "Gagal menyimpan NIP: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputNipActivity.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(InputNipActivity.this, "Gagal menyimpan NIP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("InputNipActivity", "Save NIP error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSimpan.setText("Menyimpan...");
            btnSimpan.setEnabled(false);
            btnBatal.setEnabled(false);
        } else {
            btnSimpan.setText("Simpan");
            btnSimpan.setEnabled(true);
            btnBatal.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}