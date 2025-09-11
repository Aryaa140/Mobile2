package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TambahProspekActivity extends AppCompatActivity {

    Button Simpan, Batal;
    MaterialToolbar TopAppBar;
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp, editTextAlamat;
    private Spinner spinnerReferensi, spinnerNPWP, spinnerBPJS;
    private Button btnSimpan, btnBatal;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    // Keys untuk SharedPreferences
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambahprospek);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        TopAppBar = findViewById(R.id.topAppBar);
        Simpan = findViewById(R.id.btnSimpan);
        Batal = findViewById(R.id.btnBatal);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        Batal.setOnClickListener(v -> {
            Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view
        editTextPenginput = findViewById(R.id.editTextProspek);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        spinnerNPWP = findViewById(R.id.spinnerNPWP);
        spinnerBPJS = findViewById(R.id.spinnerBPJS);

        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // OTOMATIS ISI NAMA PENGINPUT BERDASARKAN USER YANG LOGIN
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!TextUtils.isEmpty(username)) {
            editTextPenginput.setText(username);
            editTextPenginput.setEnabled(false); // Nonaktifkan edit, karena sudah otomatis terisi
        } else {
            // Fallback: Ambil dari intent jika tidak ada di SharedPreferences
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("USERNAME")) {
                username = intent.getStringExtra("USERNAME");
                editTextPenginput.setText(username);
                editTextPenginput.setEnabled(false);
            }
        }

        // Setup listeners
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanDataProspek();
            }
        });
    }

    private void simpanDataProspek() {
        // Ambil data dari form
        String penginput = editTextPenginput.getText().toString().trim();
        String nama = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();
        String statusNpwp = spinnerNPWP.getSelectedItem().toString();
        String statusBpjs = spinnerBPJS.getSelectedItem().toString();

        // Validasi input
        if (penginput.isEmpty()) {
            editTextPenginput.setError("Nama penginput harus diisi");
            editTextPenginput.requestFocus();
            return;
        }

        if (nama.isEmpty()) {
            editTextNama.setError("Nama lengkap harus diisi");
            editTextNama.requestFocus();
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

        // Validasi format email (hanya jika email tidak kosong)
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
            return;
        }

        // Validasi duplikasi data - TIDAK DIUBAH (SUDAH BENAR)
        if (databaseHelper.isProspekExists(nama, noHp)) {
            // Cek apakah duplikat nama atau nomor HP
            if (databaseHelper.isProspekExistsByName(nama)) {
                editTextNama.setError("Nama prospek sudah terdaftar");
                editTextNama.requestFocus();
            } else {
                editTextNoHp.setError("Nomor HP sudah terdaftar");
                editTextNoHp.requestFocus();
            }
            return;
        }

        // Simpan data ke database dengan parameter baru
        long result = databaseHelper.addProspek(penginput, nama, email, noHp, alamat, referensi, statusNpwp, statusBpjs);

        if (result != -1) {
            Toast.makeText(this, "Data prospek berhasil disimpan", Toast.LENGTH_SHORT).show();
            clearForm();

            // Kembali ke BerandaActivity setelah berhasil menyimpan
            Intent intent = new Intent(TambahProspekActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Gagal menyimpan data prospek", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        // Jangan reset editTextPenginput karena sudah terisi otomatis
        editTextNama.setText("");
        editTextEmail.setText("");
        editTextNoHp.setText("");
        editTextAlamat.setText("");
        spinnerReferensi.setSelection(0);
        spinnerNPWP.setSelection(0);
        spinnerBPJS.setSelection(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}