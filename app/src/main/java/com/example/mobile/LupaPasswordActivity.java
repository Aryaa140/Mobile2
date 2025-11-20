package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LupaPasswordActivity extends AppCompatActivity {

    Button btnKirimPermintaan, btnKembali;
    EditText etUsername, etNip, etEmail;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lupa_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        etUsername = findViewById(R.id.username);
        etNip = findViewById(R.id.noNip);
        etEmail = findViewById(R.id.email);
        btnKirimPermintaan = findViewById(R.id.btnKirimPermintaan);
        btnKembali = findViewById(R.id.btnKembali);

        btnKirimPermintaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kirimKodeVerifikasi();
            }
        });

        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LupaPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void kirimKodeVerifikasi() {
        String username = etUsername.getText().toString().trim();
        String nip = etNip.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username harus diisi");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nip)) {
            etNip.setError("NIP harus diisi");
            etNip.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email harus diisi");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format email tidak valid");
            etEmail.requestFocus();
            return;
        }

        // Verifikasi data user
        verifikasiDataUser(username, nip, email);
    }

    private void verifikasiDataUser(String username, String nip, String email) {
        showLoading(true);

        Call<BasicResponse> call = apiService.verifikasiDataUser(username, nip, email);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse verifyResponse = response.body();

                    if (verifyResponse.isSuccess()) {
                        // Data valid, generate kode verifikasi
                        int kodeVerifikasi = generateKodeVerifikasi();

                        // Kirim kode verifikasi via email langsung
                        kirimKodeVerifikasiEmail(username, email, kodeVerifikasi);
                    } else {
                        showLoading(false);
                        Toast.makeText(LupaPasswordActivity.this,
                                "Data tidak valid: " + verifyResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(LupaPasswordActivity.this,
                            "Gagal memverifikasi data",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LupaPasswordActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void kirimKodeVerifikasiEmail(String username, String email, int kodeVerifikasi) {
        Call<BasicResponse> call = apiService.kirimKodeVerifikasi(username, email, kodeVerifikasi);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse emailResponse = response.body();

                    if (emailResponse.isSuccess()) {
                        // Kode verifikasi berhasil dikirim, pindah ke activity verifikasi
                        Intent intent = new Intent(LupaPasswordActivity.this, VerifikasiKodeActivity.class);
                        intent.putExtra("USERNAME", username);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("KODE_VERIFIKASI", kodeVerifikasi);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LupaPasswordActivity.this,
                                "Gagal mengirim kode verifikasi: " + emailResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LupaPasswordActivity.this,
                            "Gagal mengirim kode verifikasi",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LupaPasswordActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private int generateKodeVerifikasi() {
        // Generate random 6 digit number
        return (int) ((Math.random() * 900000) + 100000);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnKirimPermintaan.setText("Memverifikasi...");
            btnKirimPermintaan.setEnabled(false);
            btnKembali.setEnabled(false);
            etUsername.setEnabled(false);
            etNip.setEnabled(false);
            etEmail.setEnabled(false);
        } else {
            btnKirimPermintaan.setText("Kirim Kode Verifikasi");
            btnKirimPermintaan.setEnabled(true);
            btnKembali.setEnabled(true);
            etUsername.setEnabled(true);
            etNip.setEnabled(true);
            etEmail.setEnabled(true);
        }
    }
}