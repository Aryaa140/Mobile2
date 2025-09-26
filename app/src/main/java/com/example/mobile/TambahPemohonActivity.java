package com.example.mobile;

import android.content.Intent;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class TambahPemohonActivity extends AppCompatActivity {
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;

    private Spinner spinnerProspek, spinnerReferensiProyek, spinnerUnitHunian, spinnerTipeUnit, spinnerStatusPembayaran;
    private EditText editTextPenginput, editTextEmail, editTextNoHp, editTextAlamat, editTextUangMuka;
    private Button btnSimpan, btnBatal;
    private DatabaseHelper databaseHelper;

    private String selectedProspekNama;
    private String selectedReferensiProyek;
    private String selectedUnitHunian;
    private String selectedTipeUnit;
    private String selectedStatusPembayaran;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambah_pemohon);

        // Inisialisasi views
        initViews();

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Setup spinners
        setupSpinnerProspek();
        setupSpinnerReferensiProyek();
        setupSpinnerTipeUnit();
        setupSpinnerStatusPembayaran();

        // Setup listeners
        setupListeners();

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(TambahPemohonActivity.this, NewBeranda.class);
            startActivity(intent);
            finish();
        });

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

    private void initViews() {
        spinnerProspek = findViewById(R.id.spinnerRoleProspek);
        spinnerReferensiProyek = findViewById(R.id.spinnerRoleRefrensiProyek);
        spinnerUnitHunian = findViewById(R.id.spinnerRoleUnitHuniah);
        spinnerTipeUnit = findViewById(R.id.spinnerRoleTipeUnit);
        spinnerStatusPembayaran = findViewById(R.id.spinnerRoleStatusPembayaran);

        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextUangMuka = findViewById(R.id.editTextUangMuka);

        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupSpinnerProspek() {
        // Ambil data nama dari tabel user_prospek
        List<DatabaseHelper.UserProspek> userProspekList = databaseHelper.getAllUserProspek();

        if (userProspekList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data user prospek", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ekstrak hanya nama-nama untuk spinner
        String[] namaProspekArray = new String[userProspekList.size()];
        for (int i = 0; i < userProspekList.size(); i++) {
            namaProspekArray[i] = userProspekList.get(i).getNama();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, namaProspekArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProspek.setAdapter(adapter);

        spinnerProspek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProspekNama = parent.getItemAtPosition(position).toString();
                // Isi otomatis data dari user_prospek berdasarkan nama yang dipilih
                fillUserProspekData(selectedProspekNama);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProspekNama = null;
            }
        });
    }

    private void fillUserProspekData(String nama) {
        // Cari data user_prospek berdasarkan nama
        List<DatabaseHelper.UserProspek> userProspekList = databaseHelper.getAllUserProspek();
        for (DatabaseHelper.UserProspek userProspek : userProspekList) {
            if (userProspek.getNama().equals(nama)) {
                editTextPenginput.setText(userProspek.getPenginput());
                editTextEmail.setText(userProspek.getEmail());
                editTextNoHp.setText(userProspek.getNoHp());
                editTextAlamat.setText(userProspek.getAlamat());
                break;
            }
        }
    }

    private void setupSpinnerReferensiProyek() {
        // Ambil data nama proyek dari database
        List<String> proyekList = databaseHelper.getAllNamaProyek();

        if (proyekList.isEmpty()) {
            Toast.makeText(this, "Tidak ada data proyek", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, proyekList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReferensiProyek.setAdapter(adapter);

        spinnerReferensiProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReferensiProyek = parent.getItemAtPosition(position).toString();
                // Setelah proyek dipilih, update spinner unit hunian
                setupSpinnerUnitHunian(selectedReferensiProyek);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedReferensiProyek = null;
            }
        });
    }

    private void setupSpinnerUnitHunian(String namaProyek) {
        // Ambil data unit hunian berdasarkan proyek yang dipilih
        List<DatabaseHelper.UnitHunian> unitHunianList = databaseHelper.getUnitHunianByProyek(namaProyek);

        if (unitHunianList.isEmpty()) {
            Toast.makeText(this, "Tidak ada unit hunian untuk proyek ini", Toast.LENGTH_SHORT).show();
            spinnerUnitHunian.setAdapter(null);
            return;
        }

        // Ekstrak hanya nama-nama unit untuk spinner
        String[] namaUnitArray = new String[unitHunianList.size()];
        for (int i = 0; i < unitHunianList.size(); i++) {
            namaUnitArray[i] = unitHunianList.get(i).getNamaUnit();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, namaUnitArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnitHunian.setAdapter(adapter);

        spinnerUnitHunian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUnitHunian = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUnitHunian = null;
            }
        });
    }

    private void setupSpinnerTipeUnit() {
        // Spinner tipe unit sudah menggunakan entries dari XML (@array/opsi_spinnerTipeUnitHunian)
        spinnerTipeUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTipeUnit = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTipeUnit = null;
            }
        });
    }

    private void setupSpinnerStatusPembayaran() {
        // Spinner status pembayaran sudah menggunakan entries dari XML (@array/opsi_spinnerStatusPembayaran)
        spinnerStatusPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatusPembayaran = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStatusPembayaran = null;
            }
        });
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanDataPemohon();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void simpanDataPemohon() {
        String penginput = editTextPenginput.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String uangMukaStr = editTextUangMuka.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(penginput)) {
            editTextPenginput.setError("Nama penginput harus diisi");
            editTextPenginput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email harus diisi");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(noHp)) {
            editTextNoHp.setError("No. HP harus diisi");
            editTextNoHp.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(alamat)) {
            editTextAlamat.setError("Alamat harus diisi");
            editTextAlamat.requestFocus();
            return;
        }

        if (selectedProspekNama == null) {
            Toast.makeText(this, "Pilih prospek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedReferensiProyek == null) {
            Toast.makeText(this, "Pilih referensi proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUnitHunian == null) {
            Toast.makeText(this, "Pilih unit hunian terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTipeUnit == null) {
            Toast.makeText(this, "Pilih tipe unit terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStatusPembayaran == null) {
            Toast.makeText(this, "Pilih status pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(uangMukaStr)) {
            editTextUangMuka.setError("Uang muka harus diisi");
            editTextUangMuka.requestFocus();
            return;
        }

        // Konversi uang muka ke double
        double uangMuka;
        try {
            uangMuka = Double.parseDouble(uangMukaStr);
            if (uangMuka < 0) {
                editTextUangMuka.setError("Uang muka tidak boleh negatif");
                editTextUangMuka.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            editTextUangMuka.setError("Format uang muka tidak valid");
            editTextUangMuka.requestFocus();
            return;
        }

        // Simpan data ke database
        long result = databaseHelper.addPemohon(
                selectedProspekNama, // nama pemohon
                email,
                noHp,
                alamat,
                selectedReferensiProyek,
                selectedUnitHunian,
                selectedTipeUnit,
                selectedStatusPembayaran,
                uangMuka
        );

        if (result != -1) {
            Toast.makeText(this, "Data pemohon berhasil disimpan", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Gagal menyimpan data pemohon", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        editTextPenginput.setText("");
        editTextEmail.setText("");
        editTextNoHp.setText("");
        editTextAlamat.setText("");
        editTextUangMuka.setText("");

        spinnerProspek.setSelection(0);
        spinnerReferensiProyek.setSelection(0);
        spinnerUnitHunian.setAdapter(null);
        spinnerTipeUnit.setSelection(0);
        spinnerStatusPembayaran.setSelection(0);
    }
}