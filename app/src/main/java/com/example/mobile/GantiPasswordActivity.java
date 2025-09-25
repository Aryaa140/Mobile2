package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
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
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
public class GantiPasswordActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    Button btnSimpan, btnBatal;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ganti_password);

        // Inisialisasi DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

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

        // Setup button listeners
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gantiPassword();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kembali ke ProfileActivity
                Intent intent = new Intent(GantiPasswordActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void gantiPassword() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validasi input
        if (username.isEmpty()) {
            editTextUsername.setError("Username harus diisi");
            editTextUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password harus diisi");
            editTextPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Konfirmasi password harus diisi");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Password tidak cocok");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Cek apakah username ada di database
        if (!databaseHelper.checkUsername(username)) {
            editTextUsername.setError("Username tidak ditemukan");
            editTextUsername.requestFocus();
            return;
        }

        // Update password di database
        boolean success = updatePasswordInDatabase(username, password);

        if (success) {
            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
            clearForm();
            // Kembali ke ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Gagal mengubah password", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updatePasswordInDatabase(String username, String newPassword) {
        // Method untuk update password di database
        // Karena DatabaseHelper tidak memiliki method updatePassword, kita buat manual

        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD, newPassword);

        String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);
        db.close();

        return rowsAffected > 0;
    }

    private void clearForm() {
        editTextUsername.setText("");
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}