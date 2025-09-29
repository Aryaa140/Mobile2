package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.ProgressDialog;
import android.util.Log;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private TextView buatAkun, lupaPassword;
    private Button buttonLogin;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    // Keys untuk SharedPreferences (Remember Me)
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DIVISION = "division";
    private static final String KEY_NIP = "nip";
    private static final String KEY_LEVEL = "level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang login...");
        progressDialog.setCancelable(false);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 🔹 Kalau user sudah login → langsung ke beranda
        if (isUserLoggedIn()) {
            redirectToBeranda();
            return;
        }

        // Inisialisasi view sesuai XML
        buatAkun = findViewById(R.id.BuatAkun);
        lupaPassword = findViewById(R.id.lupaPassword);
        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btnLogin);

        // Tombol buat akun → ke SignUpActivity
        buatAkun.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Tombol lupa password → ke LupaPasswordActivity
        lupaPassword.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LupaPasswordActivity.class);
            startActivity(intent);
        });

        // Tombol login
        buttonLogin.setOnClickListener(v -> {
            if (validateInput()) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                loginUser(username, password);
            }
        });
    }

    // 🔹 Cek apakah user sudah login
    private boolean isUserLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d("MainActivity", "isUserLoggedIn: " + isLoggedIn);
        return isLoggedIn;
    }

    // 🔹 Simpan status login ke SharedPreferences
    private void saveLoginStatus(String username, String division, String nip, String level) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_DIVISION, division);
        editor.putString(KEY_NIP, nip);
        editor.putString(KEY_LEVEL, level != null ? level : "Operator");
        editor.apply();

        Log.d("MainActivity", "=== LOGIN DATA SAVED ===");
        Log.d("MainActivity", "Username: " + username);
        Log.d("MainActivity", "Division: " + division);
        Log.d("MainActivity", "NIP: " + nip);
        Log.d("MainActivity", "Level: " + level);
        Log.d("MainActivity", "All saved data: " + sharedPreferences.getAll().toString());
    }

    // 🔹 Redirect ke BerandaActivity
    private void redirectToBeranda() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String division = sharedPreferences.getString(KEY_DIVISION, "");
        String nip = sharedPreferences.getString(KEY_NIP, "");
        String level = sharedPreferences.getString(KEY_LEVEL, "Operator");

        Log.d("MainActivity", "=== REDIRECTING TO BERANDA ===");
        Log.d("MainActivity", "Username: " + username);
        Log.d("MainActivity", "Division: " + division);
        Log.d("MainActivity", "NIP: " + nip);
        Log.d("MainActivity", "Level: " + level);

        Intent intent = new Intent(MainActivity.this, NewBeranda.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("DIVISION", division);
        intent.putExtra("NIP", nip);
        intent.putExtra("LEVEL", level);
        startActivity(intent);
        finish();
    }

    // 🔹 Logout → bisa dipanggil dari activity lain
    public static void logout(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Log.d("MainActivity", "=== LOGOUT EXECUTED ===");

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    // 🔹 Validasi input
    private boolean validateInput() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username tidak boleh kosong");
            editTextUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password tidak boleh kosong");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password minimal 6 karakter");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    // 🔹 Proses login dengan Retrofit (ke PHP MySQL API)
    private void loginUser(String username, String password) {
        Log.d("MainActivity", "=== ATTEMPTING LOGIN ===");
        Log.d("MainActivity", "Username: " + username);
        Log.d("MainActivity", "Password length: " + password.length());

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(username, password);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);
                Log.d("MainActivity", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d("MainActivity", "Response success: " + loginResponse.isSuccess());
                    Log.d("MainActivity", "Response message: " + loginResponse.getMessage());
                    Log.d("MainActivity", "Response level: " + loginResponse.getLevel());
                    Log.d("MainActivity", "Response division: " + loginResponse.getDivisi());
                    Log.d("MainActivity", "Response NIP: " + loginResponse.getNIP());

                    if (loginResponse.isSuccess()) {
                        Toast.makeText(MainActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                        // Simpan status login (Remember Me)
                        saveLoginStatus(
                                username,  // username dari input
                                loginResponse.getDivisi(),  // divisi dari response
                                loginResponse.getNIP(),      // NIP dari response
                                loginResponse.getLevel()     // level dari response
                        );

                        // Pindah ke beranda
                        redirectToBeranda();

                    } else {
                        // Tampilkan pesan error dari server
                        String errorMessage = loginResponse.getMessage();
                        Toast.makeText(MainActivity.this, "Login gagal: " + errorMessage, Toast.LENGTH_SHORT).show();

                        // Berikan feedback spesifik untuk akun nonaktif
                        if (errorMessage != null && errorMessage.contains("tidak aktif")) {
                            editTextUsername.setError("Akun tidak aktif");
                            editTextUsername.requestFocus();
                        }
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("MainActivity", "Error response: " + errorBody);
                        Toast.makeText(MainActivity.this, "Error response dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e("MainActivity", "Error parsing error response: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Log.e("MainActivity", "Network error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method untuk menampilkan/menyembunyikan loading
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Pastikan ProgressDialog di-dismiss saat activity dihancurkan
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}