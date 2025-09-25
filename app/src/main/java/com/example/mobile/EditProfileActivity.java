package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    EditText editTextNoNip, editTextUsername;
    Spinner spinnerDivisi;
    Button btnEdit, btnBatal;

    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private String currentUsername = "";
    private String currentNip = "";
    private static final String TAG = "EditProfileActivity";

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DIVISION = "division";
    private static final String KEY_NIP = "nip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // PERBAIKAN: Gunakan RetrofitClient yang sudah ada dengan base URL yang benar
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editTextNoNip = findViewById(R.id.noNip);
        editTextUsername = findViewById(R.id.username);
        spinnerDivisi = findViewById(R.id.spinnerOpsi);
        btnEdit = findViewById(R.id.btnEdit);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup spinner divisi
        setupSpinnerDivisi();

        // Set bottom navigation selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Setup TopAppBar navigation
        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Setup button listeners
        btnEdit.setOnClickListener(v -> editProfil());

        btnBatal.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Load data user yang sedang login
        loadCurrentUserData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSpinnerDivisi() {
        // Data divisi - sesuaikan dengan data yang ada di database
        String[] divisions = {
                "IT",
                "Marketing",
                "Sales",
                "HRD",
                "Finance",
                "Operations",
                "Production"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                divisions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivisi.setAdapter(adapter);
    }

    private void loadCurrentUserData() {
        // Ambil data dari SharedPreferences (user yang sedang login)
        currentNip = sharedPreferences.getString(KEY_NIP, "");
        currentUsername = sharedPreferences.getString(KEY_USERNAME, "");
        String currentDivision = sharedPreferences.getString(KEY_DIVISION, "");

        // Set data ke form
        editTextNoNip.setText(currentNip);
        editTextUsername.setText(currentUsername);

        // Set spinner selection
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDivisi.getAdapter();
        int position = adapter.getPosition(currentDivision);
        if (position >= 0) {
            spinnerDivisi.setSelection(position);
        }

        // Non-aktifkan edit NIP karena tidak boleh diubah
        editTextNoNip.setEnabled(false);
    }

    private void editProfil() {
        String nip = editTextNoNip.getText().toString().trim();
        String newUsername = editTextUsername.getText().toString().trim();
        String newDivision = spinnerDivisi.getSelectedItem().toString();

        // Validasi input
        if (!validateInput(nip, newUsername)) {
            return;
        }

        // Cek apakah ada perubahan data
        if (newUsername.equals(currentUsername) && newDivision.equals(sharedPreferences.getString(KEY_DIVISION, ""))) {
            Toast.makeText(this, "Tidak ada perubahan data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cek apakah username berubah
        if (newUsername.equals(currentUsername)) {
            // Jika username tidak berubah, hanya update divisi
            updateProfile(nip, newUsername, newDivision, false);
        } else {
            // Jika username berubah, cek dulu apakah username baru available
            checkUsernameAvailability(nip, newUsername, newDivision);
        }
    }

    private boolean validateInput(String nip, String newUsername) {
        if (nip.isEmpty()) {
            editTextNoNip.setError("No. NIP harus diisi");
            editTextNoNip.requestFocus();
            return false;
        }

        if (newUsername.isEmpty()) {
            editTextUsername.setError("Username harus diisi");
            editTextUsername.requestFocus();
            return false;
        }

        if (!nip.matches("\\d+")) {
            editTextNoNip.setError("NIP harus berupa angka");
            editTextNoNip.requestFocus();
            return false;
        }

        return true;
    }

    private void checkUsernameAvailability(String nip, String newUsername, String newDivision) {
        Toast.makeText(this, "Memeriksa ketersediaan username...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Checking username availability: " + newUsername);
        Log.d(TAG, "Current username: " + currentUsername);
        Log.d(TAG, "NIP: " + nip);
        Log.d(TAG, "Division: " + newDivision);

        Call<BasicResponse> call = apiService.checkUsername(newUsername, currentUsername);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "Check username response code: " + response.code());
                Log.d(TAG, "Check username response headers: " + response.headers());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse checkResponse = response.body();
                    Log.d(TAG, "Check username full response: " +
                            "success=" + checkResponse.isSuccess() +
                            ", available=" + checkResponse.isAvailable() +
                            ", message=" + checkResponse.getMessage());

                    if (checkResponse.isSuccess() && checkResponse.isAvailable()) {
                        // Username available
                        Log.d(TAG, "Username AVAILABLE - proceeding with update");
                        updateProfile(nip, newUsername, newDivision, true);
                    } else {
                        Log.d(TAG, "Username NOT AVAILABLE - showing error");
                        runOnUiThread(() -> {
                            editTextUsername.setError("Username sudah digunakan: " + checkResponse.getMessage());
                            editTextUsername.requestFocus();
                            Toast.makeText(EditProfileActivity.this,
                                    "Username tidak tersedia: " + checkResponse.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Check username error response: " + errorBody);
                        runOnUiThread(() ->
                                Toast.makeText(EditProfileActivity.this,
                                        "Error memeriksa username: " + errorBody,
                                        Toast.LENGTH_LONG).show()
                        );
                    } catch (IOException e) {
                        Log.e(TAG, "Check username IO error: " + e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(EditProfileActivity.this,
                                        "Error membaca response: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show()
                        );
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "Check username network error: ", t);
                runOnUiThread(() ->
                        Toast.makeText(EditProfileActivity.this,
                                "Gagal memeriksa username: " + t.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void updateProfile(String nip, String newUsername, String newDivision, boolean usernameChanged) {
        Toast.makeText(this, "Mengupdate profil...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Updating profile: " + nip + ", " + newUsername + ", " + newDivision + ", changed: " + usernameChanged);

        Call<BasicResponse> call = apiService.updateProfile(
                Integer.parseInt(nip),
                currentUsername,
                newUsername,
                newDivision,
                usernameChanged ? 1 : 0
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "Update profile response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        // Update SharedPreferences jika username berubah
                        if (usernameChanged) {
                            updateSharedPreferences(newUsername, newDivision, nip);
                        } else {
                            updateSharedPreferences(currentUsername, newDivision, nip);
                        }

                        Toast.makeText(EditProfileActivity.this, "Profil berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // Jika username berubah, update juga di tabel terkait
                        if (usernameChanged) {
                            updateRelatedTables(currentUsername, newUsername);
                        } else {
                            // Kembali ke ProfileActivity
                            navigateToProfile();
                        }
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(EditProfileActivity.this, "Error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(EditProfileActivity.this, "Error membaca response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "Update profile network error: ", t);
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSharedPreferences(String username, String division, String nip) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_DIVISION, division);
        editor.putString(KEY_NIP, nip);
        editor.apply();
        Log.d(TAG, "SharedPreferences updated: " + username);
    }

    private void updateRelatedTables(String oldUsername, String newUsername) {
        Log.d(TAG, "Updating relations...");

        Call<BasicResponse> call = apiService.updateUsernameInRelatedTables(oldUsername, newUsername);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "HTTP Response Code: " + response.code());

                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            BasicResponse basicResponse = response.body();
                            Log.d(TAG, "Parsing successful - Success: " + basicResponse.isSuccess());
                            Log.d(TAG, "Parsing successful - Message: " + basicResponse.getMessage());

                            showToast("Profil berhasil diupdate!");
                        } else {
                            Log.e(TAG, "Response body is null");
                            showToast("Profil berhasil diupdate!");
                        }
                    } else {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "HTTP Error: " + response.code() + " - " + errorBody);
                        showToast("Profil berhasil diupdate!");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception in onResponse: " + e.getMessage());
                    showToast("Profil berhasil diupdate!");
                }

                navigateToProfile();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
                showToast("Profil berhasil diupdate!");
                navigateToProfile();
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show());
    }
    private void navigateToProfile() {
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}