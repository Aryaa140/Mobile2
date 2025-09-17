package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextNoNip, editTextPassword, editTextPassword2;
    private Spinner spinnerDivision;
    private Button buttonBuatAkun, buttonKembali;
    private DatabaseHelper databaseHelper;
    private String selectedDivision = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view
        editTextUsername = findViewById(R.id.username);
        spinnerDivision = findViewById(R.id.spinnerOpsi);
        editTextNoNip = findViewById(R.id.noNip);
        editTextPassword = findViewById(R.id.password);
        editTextPassword2 = findViewById(R.id.password2);
        buttonBuatAkun = findViewById(R.id.btnBuatAkun);
        buttonKembali = findViewById(R.id.btnKembali);

        // Setup spinner dengan divisi dari database helper
        setupDivisionSpinner();

        // Menangani klik tombol buat akun
        buttonBuatAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validasi input
                if (validateInput()) {
                    // Daftarkan pengguna baru
                    String username = editTextUsername.getText().toString().trim();
                    String nip = editTextNoNip.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    if (databaseHelper.addUser(username, selectedDivision, nip, password)) {

                        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("username", username);
                        editor.putString("division", selectedDivision);
                        editor.putString("nip", nip);
                        editor.apply();

                        // Pendaftaran berhasil
                        Toast.makeText(SignUpActivity.this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show();

                        // Kembali ke activity login
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Menutup activity signup
                    } else {
                        // Pendaftaran gagal
                        Toast.makeText(SignUpActivity.this, "Gagal membuat akun. Coba lagi.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Menangani klik tombol kembali
        buttonKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kembali ke activity login
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Menutup activity signup
            }
        });
    }

    // Method untuk setup spinner dengan divisi
    private void setupDivisionSpinner() {
        // Ambil divisi dari database helper
        String[] divisions = databaseHelper.getAllDivisions();

        // Buat array dengan tambahan "Pilih Divisi" di awal
        String[] spinnerItems = new String[divisions.length + 1];
        spinnerItems[0] = "Pilih Divisi";
        System.arraycopy(divisions, 0, spinnerItems, 1, divisions.length);

        // Create an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerDivision.setAdapter(adapter);

        // Set listener untuk spinner
        spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedDivision = parent.getItemAtPosition(position).toString();
                } else {
                    selectedDivision = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDivision = "";
            }
        });
    }

    // Method untuk validasi input
    private boolean validateInput() {
        String username = editTextUsername.getText().toString().trim();
        String nip = editTextNoNip.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String password2 = editTextPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username tidak boleh kosong");
            editTextUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(selectedDivision) || selectedDivision.equals("Pilih Divisi")) {
            Toast.makeText(this, "Silakan pilih divisi", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(nip)) {
            editTextNoNip.setError("No. NIP tidak boleh kosong");
            editTextNoNip.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password tidak boleh kosong");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password minimal 6 karakter");
            editTextPassword.requestFocus();
            return false;
        }

        if (!password.equals(password2)) {
            editTextPassword2.setError("Password tidak cocok");
            editTextPassword2.requestFocus();
            return false;
        }

        // Cek apakah username sudah digunakan
        if (databaseHelper.checkUsername(username)) {
            editTextUsername.setError("Username sudah digunakan");
            editTextUsername.requestFocus();
            return false;
        }

        // Cek apakah NIP sudah digunakan
        if (databaseHelper.checkNip(nip)) {
            editTextNoNip.setError("No. NIP sudah digunakan");
            editTextNoNip.requestFocus();
            return false;
        }

        return true;
    }
}