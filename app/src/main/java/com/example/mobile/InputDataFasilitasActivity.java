package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

public class InputDataFasilitasActivity extends AppCompatActivity {

    MaterialToolbar TopAppBar;
    Button Simpan, Batal;
    BottomNavigationView bottomNavigationView;

    EditText editTextNamaFasilitas, editTextLokasiFasilitas;
    Spinner spinnerNamaProyek, spinnerStatusFasilitas;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_data_fasilitas);

        // Init
        TopAppBar = findViewById(R.id.topAppBar);
        Simpan = findViewById(R.id.btnSimpan);
        Batal = findViewById(R.id.btnBatal);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        editTextNamaFasilitas = findViewById(R.id.editTextNamaProyek); // ini harusnya editTextNamaFasilitas di xml
        editTextLokasiFasilitas = findViewById(R.id.editTextLokasiFasilitas);
        spinnerNamaProyek = findViewById(R.id.spinnerRoleNamaProyek);
        spinnerStatusFasilitas = findViewById(R.id.spinnerRoleProspek);

        dbHelper = new DatabaseHelper(this);

        // Load data proyek ke spinner
        loadNamaProyekSpinner();

        // Button Simpan
        Simpan.setOnClickListener(v -> {
            String namaFasilitas = editTextNamaFasilitas.getText().toString().trim();
            String lokasiFasilitas = editTextLokasiFasilitas.getText().toString().trim();
            String namaProyek = spinnerNamaProyek.getSelectedItem().toString();
            String statusFasilitas = spinnerStatusFasilitas.getSelectedItem().toString();

            if (namaFasilitas.isEmpty() || lokasiFasilitas.isEmpty()) {
                Toast.makeText(this, "Isi semua field!", Toast.LENGTH_SHORT).show();
                return;
            }

            long result = dbHelper.addFasilitas(namaFasilitas, namaProyek, lokasiFasilitas, statusFasilitas);
            if (result != -1) {
                Toast.makeText(this, "Fasilitas berhasil ditambahkan!", Toast.LENGTH_SHORT).show();

                finish(); // kembali ke activity sebelumnya
            } else {
                Toast.makeText(this, "Gagal menambahkan fasilitas", Toast.LENGTH_SHORT).show();
            }
        });

        // Button Batal
        Batal.setOnClickListener(v -> {
            Intent intent = new Intent(InputDataFasilitasActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(InputDataFasilitasActivity.this, BerandaActivity.class);
            startActivity(intent);
            finish();
        });

        // Bottom nav
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadNamaProyekSpinner() {
        List<String> namaProyekList = dbHelper.getAllNamaProyek();

        if (namaProyekList.isEmpty()) {
            namaProyekList.add("Belum ada proyek");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, namaProyekList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNamaProyek.setAdapter(adapter);
    }
}
