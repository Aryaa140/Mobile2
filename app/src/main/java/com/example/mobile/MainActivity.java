package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private TextView buatAkun, lupaPassword;
    private Button buttonLogin;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    // Keys untuk SharedPreferences
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

        // Cek apakah user sudah login
        if (isUserLoggedIn()) {
            // Jika sudah login, langsung arahkan ke BerandaActivity
            redirectToBeranda();
            return; // Keluar dari onCreate agar tidak inisialisasi view yang tidak perlu
        }

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view
        buatAkun = findViewById(R.id.BuatAkun);
        lupaPassword = findViewById(R.id.lupaPassword);
        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.btnLogin);

        // Tombol untuk pindah ke halaman sign up
        buatAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        lupaPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LupaPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Tombol login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validasi input
                if (validateInput()) {
                    // Verifikasi kredensial
                    String username = editTextUsername.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    if (databaseHelper.checkUser(username, password)) {
                        // Login berhasil
                        Toast.makeText(MainActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();

                        // Dapatkan data user untuk ditampilkan di beranda
                        DatabaseHelper.User user = databaseHelper.getUserData(username);

                        // Simpan status login ke SharedPreferences
                        saveLoginStatus(user);

                        // Pindah ke activity beranda dengan membawa data user
                        Intent intent = new Intent(MainActivity.this, BerandaActivity.class);
                        intent.putExtra("USERNAME", user.getUsername());
                        intent.putExtra("DIVISION", user.getDivision());
                        intent.putExtra("NIP", user.getNip());
                        startActivity(intent);
                        finish(); // Menutup activity login
                    } else {
                        // Login gagal
                        Toast.makeText(MainActivity.this, "Username atau password salah!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Method untuk mengecek apakah user sudah login
    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Method untuk menyimpan status login
    private void saveLoginStatus(DatabaseHelper.User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_DIVISION, user.getDivision());
        editor.putString(KEY_NIP, user.getNip());
        editor.apply();
    }

    // Method untuk redirect ke BerandaActivity
    private void redirectToBeranda() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        String division = sharedPreferences.getString(KEY_DIVISION, "");
        String nip = sharedPreferences.getString(KEY_NIP, "");

        Intent intent = new Intent(MainActivity.this, BerandaActivity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("DIVISION", division);
        intent.putExtra("NIP", nip);
        startActivity(intent);
        finish(); // Menutup MainActivity
    }

    // Method untuk logout (bisa dipanggil dari activity lain)
    public static void logout(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Hapus semua data login
        editor.apply();

        // Redirect ke MainActivity
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    // Method untuk mendapatkan data user yang login (bisa dipanggil dari activity lain)
    public static DatabaseHelper.User getLoggedInUser(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return null; // User belum login
        }

        DatabaseHelper.User user = new DatabaseHelper.User();
        user.setUsername(sharedPreferences.getString(KEY_USERNAME, ""));
        user.setDivision(sharedPreferences.getString(KEY_DIVISION, ""));
        user.setNip(sharedPreferences.getString(KEY_NIP, ""));

        return user;
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Tutup koneksi database jika perlu
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}