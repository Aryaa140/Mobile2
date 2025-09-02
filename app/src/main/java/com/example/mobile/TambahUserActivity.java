package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class TambahUserActivity extends AppCompatActivity {

    Button Simpan, Batal;
    MaterialToolbar TopAppBar;
    private EditText editTextNama, editTextEmail, editTextNoHp, editTextAlamat;
    private Spinner spinnerReferensi;
    private Button btnSimpan, btnBatal;
    private DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambahuser);


        TopAppBar = findViewById(R.id.topAppBar);
        Simpan = findViewById(R.id.btnSimpan);
        Batal = findViewById(R.id.btnBatal);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(TambahUserActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        Batal.setOnClickListener(v -> {
            Intent intent = new Intent(TambahUserActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view - LANGSUNG di onCreate
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup listeners - LANGSUNG di onCreate
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanDataProspek();
            }
        });

        /*btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Tutup activity
            }
        });*/
    }
    private void simpanDataProspek() {
        // Ambil data dari form
        String nama = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();

        // Validasi input
        if (nama.isEmpty()) {
            editTextNama.setError("Nama lengkap harus diisi");
            editTextNama.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email harus diisi");
            editTextEmail.requestFocus();
            return;
        }

        if (noHp.isEmpty()) {
            editTextNoHp.setError("No. Handphone harus diisi");
            editTextNoHp.requestFocus();
            return;
        }

        if (alamat.isEmpty()) {
            editTextAlamat.setError("Alamat harus diisi");
            editTextAlamat.requestFocus();
            return;
        }

        // Validasi format email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return;
        }

        // Simpan data ke database
        long result = databaseHelper.addProspek(nama, email, noHp, alamat, referensi);

        if (result != -1) {
            Toast.makeText(this, "Data prospek berhasil disimpan", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Gagal menyimpan data prospek", Toast.LENGTH_SHORT).show();
        }
    }
    private void clearForm() {
        editTextNama.setText("");
        editTextEmail.setText("");
        editTextNoHp.setText("");
        editTextAlamat.setText("");
        spinnerReferensi.setSelection(0); // Reset ke pilihan pertama
    }
    protected void onDestroy() {
        super.onDestroy(); // ✅ BENAR: onDestroy() bukan onOnDestroy()
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}