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

public class EditDataPemohonActivity extends AppCompatActivity {

    private EditText editTextPenginput, editTextEmail, editTextNoHp, editTextAlamat, editTextUangMuka;
    private Spinner spinnerReferensiProyek, spinnerUnitHunian, spinnerTipeUnit, spinnerStatusPembayaran;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private DatabaseHelper databaseHelper;

    private String selectedReferensiProyek;
    private String selectedUnitHunian;
    private String selectedTipeUnit;
    private String selectedStatusPembayaran;
    private int pemohonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_pemohon);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi views
        initViews();

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Dapatkan ID pemohon dari intent
        pemohonId = getIntent().getIntExtra("PEMOHON_ID", -1);

        if (pemohonId == -1) {
            Toast.makeText(this, "Error: Data pemohon tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup spinners
        setupSpinnerReferensiProyek();
        setupSpinnerTipeUnit();
        setupSpinnerStatusPembayaran();

        // Load data pemohon
        loadPemohonData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextUangMuka = findViewById(R.id.editTextUangMuka);

        spinnerReferensiProyek = findViewById(R.id.spinnerRoleRefrensiProyek);
        spinnerUnitHunian = findViewById(R.id.spinnerRoleUnitHuniah);
        spinnerTipeUnit = findViewById(R.id.spinnerRoleTipeUnit);
        spinnerStatusPembayaran = findViewById(R.id.spinnerRoleStatusPembayaran);

        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        // Setup toolbar
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Set hints
        editTextPenginput.setHint("Nama Penginput");
        editTextEmail.setHint("Email");
        editTextNoHp.setHint("No. HP");
        editTextAlamat.setHint("Alamat");
        editTextUangMuka.setHint("Uang Muka");
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

    private void loadPemohonData() {
        DatabaseHelper.Pemohon pemohon = databaseHelper.getPemohonById(pemohonId);
        if (pemohon != null) {
            editTextPenginput.setText(pemohon.getNamaPemohon());
            editTextEmail.setText(pemohon.getEmailPemohon());
            editTextNoHp.setText(pemohon.getNoHpPemohon());
            editTextAlamat.setText(pemohon.getAlamatPemohon());
            editTextUangMuka.setText(String.valueOf(pemohon.getUangMuka()));

            // Set selected proyek in spinner
            ArrayAdapter adapterProyek = (ArrayAdapter) spinnerReferensiProyek.getAdapter();
            int positionProyek = adapterProyek.getPosition(pemohon.getReferensiProyek());
            if (positionProyek >= 0) {
                spinnerReferensiProyek.setSelection(positionProyek);
            }

            // Set selected unit hunian in spinner (setelah proyek dipilih)
            if (pemohon.getReferensiProyek() != null) {
                setupSpinnerUnitHunian(pemohon.getReferensiProyek());
                // Tunggu sebentar untuk adapter unit hunian terisi
                spinnerUnitHunian.postDelayed(() -> {
                    ArrayAdapter adapterUnit = (ArrayAdapter) spinnerUnitHunian.getAdapter();
                    if (adapterUnit != null) {
                        int positionUnit = adapterUnit.getPosition(pemohon.getReferensiUnitHunian());
                        if (positionUnit >= 0) {
                            spinnerUnitHunian.setSelection(positionUnit);
                        }
                    }
                }, 100);
            }

            // Set selected tipe unit in spinner
            ArrayAdapter adapterTipe = (ArrayAdapter) spinnerTipeUnit.getAdapter();
            int positionTipe = adapterTipe.getPosition(pemohon.getTipeUnitHunian());
            if (positionTipe >= 0) {
                spinnerTipeUnit.setSelection(positionTipe);
            }

            // Set selected status pembayaran in spinner
            ArrayAdapter adapterStatus = (ArrayAdapter) spinnerStatusPembayaran.getAdapter();
            int positionStatus = adapterStatus.getPosition(pemohon.getStatusPembayaran());
            if (positionStatus >= 0) {
                spinnerStatusPembayaran.setSelection(positionStatus);
            }
        }
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePemohon();
            }
        });

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updatePemohon() {
        String namaPemohon = editTextPenginput.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String uangMukaStr = editTextUangMuka.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(namaPemohon)) {
            editTextPenginput.setError("Nama pemohon harus diisi");
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

        // Update data ke database
        int result = databaseHelper.updatePemohon(
                pemohonId,
                namaPemohon,
                email,
                noHp,
                alamat,
                selectedReferensiProyek,
                selectedUnitHunian,
                selectedTipeUnit,
                selectedStatusPembayaran,
                uangMuka
        );

        if (result > 0) {
            Toast.makeText(this, "Data pemohon berhasil diperbarui", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Gagal memperbarui data pemohon", Toast.LENGTH_SHORT).show();
        }
    }
}