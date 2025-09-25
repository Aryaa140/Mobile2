package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    // Keys untuk SharedPreferences (Remember Me)
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DIVISION = "division";
    private static final String KEY_NIP = "nip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 🔹 Simpan status login ke SharedPreferences
    private void saveLoginStatus(String username, String division, String nip) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_DIVISION, division);
        editor.putString(KEY_NIP, nip);
        editor.apply();
    }

    // 🔹 Redirect ke BerandaActivity
    private void redirectToBeranda() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String division = sharedPreferences.getString(KEY_DIVISION, "");
        String nip = sharedPreferences.getString(KEY_NIP, "");

        Intent intent = new Intent(MainActivity.this, NewBeranda.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("DIVISION", division);
        intent.putExtra("NIP", nip);
        startActivity(intent);
        finish();
    }

    // 🔹 Logout → bisa dipanggil dari activity lain
    public static void logout(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

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
    // 🔹 Proses login dengan Retrofit (ke PHP MySQL API)
    private void loginUser(String username, String password) {
        // Tampilkan progress dialog atau loading indicator
        Log.d("LoginDebug", "Attempting login with: " + username + "/" + password);
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginUser(username, password);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showLoading(false);
                Log.d("LoginDebug", "Response code: " + response.code());
                Log.d("LoginDebug", "Response body: " + response.body());
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        Toast.makeText(MainActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                        // Simpan status login (Remember Me)
                        // Perhatikan struktur response, jika ada objek data
                        if (loginResponse.getData() != null) {
                            // Jika response menggunakan struktur data
                            saveLoginStatus(
                                    loginResponse.getData().getUsername(),
                                    loginResponse.getData().getDivisi(),
                                    loginResponse.getData().getNIP()
                            );
                        } else {
                            // Jika response langsung di root
                            saveLoginStatus(
                                    username,  // karena username tidak dikembalikan di response
                                    loginResponse.getDivisi(),
                                    loginResponse.getNIP()
                            );
                        }

                        // Pindah ke beranda
                        redirectToBeranda();

                    } else {
                        Toast.makeText(MainActivity.this, "Login gagal: " + loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(MainActivity.this, "Error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Response error!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(MainActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LoginError", "Error: " + t.getMessage());
            }
        });
    }

    // Method untuk menampilkan/menyembunyikan loading
    private void showLoading(boolean isLoading) {
        // Implementasi progress dialog atau progress bar
        if (isLoading) {
            // Tampilkan loading
        } else {
            // Sembunyikan loading
        }
    }
}
