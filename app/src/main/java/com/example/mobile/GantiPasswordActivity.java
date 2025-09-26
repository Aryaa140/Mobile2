package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class GantiPasswordActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    Button btnSimpan, btnBatal;

    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private boolean isUsernameValid = false;
    private static final String TAG = "GantiPasswordActivity";

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ganti_password);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Gunakan RetrofitClient yang sudah ada
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editTextUsername = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.password2);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Set bottom navigation selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        // Setup TopAppBar navigation
        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(GantiPasswordActivity.this, ProfileActivity.class);
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
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // Auto-set username dari SharedPreferences
        String currentUsername = sharedPreferences.getString(KEY_USERNAME, "");
        if (!TextUtils.isEmpty(currentUsername)) {
            editTextUsername.setText(currentUsername);
            validateUsername(currentUsername);
        }

        // Setup TextWatcher untuk real-time username validation
        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                if (!TextUtils.isEmpty(username)) {
                    editTextUsername.removeCallbacks(validateRunnable);
                    editTextUsername.postDelayed(validateRunnable, 1000);
                } else {
                    isUsernameValid = false;
                    updateUIForUsernameValidation(false);
                }
            }

            private final Runnable validateRunnable = new Runnable() {
                @Override
                public void run() {
                    String username = editTextUsername.getText().toString().trim();
                    if (!TextUtils.isEmpty(username)) {
                        validateUsername(username);
                    }
                }
            };
        });

        // Setup button listeners
        btnSimpan.setOnClickListener(v -> gantiPassword());

        btnBatal.setOnClickListener(v -> {
            Intent intent = new Intent(GantiPasswordActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validateUsername(String username) {
        Log.d(TAG, "Validating username for password change: " + username);

        // GUNAKAN METHOD BARU UNTUK GANTI PASSWORD
        Call<PasswordCheckResponse> call = apiService.checkUsernameForPassword(username);
        call.enqueue(new Callback<PasswordCheckResponse>() {
            @Override
            public void onResponse(Call<PasswordCheckResponse> call, Response<PasswordCheckResponse> response) {
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    PasswordCheckResponse checkResponse = response.body();
                    Log.d(TAG, "Response - Success: " + checkResponse.isSuccess() + ", Exists: " + checkResponse.isExists());

                    if (checkResponse.isSuccess() && checkResponse.isExists()) {
                        // Username DITEMUKAN di database - VALID untuk ganti password
                        isUsernameValid = true;
                        runOnUiThread(() -> {
                            updateUIForUsernameValidation(true);
                            editTextUsername.setError(null);
                            Toast.makeText(GantiPasswordActivity.this, "Username valid", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Username TIDAK DITEMUKAN di database
                        isUsernameValid = false;
                        runOnUiThread(() -> {
                            updateUIForUsernameValidation(false);
                            editTextUsername.setError("Username tidak ditemukan");
                            Toast.makeText(GantiPasswordActivity.this,
                                    "Username tidak valid: " + checkResponse.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    // Handle error response
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Error response: " + errorBody);
                        handleError("Error: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        handleError("Error reading response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<PasswordCheckResponse> call, Throwable t) {
                Log.e(TAG, "Network error: ", t);
                handleError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleError(String errorMessage) {
        Log.e(TAG, errorMessage);
        runOnUiThread(() -> {
            isUsernameValid = false;
            updateUIForUsernameValidation(false);
            editTextUsername.setError("Gagal terhubung ke server");
            Toast.makeText(GantiPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        });
    }

    private void updateUIForUsernameValidation(boolean isValid) {
        editTextPassword.setEnabled(isValid);
        editTextConfirmPassword.setEnabled(isValid);
        btnSimpan.setEnabled(isValid);

        if (!isValid) {
            editTextPassword.setText("");
            editTextConfirmPassword.setText("");
        }
    }

    private void gantiPassword() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (validateInput(username, password, confirmPassword)) {
            updatePasswordViaAPI(username, password);
        }
    }

    private boolean validateInput(String username, String password, String confirmPassword) {
        if (username.isEmpty()) {
            editTextUsername.setError("Username harus diisi");
            editTextUsername.requestFocus();
            return false;
        }

        if (!isUsernameValid) {
            editTextUsername.setError("Username tidak valid");
            editTextUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password harus diisi");
            editTextPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Konfirmasi password harus diisi");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Password tidak cocok");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password minimal 6 karakter");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void updatePasswordViaAPI(String username, String newPassword) {
        Toast.makeText(this, "Mengubah password...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Updating password for: " + username);

        Call<BasicResponse> call = apiService.updatePassword(username, newPassword);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "Update password response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Toast.makeText(GantiPasswordActivity.this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                        clearForm();
                        Intent intent = new Intent(GantiPasswordActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(GantiPasswordActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(GantiPasswordActivity.this, "Error: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(GantiPasswordActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "Update password error: ", t);
                Toast.makeText(GantiPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        editTextUsername.setText("");
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
        isUsernameValid = false;
        updateUIForUsernameValidation(false);
    }
}