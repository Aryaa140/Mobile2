package com.example.mobile;

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

public class EditDataProyekActivity extends AppCompatActivity {

    private EditText editTextNamaProyek, editTextLokasiProyek;
    private Spinner spinnerStatusProyek;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private DatabaseHelper databaseHelper;
    private int proyekId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_proyek);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi views
        initViews();

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Dapatkan ID proyek dari intent
        proyekId = getIntent().getIntExtra("PROYEK_ID", -1);

        if (proyekId == -1) {
            Toast.makeText(this, "Error: Proyek tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load data proyek
        loadProyekData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextNamaProyek = findViewById(R.id.editTextNamaProyek);
        editTextLokasiProyek = findViewById(R.id.editTextLokasiProyek);
        spinnerStatusProyek = findViewById(R.id.spinnerRoleProspek);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opsi_spinnerStatusProyek, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusProyek.setAdapter(adapter);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());
    }

    private void loadProyekData() {
        DatabaseHelper.Proyek proyek = databaseHelper.getProyekById(proyekId);
        if (proyek != null) {
            editTextNamaProyek.setText(proyek.getNamaProyek());
            editTextLokasiProyek.setText(proyek.getLokasiProyek());

            // Set selected status in spinner
            String status = proyek.getStatusProyek();
            ArrayAdapter adapter = (ArrayAdapter) spinnerStatusProyek.getAdapter();
            int position = adapter.getPosition(status);
            if (position >= 0) {
                spinnerStatusProyek.setSelection(position);
            }
        }
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProyek();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateProyek() {
        String namaProyek = editTextNamaProyek.getText().toString().trim();
        String lokasiProyek = editTextLokasiProyek.getText().toString().trim();
        String statusProyek = spinnerStatusProyek.getSelectedItem().toString();

        if (namaProyek.isEmpty()) {
            editTextNamaProyek.setError("Nama proyek harus diisi");
            editTextNamaProyek.requestFocus();
            return;
        }

        if (lokasiProyek.isEmpty()) {
            editTextLokasiProyek.setError("Lokasi proyek harus diisi");
            editTextLokasiProyek.requestFocus();
            return;
        }

        int result = databaseHelper.updateProyek(proyekId, namaProyek, lokasiProyek, statusProyek);
        if (result > 0) {
            Toast.makeText(this, "Proyek berhasil diperbarui", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Gagal memperbarui proyek", Toast.LENGTH_SHORT).show();
        }
    }
}