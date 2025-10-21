package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputDataProyekActivity extends AppCompatActivity {

    private static final String TAG = "InputDataProyekActivity";

    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaProyek;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data_proyek);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        initViews();
        setupNavigation();
        setupButtonListeners();
    }

    private void initViews() {
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        editTextNamaProyek = findViewById(R.id.editTextNamaProyek);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
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
        // Simpan data proyek
        btnSimpan.setOnClickListener(v -> simpanDataProyek());

        // Batal input
        btnBatal.setOnClickListener(v -> {
            navigateToHome();
        });
    }

    private void simpanDataProyek() {
        // Validasi input
        String namaProyek = editTextNamaProyek.getText().toString().trim();

        if (namaProyek.isEmpty()) {
            editTextNamaProyek.setError("Nama proyek harus diisi");
            editTextNamaProyek.requestFocus();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Debug log
        Log.d(TAG, "Mengirim data proyek: " + namaProyek);

        // Kirim data ke server
        Call<BasicResponse> call = apiService.addProyek("addProyek", namaProyek);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response successful: " + response.isSuccessful());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        BasicResponse basicResponse = response.body();
                        Log.d(TAG, "Response body - success: " + basicResponse.isSuccess());
                        Log.d(TAG, "Response body - message: " + basicResponse.getMessage());

                        if (basicResponse.isSuccess()) {
                            Toast.makeText(InputDataProyekActivity.this,
                                    basicResponse.getMessage(), Toast.LENGTH_SHORT).show();

                            // Kosongkan form
                            editTextNamaProyek.setText("");

                            // Redirect ke halaman beranda setelah delay singkat
                            new android.os.Handler().postDelayed(
                                    () -> navigateToHome(),
                                    1000
                            );
                        } else {
                            String errorMsg = basicResponse.getMessage();
                            String displayMsg = errorMsg.isEmpty() ? "Gagal menyimpan data" : errorMsg;
                            Toast.makeText(InputDataProyekActivity.this,
                                    displayMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Server error: " + errorMsg);
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(InputDataProyekActivity.this,
                                "Response dari server kosong", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle error response
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(InputDataProyekActivity.this,
                        "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleErrorResponse(Response<BasicResponse> response) {
        String errorMessage = "Gagal menyimpan data. ";

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Raw error body: " + errorBody);

                // Coba parse error body sebagai JSON
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    BasicResponse errorResponse = gson.fromJson(errorBody, BasicResponse.class);
                    if (errorResponse != null && !errorResponse.getMessage().isEmpty()) {
                        errorMessage = errorResponse.getMessage();
                        Log.d(TAG, "Parsed error message: " + errorMessage);
                    } else {
                        errorMessage += "HTTP " + response.code() + " - " + errorBody;
                    }
                } catch (com.google.gson.JsonSyntaxException e) {
                    // Jika bukan JSON, tampilkan raw error
                    Log.e(TAG, "Error body is not JSON: " + errorBody);
                    errorMessage += "HTTP " + response.code() + " - " + getHttpErrorDescription(response.code());

                    // Jika error body mengandung pesan yang bisa dibaca
                    if (errorBody.toLowerCase().contains("error") || errorBody.toLowerCase().contains("message")) {
                        errorMessage += "\n" + errorBody;
                    }
                }
            } else {
                errorMessage += "HTTP " + response.code() + " - " + getHttpErrorDescription(response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error body: " + e.getMessage());
            errorMessage += "HTTP " + response.code() + " - " + getHttpErrorDescription(response.code());
        }

        Log.e(TAG, "Final error message: " + errorMessage);
        Toast.makeText(InputDataProyekActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private String getHttpErrorDescription(int code) {
        switch (code) {
            case 400: return "Bad Request - Permintaan tidak valid";
            case 401: return "Unauthorized - Tidak terotorisasi";
            case 403: return "Forbidden - Akses ditolak";
            case 404: return "Not Found - API tidak ditemukan";
            case 405: return "Method Not Allowed - Method tidak diizinkan";
            case 500: return "Internal Server Error - Error server internal";
            case 502: return "Bad Gateway - Gateway error";
            case 503: return "Service Unavailable - Layanan tidak tersedia";
            default: return "Error " + code;
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(InputDataProyekActivity.this, NewBeranda.class);
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