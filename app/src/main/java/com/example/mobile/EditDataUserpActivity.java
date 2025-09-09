package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class EditDataUserpActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int userProspekId;
    private String selectedNamaProyek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_data_userp);

        dbHelper = new DatabaseHelper(this);

        // Get data from intent
        Intent intent = getIntent();
        userProspekId = intent.getIntExtra("USER_PROSPEK_ID", -1);
        String penginput = intent.getStringExtra("PENGINPUT");
        String nama = intent.getStringExtra("NAMA");
        String email = intent.getStringExtra("EMAIL");
        String noHp = intent.getStringExtra("NO_HP");
        String alamat = intent.getStringExtra("ALAMAT");
        String namaProyek = intent.getStringExtra("NAMA_PROYEK");
        double uangTandaJadi = intent.getDoubleExtra("UANG_TANDA_JADI", 0);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.topAppBar);
        EditText editTextPenginput = findViewById(R.id.editTextPenginput);
        EditText editTextNama = findViewById(R.id.editTextNama);
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextNoHp = findViewById(R.id.editTextNoHp);
        EditText editTextAlamat = findViewById(R.id.editTextAlamat);
        Spinner spinnerProyek = findViewById(R.id.spinnerRoleRefrensiProyek);
        EditText editTextUangTandaJadi = findViewById(R.id.editTextUangPengadaan);
        Button btnSimpan = findViewById(R.id.btnSimpan);
        Button btnBatal = findViewById(R.id.btnBatal);

        // Set data to views
        editTextPenginput.setText(penginput);
        editTextNama.setText(nama);
        editTextEmail.setText(email);
        editTextNoHp.setText(noHp);
        editTextAlamat.setText(alamat);
        editTextUangTandaJadi.setText(String.valueOf(uangTandaJadi));

        // Disable penginput field
        editTextPenginput.setEnabled(false);

        // Load proyek data
        List<String> proyekList = dbHelper.getAllNamaProyek();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, proyekList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(adapter);

        // Set selected proyek
        if (namaProyek != null && !namaProyek.isEmpty()) {
            int position = adapter.getPosition(namaProyek);
            if (position >= 0) {
                spinnerProyek.setSelection(position);
            }
        }
        selectedNamaProyek = namaProyek;

        spinnerProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNamaProyek = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedNamaProyek = "";
            }
        });

        // Set toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Button listeners
        btnSimpan.setOnClickListener(v -> {
            String newNama = editTextNama.getText().toString().trim();
            String newEmail = editTextEmail.getText().toString().trim();
            String newNoHp = editTextNoHp.getText().toString().trim();
            String newAlamat = editTextAlamat.getText().toString().trim();
            String uangStr = editTextUangTandaJadi.getText().toString().trim();

            if (newNama.isEmpty() || uangStr.isEmpty()) {
                Toast.makeText(this, "Nama dan uang tanda jadi harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedNamaProyek.isEmpty()) {
                Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newUangTandaJadi = Double.parseDouble(uangStr);

                int result = dbHelper.updateUserProspek(
                        userProspekId,
                        penginput,
                        newNama,
                        newEmail,
                        newNoHp,
                        newAlamat,
                        selectedNamaProyek,
                        newUangTandaJadi
                );

                if (result > 0) {
                    Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Gagal mengupdate data", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Format uang tanda jadi tidak valid", Toast.LENGTH_SHORT).show();
            }
        });

        btnBatal.setOnClickListener(v -> finish());

        // EdgeToEdge insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}