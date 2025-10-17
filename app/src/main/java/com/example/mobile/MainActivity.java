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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private TextView buatAkun, lupaPassword;
    private Button buttonLogin;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DIVISION = "division";
    private static final String KEY_NIP = "nip";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_DATE_OUT = "date_out";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inisialisasi ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang login...");
        progressDialog.setCancelable(false);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi view
        buatAkun = findViewById(R.id.BuatAkun);
        lupaPassword = findViewById(R.id.lupaPassword);
        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btnLogin);

        // Cek jika user sudah login
        if (isUserLoggedIn()) {
            if (isAccountExpired()) {
                Toast.makeText(this, "Akun telah expired. Silakan hubungi administrator.", Toast.LENGTH_LONG).show();
                logout();
            } else {
                redirectToBeranda();
            }
            return;
        }

        // Tombol buat akun
        buatAkun.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Tombol lupa password
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

    // CEK APAKAH USER SUDAH LOGIN
    private boolean isUserLoggedIn() {
        try {
            return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status: " + e.getMessage());
            return false;
        }
    }

    // CEK APAKAH AKUN SUDAH EXPIRED
    private boolean isAccountExpired() {
        try {
            String dateOutStr = sharedPreferences.getString(KEY_DATE_OUT, null);

            if (dateOutStr == null || dateOutStr.isEmpty() || "null".equals(dateOutStr)) {
                Log.d(TAG, "No date_out found, account not expired");
                return false;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dateOut = dateFormat.parse(dateOutStr);
            Date today = new Date();

            // Reset waktu ke 00:00:00 untuk perbandingan
            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);
            today = calToday.getTime();

            Calendar calDateOut = Calendar.getInstance();
            calDateOut.setTime(dateOut);
            calDateOut.set(Calendar.HOUR_OF_DAY, 0);
            calDateOut.set(Calendar.MINUTE, 0);
            calDateOut.set(Calendar.SECOND, 0);
            calDateOut.set(Calendar.MILLISECOND, 0);
            dateOut = calDateOut.getTime();

            return today.getTime() >= dateOut.getTime();

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date_out: " + e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error in isAccountExpired: " + e.getMessage());
            return false;
        }
    }

    // SIMPAN STATUS LOGIN
    private void saveLoginStatus(String username, String division, String nip, String level, String dateOut) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_DIVISION, division);
            editor.putString(KEY_NIP, nip);
            editor.putString(KEY_LEVEL, level != null ? level : "Operator");
            editor.putString(KEY_DATE_OUT, dateOut != null ? dateOut : "");
            editor.apply();

            Log.d(TAG, "=== LOGIN DATA SAVED ===");
            Log.d(TAG, "Username: " + username);
            Log.d(TAG, "Division: " + division);
            Log.d(TAG, "Level: " + level);
            Log.d(TAG, "Date Out: " + dateOut);

        } catch (Exception e) {
            Log.e(TAG, "Error saving login status: " + e.getMessage());
        }
    }

    // REDIRECT KE BERANDA
    private void redirectToBeranda() {
        try {
            if (isAccountExpired()) {
                Toast.makeText(this, "Akun telah expired. Silakan hubungi administrator.", Toast.LENGTH_LONG).show();
                logout();
                return;
            }

            String username = sharedPreferences.getString(KEY_USERNAME, "");
            String division = sharedPreferences.getString(KEY_DIVISION, "");
            String nip = sharedPreferences.getString(KEY_NIP, "");
            String level = sharedPreferences.getString(KEY_LEVEL, "Operator");

            Intent intent = new Intent(MainActivity.this, NewBeranda.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("DIVISION", division);
            intent.putExtra("NIP", nip);
            intent.putExtra("LEVEL", level);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to beranda: " + e.getMessage());
            Toast.makeText(this, "Error redirecting", Toast.LENGTH_SHORT).show();
        }
    }

    // LOGOUT - STATIC METHOD
    public static void logout(AppCompatActivity activity) {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
            activity.finish();
        } catch (Exception e) {
            Log.e(TAG, "Error in static logout: " + e.getMessage());
        }
    }

    // LOGOUT - INSTANCE METHOD
    private void logout() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error in logout: " + e.getMessage());
        }
    }

    // VALIDASI INPUT
    private boolean validateInput() {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error in validateInput: " + e.getMessage());
            return false;
        }
    }

    // PROSES LOGIN
    private void loginUser(String username, String password) {
        Log.d(TAG, "=== ATTEMPTING LOGIN ===");
        Log.d(TAG, "Username: " + username);

        showLoading(true);

        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<LoginResponse> call = apiService.loginUser(username, password);

            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse loginResponse = response.body();

                        if (loginResponse.isSuccess()) {
                            Toast.makeText(MainActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                            saveLoginStatus(
                                    username,
                                    loginResponse.getDivisi(),
                                    loginResponse.getNIP(),
                                    loginResponse.getLevel(),
                                    loginResponse.getDate_out()
                            );

                            redirectToBeranda();

                        } else {
                            String errorMessage = loginResponse.getMessage();
                            Toast.makeText(MainActivity.this, "Login gagal: " + errorMessage, Toast.LENGTH_SHORT).show();

                            if (errorMessage != null && errorMessage.contains("tidak aktif")) {
                                editTextUsername.setError("Akun tidak aktif");
                                editTextUsername.requestFocus();
                            }
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "Error response: " + errorBody);
                            Toast.makeText(MainActivity.this, "Error dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing error response: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Network error: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error in loginUser: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // LOADING
    private void showLoading(boolean isLoading) {
        try {
            if (isLoading) {
                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }
            } else {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showLoading: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (isUserLoggedIn()) {
                checkAccountExpiryRealTime();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage());
        }
    }

    // REAL-TIME CHECK EXPIRY
    private void checkAccountExpiryRealTime() {
        try {
            String username = sharedPreferences.getString(KEY_USERNAME, "");

            if (username.isEmpty()) {
                return;
            }

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<DateOutResponse> call = apiService.checkDateOut(username);

            call.enqueue(new Callback<DateOutResponse>() {
                @Override
                public void onResponse(Call<DateOutResponse> call, Response<DateOutResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        DateOutResponse dateOutResponse = response.body();

                        if (dateOutResponse.isSuccess()) {
                            String currentDateOut = dateOutResponse.getDate_out();
                            boolean isExpired = dateOutResponse.isIs_expired();

                            if (currentDateOut != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_DATE_OUT, currentDateOut);
                                editor.apply();
                            }

                            if (isExpired) {
                                Toast.makeText(MainActivity.this,
                                        "Akun telah expired. Silakan hubungi administrator.",
                                        Toast.LENGTH_LONG).show();
                                logout();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<DateOutResponse> call, Throwable t) {
                    Log.e(TAG, "Real-time check failed: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in checkAccountExpiryRealTime: " + e.getMessage());
        }
    }
}