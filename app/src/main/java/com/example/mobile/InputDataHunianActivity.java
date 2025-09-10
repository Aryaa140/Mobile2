package com.example.mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.List;

public class InputDataHunianActivity extends AppCompatActivity {

    private EditText editTextNamaUnit, editTextHargaUnit;
    private Spinner spinnerReferensiProyek;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private DatabaseHelper databaseHelper;
    private String selectedProyek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_data_hunian);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi views
        initViews();

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Setup spinner dengan data proyek
        setupProyekSpinner();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextNamaUnit = findViewById(R.id.editTextNamaProyek); // Perhatikan ID-nya sama dengan XML
        spinnerReferensiProyek = findViewById(R.id.spinnerRoleNamaProyek);
        editTextHargaUnit = findViewById(R.id.editTextHargaUnit);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Ubah hint editTextNamaUnit agar sesuai dengan fungsinya
        editTextNamaUnit.setHint("Masukkan Nama Unit Hunian");
    }

    private void setupProyekSpinner() {
        // Ambil data nama proyek dari database
        List<String> proyekList = databaseHelper.getAllNamaProyek();

        if (proyekList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data proyek. Silakan tambahkan proyek terlebih dahulu.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Buat adapter untuk spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, proyekList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReferensiProyek.setAdapter(adapter);

        // Listener untuk spinner
        spinnerReferensiProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProyek = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProyek = null;
            }
        });
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanDataHunian();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void simpanDataHunian() {
        String namaUnit = editTextNamaUnit.getText().toString().trim();
        String hargaUnitStr = editTextHargaUnit.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(namaUnit)) {
            editTextNamaUnit.setError("Nama unit harus diisi");
            editTextNamaUnit.requestFocus();
            return;
        }

        if (selectedProyek == null) {
            Toast.makeText(this, "Pilih referensi proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(hargaUnitStr)) {
            editTextHargaUnit.setError("Harga unit harus diisi");
            editTextHargaUnit.requestFocus();
            return;
        }

        // Konversi harga unit ke double
        double hargaUnit;
        try {
            hargaUnit = Double.parseDouble(hargaUnitStr);
            if (hargaUnit <= 0) {
                editTextHargaUnit.setError("Harga unit harus lebih dari 0");
                editTextHargaUnit.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextHargaUnit.setError("Format harga tidak valid");
            editTextHargaUnit.requestFocus();
            return;
        }

        // Simpan data ke database
        long result = databaseHelper.addUnitHunian(namaUnit, selectedProyek, hargaUnit);

        if (result != -1) {
            Toast.makeText(this, "Data unit hunian berhasil disimpan", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Gagal menyimpan data unit hunian", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextNamaUnit.setText("");
        editTextHargaUnit.setText("");
        spinnerReferensiProyek.setSelection(0);
        editTextNamaUnit.requestFocus();
    }
}