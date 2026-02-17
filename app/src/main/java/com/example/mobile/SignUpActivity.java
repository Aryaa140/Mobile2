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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextEmail, editTextNoNip, editTextPassword, editTextPassword2;
    private Spinner spinnerDivision;
    private Button buttonBuatAkun, buttonKembali;
    private DatabaseHelper databaseHelper;
    private String selectedDivision = "";

    // Tambahkan variabel untuk menyimpan daftar divisi
    private List<String> divisiList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi database helper
        databaseHelper = new DatabaseHelper(this);

        // Inisialisasi view
        editTextUsername = findViewById(R.id.username);
        editTextEmail = findViewById(R.id.email);
        spinnerDivision = findViewById(R.id.spinnerOpsi);
        editTextNoNip = findViewById(R.id.noNip);
        editTextPassword = findViewById(R.id.password);
        editTextPassword2 = findViewById(R.id.password2);
        buttonBuatAkun = findViewById(R.id.btnBuatAkun);
        buttonKembali = findViewById(R.id.btnKembali);

        // Setup spinner dengan divisi dari server
        loadDivisiFromServer();

        // Pastikan EditText NIP bisa diketik
        setupEditTextNip();

        // Menangani klik tombol buat akun
        buttonBuatAkun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validasi input
                if (validateInput()) {
                    // Daftarkan pengguna baru
                    String username = editTextUsername.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();
                    String nip = editTextNoNip.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    // Cek NIP terlebih dahulu sebelum registrasi
                    checkNIPBeforeRegister(username, email, nip, selectedDivision, password);
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
                finish();
            }
        });
    }

    // Method untuk mengambil data divisi dari server
    private void loadDivisiFromServer() {
        showLoadingDivisi(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<DivisiResponse> call = apiService.getDivisiList();

        call.enqueue(new Callback<DivisiResponse>() {
            @Override
            public void onResponse(Call<DivisiResponse> call, Response<DivisiResponse> response) {
                showLoadingDivisi(false);

                if (response.isSuccessful() && response.body() != null) {
                    DivisiResponse divisiResponse = response.body();

                    if (divisiResponse.isSuccess()) {
                        List<String> serverDivisiList = divisiResponse.getDivisiList();
                        if (serverDivisiList != null && !serverDivisiList.isEmpty()) {
                            divisiList.clear();
                            divisiList.addAll(serverDivisiList);
                            setupDivisionSpinner();
                        } else {
                            // Jika data kosong, gunakan data default
                            setupDefaultDivisionSpinner();
                            Toast.makeText(SignUpActivity.this,
                                    "Data divisi kosong, menggunakan data default",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        setupDefaultDivisionSpinner();
                        Toast.makeText(SignUpActivity.this,
                                "Gagal mengambil data divisi: " + divisiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    setupDefaultDivisionSpinner();
                    Toast.makeText(SignUpActivity.this,
                            "Error mengambil data divisi",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DivisiResponse> call, Throwable t) {
                showLoadingDivisi(false);
                setupDefaultDivisionSpinner();
                Toast.makeText(SignUpActivity.this,
                        "Koneksi gagal: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method untuk setup spinner dengan data dari server
    private void setupDivisionSpinner() {
        // Buat List dengan data dari server
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Pilih Divisi");
        spinnerItems.addAll(divisiList);

        // Create an ArrayAdapter menggunakan List<String>
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
                    // Update hint berdasarkan divisi yang dipilih
                    updateNipHint();
                } else {
                    selectedDivision = "";
                    editTextNoNip.setHint("Masukkan No. NIP");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDivision = "";
                editTextNoNip.setHint("Masukkan No. NIP");
            }
        });
    }

    // Method untuk setup spinner default jika gagal mengambil dari server
    private void setupDefaultDivisionSpinner() {
        // Buat List dengan opsi default Marketing
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Pilih Divisi");
        spinnerItems.add("Marketing Inhouse");
        spinnerItems.add("Marketing Freelance");

        // Create an ArrayAdapter menggunakan List<String>
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
                    // Update hint berdasarkan divisi yang dipilih
                    updateNipHint();
                } else {
                    selectedDivision = "";
                    editTextNoNip.setHint("Masukkan No. NIP");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDivision = "";
                editTextNoNip.setHint("Masukkan No. NIP");
            }
        });
    }

    // Method untuk setup EditText NIP agar bisa diketik
    private void setupEditTextNip() {
        // Pastikan EditText NIP enabled dan focusable
        editTextNoNip.setEnabled(true);
        editTextNoNip.setFocusable(true);
        editTextNoNip.setFocusableInTouchMode(true);
        editTextNoNip.setClickable(true);

        // Set hint default
        editTextNoNip.setHint("Masukkan No. NIP");

        // Hapus semua text filter atau input type yang mungkin memblokir input
        editTextNoNip.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        // Clear any existing text
        editTextNoNip.setText("");
    }

    // Method untuk update hint NIP berdasarkan divisi
    private void updateNipHint() {
        if (selectedDivision != null) {
            if (selectedDivision.toLowerCase().contains("inhouse")) {
                editTextNoNip.setHint("Contoh: MI4123 (harus diawali MI)");
            } else if (selectedDivision.toLowerCase().contains("freelance")) {
                editTextNoNip.setHint("Contoh: MF4123 (harus diawali MF)");
            } else {
                editTextNoNip.setHint("Masukkan No. NIP");
            }
        }
    }

    // Method untuk validasi input
    private boolean validateInput() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String nip = editTextNoNip.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String password2 = editTextPassword2.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username tidak boleh kosong");
            editTextUsername.requestFocus();
            return false;
        }

        // Validasi email
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email tidak boleh kosong");
            editTextEmail.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format email tidak valid");
            editTextEmail.requestFocus();
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

        return true;
    }

    // METHOD UNTUK CEK NIP SEBELUM REGISTRASI - TAMBAH PARAMETER EMAIL
    private void checkNIPBeforeRegister(String username, String email, String nip, String division, String password) {
        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<CheckNIPResponse> call = apiService.checkNIP(nip);

        call.enqueue(new Callback<CheckNIPResponse>() {
            @Override
            public void onResponse(Call<CheckNIPResponse> call, Response<CheckNIPResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckNIPResponse checkResponse = response.body();

                    if (checkResponse.isExists()) {
                        // NIP valid, cek kecocokan divisi dengan kode NIP
                        if (validateDivisionWithNIP(division, nip)) {
                            // Kecocokan valid, lanjutkan registrasi
                            registerUserToMySQL(username, email, nip, division, password);
                        } else {
                            showLoading(false);
                            Toast.makeText(SignUpActivity.this,
                                    "Divisi tidak sesuai dengan kode NIP", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(SignUpActivity.this, "NIP tidak terdaftar dalam sistem", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(SignUpActivity.this, "Error saat memverifikasi NIP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckNIPResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SignUpActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // METHOD BARU: Validasi kecocokan divisi dengan kode NIP
    private boolean validateDivisionWithNIP(String division, String nip) {
        // Ambil 2 karakter pertama dari NIP (kode divisi)
        String nipCode = nip.length() >= 2 ? nip.substring(0, 2).toUpperCase() : "";

        // Validasi berdasarkan divisi yang dipilih
        if (division.toLowerCase().contains("inhouse")) {
            // Untuk Marketing Inhouse, kode NIP harus "MI"
            return nipCode.equals("MI");
        } else if (division.toLowerCase().contains("freelance")) {
            // Untuk Marketing Freelance, kode NIP harus "MF"
            return nipCode.equals("MF");
        }

        return false;
    }

    // METHOD UNTUK REGISTRASI KE MYSQL MELALUI API PHP - TAMBAH PARAMETER EMAIL
    private void registerUserToMySQL(String username, String email, String nip, String division, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Gunakan method yang sudah ada di ApiService (5 parameter dengan email)
        Call<RegisterResponse> call = apiService.registerUser(username, nip, division, password, email);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    if (registerResponse.isSuccess()) {
                        // Pendaftaran berhasil
                        Toast.makeText(SignUpActivity.this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show();

                        // Kembali ke activity login
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Pendaftaran gagal
                        Toast.makeText(SignUpActivity.this, "Gagal membuat akun: " + registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SignUpActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method untuk menampilkan/menyembunyikan loading registrasi
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            // Tampilkan loading dan disable sementara
            buttonBuatAkun.setText("Memproses...");
            buttonBuatAkun.setEnabled(false);
            buttonKembali.setEnabled(false);
            spinnerDivision.setEnabled(false);
            editTextUsername.setEnabled(false);
            editTextEmail.setEnabled(false);
            editTextNoNip.setEnabled(false);
            editTextPassword.setEnabled(false);
            editTextPassword2.setEnabled(false);
        } else {
            // Enable kembali semua input
            buttonBuatAkun.setText("Buat Akun");
            buttonBuatAkun.setEnabled(true);
            buttonKembali.setEnabled(true);
            spinnerDivision.setEnabled(true);
            editTextUsername.setEnabled(true);
            editTextEmail.setEnabled(true);
            editTextNoNip.setEnabled(true);
            editTextPassword.setEnabled(true);
            editTextPassword2.setEnabled(true);
        }
    }

    // Method baru untuk loading saat mengambil data divisi
    private void showLoadingDivisi(boolean isLoading) {
        if (isLoading) {
            // Sementara disable spinner dan tombol
            spinnerDivision.setEnabled(false);
            buttonBuatAkun.setEnabled(false);
        } else {
            spinnerDivision.setEnabled(true);
            buttonBuatAkun.setEnabled(true);
        }
    }
}