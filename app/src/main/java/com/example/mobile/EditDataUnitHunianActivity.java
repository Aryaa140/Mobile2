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

public class EditDataUnitHunianActivity extends AppCompatActivity {

    private EditText editTextNamaUnit, editTextHargaUnit;
    private Spinner spinnerReferensiProyek;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private DatabaseHelper databaseHelper;
    private String selectedProyek;
    private int unitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_unit_hunian);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi views
        initViews();

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Dapatkan ID unit dari intent
        unitId = getIntent().getIntExtra("UNIT_ID", -1);

        if (unitId == -1) {
            Toast.makeText(this, "Error: Unit hunian tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup spinner dengan data proyek
        setupProyekSpinner();

        // Load data unit hunian
        loadUnitHunianData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextNamaUnit = findViewById(R.id.editTextNamaProyek); // Sesuaikan dengan XML
        spinnerReferensiProyek = findViewById(R.id.spinnerRoleNamaProyek); // Sesuaikan dengan XML
        editTextHargaUnit = findViewById(R.id.editTextHargaUnit);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Ubah hint editTextNamaUnit agar sesuai dengan fungsinya
        editTextNamaUnit.setHint("Nama Unit Hunian");
        editTextHargaUnit.setHint("Harga Unit Hunian");
    }

    private void setupProyekSpinner() {
        // Ambil data nama proyek dari database
        List<String> proyekList = databaseHelper.getAllNamaProyek();

        if (proyekList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data proyek", Toast.LENGTH_SHORT).show();
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

    private void loadUnitHunianData() {
        DatabaseHelper.UnitHunian unitHunian = databaseHelper.getUnitHunianById(unitId);
        if (unitHunian != null) {
            editTextNamaUnit.setText(unitHunian.getNamaUnit());
            editTextHargaUnit.setText(String.valueOf(unitHunian.getHargaUnit()));

            // Set selected proyek in spinner
            ArrayAdapter adapter = (ArrayAdapter) spinnerReferensiProyek.getAdapter();
            int position = adapter.getPosition(unitHunian.getReferensiProyek());
            if (position >= 0) {
                spinnerReferensiProyek.setSelection(position);
            }
        }
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUnitHunian();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateUnitHunian() {
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

        // Update data ke database
        int result = databaseHelper.updateUnitHunian(unitId, namaUnit, selectedProyek, hargaUnit);

        if (result > 0) {
            Toast.makeText(this, "Data unit hunian berhasil diperbarui", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Gagal memperbarui data unit hunian", Toast.LENGTH_SHORT).show();
        }
    }
}