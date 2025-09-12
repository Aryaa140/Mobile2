package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
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

public class EditProfileActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    EditText editTextNoNip, editTextUsername;
    Spinner spinnerDivisi;
    Button btnSimpan, btnBatal;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Inisialisasi DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        editTextNoNip = findViewById(R.id.noNip);
        editTextUsername = findViewById(R.id.username);
        spinnerDivisi = findViewById(R.id.spinnerOpsi);
        btnSimpan = findViewById(R.id.btnSimpan);
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
                startActivity(new Intent(this, BerandaActivity.class));
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
                editProfil();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kembali ke ProfileActivity
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
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

    private void setupSpinnerDivisi() {
        // Ambil data divisi dari DatabaseHelper
        String[] divisions = databaseHelper.getAllDivisions();

        // Buat adapter untuk spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                divisions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivisi.setAdapter(adapter);
    }

    private void editProfil() {
        String nip = editTextNoNip.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String division = spinnerDivisi.getSelectedItem().toString();

        // Validasi input
        if (nip.isEmpty()) {
            editTextNoNip.setError("No. NIP harus diisi");
            editTextNoNip.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            editTextUsername.setError("Username harus diisi");
            editTextUsername.requestFocus();
            return;
        }

        // Validasi format NIP (angka saja)
        if (!nip.matches("\\d+")) {
            editTextNoNip.setError("NIP harus berupa angka");
            editTextNoNip.requestFocus();
            return;
        }

        // Cek apakah NIP ada di database
        if (!databaseHelper.checkNip(nip)) {
            editTextNoNip.setError("NIP tidak ditemukan");
            editTextNoNip.requestFocus();
            return;
        }

        // Cek apakah username sudah digunakan (kecuali oleh user yang sama)
        if (databaseHelper.checkUsername(username)) {
            // Jika username sudah ada, cek apakah milik user yang sama
            DatabaseHelper.User existingUser = databaseHelper.getUserData(username);
            if (existingUser != null && !existingUser.getNip().equals(nip)) {
                editTextUsername.setError("Username sudah digunakan");
                editTextUsername.requestFocus();
                return;
            }
        }

        // Update data user di database
        boolean success = updateUserProfile(nip, username, division);

        if (success) {
            Toast.makeText(this, "Profil berhasil diupdate", Toast.LENGTH_SHORT).show();
            // Kembali ke ProfileActivity
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Gagal mengupdate profil", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updateUserProfile(String nip, String newUsername, String newDivision) {
        // Method untuk update data user di database
        // Karena DatabaseHelper tidak memiliki method updateUser, kita buat manual

        android.database.sqlite.SQLiteDatabase db = databaseHelper.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, newUsername);
        values.put(DatabaseHelper.COLUMN_DIVISION, newDivision);

        String selection = DatabaseHelper.COLUMN_NIP + " = ?";
        String[] selectionArgs = {nip};

        try {
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    selection,
                    selectionArgs
            );
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Optional: Method untuk load data user berdasarkan NIP
    private void loadUserData(String nip) {
        // Cari user berdasarkan NIP
        android.database.sqlite.SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_DIVISION
        };

        String selection = DatabaseHelper.COLUMN_NIP + " = ?";
        String[] selectionArgs = {nip};

        android.database.Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            String division = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIVISION));

            // Set data ke form
            editTextUsername.setText(username);

            // Set spinner selection
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDivisi.getAdapter();
            int position = adapter.getPosition(division);
            if (position >= 0) {
                spinnerDivisi.setSelection(position);
            }

            cursor.close();
        }

        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}