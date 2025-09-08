package com.example.mobile;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import java.util.Calendar;

public class InputDataProyekActivity extends AppCompatActivity {

    Button btnPilihTanggal, btnSimpan, btnBatal;
    MaterialToolbar TopAppBar;
    BottomNavigationView bottomNavigationView;
    EditText editTextNamaProyek, editTextLokasiProyek;
    Spinner spinnerStatusProyek;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_data_proyek);

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view
        btnPilihTanggal = findViewById(R.id.btnPilihTanggal);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        editTextNamaProyek = findViewById(R.id.editTextNamaProyek);
        editTextLokasiProyek = findViewById(R.id.editTextLokasiProyek);
        spinnerStatusProyek = findViewById(R.id.spinnerRoleProspek);

        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup spinner untuk status proyek
        setupStatusSpinner();

        // Pilih tanggal dan waktu
        btnPilihTanggal.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    InputDataProyekActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                InputDataProyekActivity.this,
                                (timeView, hourOfDay, minuteOfHour) -> {
                                    String dateTime = dayOfMonth + "/" + (month1 + 1) + "/" + year1
                                            + " " + String.format("%02d:%02d", hourOfDay, minuteOfHour);
                                    btnPilihTanggal.setText(dateTime);
                                }, hour, minute, true);
                        timePickerDialog.show();
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Simpan data proyek
        btnSimpan.setOnClickListener(v -> {
            simpanDataProyek();
        });

        // Batal input
        btnBatal.setOnClickListener(v -> {
            Intent intent = new Intent(InputDataProyekActivity.this, InputActivity.class);
            startActivity(intent);
            finish();
        });

        // Navigasi toolbar
        TopAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(InputDataProyekActivity.this, InputActivity.class);
            startActivity(intent);
            finish();
        });

        // Bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
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

    private void setupStatusSpinner() {
        // Opsi status proyek (sesuaikan dengan array yang ada di strings.xml)
        String[] statusOptions = getResources().getStringArray(R.array.opsi_spinnerStatusProyek);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusProyek.setAdapter(adapter);
    }

    private void simpanDataProyek() {
        // Validasi input
        String namaProyek = editTextNamaProyek.getText().toString().trim();
        String lokasiProyek = editTextLokasiProyek.getText().toString().trim();
        String tanggalProyek = btnPilihTanggal.getText().toString();
        String statusProyek = spinnerStatusProyek.getSelectedItem().toString();

        // Cek apakah semua field terisi
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

        if (tanggalProyek.equals("Input Tanggal")) {
            Toast.makeText(this, "Pilih tanggal proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simpan data ke database
        long result = databaseHelper.addProyek(namaProyek, lokasiProyek, tanggalProyek, statusProyek);

        if (result != -1) {
            Toast.makeText(this, "Data proyek berhasil disimpan", Toast.LENGTH_SHORT).show();

            // Kosongkan form
            editTextNamaProyek.setText("");
            editTextLokasiProyek.setText("");
            btnPilihTanggal.setText("Input Tanggal");
            spinnerStatusProyek.setSelection(0);

            // Redirect ke halaman input activity
            Intent intent = new Intent(InputDataProyekActivity.this, InputActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Gagal menyimpan data proyek", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Tutup koneksi database
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}