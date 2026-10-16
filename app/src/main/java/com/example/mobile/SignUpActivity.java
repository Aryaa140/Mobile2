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
import android.text.Editable;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.TextWatcher;
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
        setupNipTextWatcher();
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
    private void setupNipTextWatcher() {
        editTextNoNip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String nip = s.toString().trim().toUpperCase();

                if (nip.startsWith("MI")) {
                    // Marketing Inhouse
                    applyAutoDivision("Marketing Inhouse");
                } else if (nip.startsWith("MF")) {
                    // Marketing Freelance
                    applyAutoDivision("Marketing Freelance");
                } else if (nip.startsWith("AO")) {
                    // Admin Operator
                    applyAutoDivision("Admin Operator");
                } else {
                    // Prefix tidak dikenali → buka lock, biarkan user pilih manual
                    spinnerDivision.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
    // Pilih divisi otomatis & kunci spinner supaya tidak bisa diubah user
    private void applyAutoDivision(String divisionName) {
        if (spinnerDivision.getAdapter() == null || spinnerDivision.getAdapter().getCount() == 0) {
            return;
        }

        // Cari index divisi yang cocok
        int foundIndex = -1;
        String targetLower = divisionName.toLowerCase();
        for (int i = 0; i < spinnerDivision.getAdapter().getCount(); i++) {
            Object item = spinnerDivision.getAdapter().getItem(i);
            if (item == null) continue;
            String itemLower = item.toString().toLowerCase();

            // Cocokkan ketat dulu (exact contains), lalu fallback kata kunci
            if (itemLower.contains(targetLower)
                    || (targetLower.contains("freelance") && itemLower.contains("freelance"))
                    || (targetLower.contains("inhouse")   && itemLower.contains("inhouse"))
                    || (targetLower.contains("admin")     && itemLower.contains("admin"))) {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex >= 0) {
            if (spinnerDivision.getSelectedItemPosition() != foundIndex) {
                spinnerDivision.setSelection(foundIndex);
            }
            // === KUNCI SPINNER ===
            spinnerDivision.setEnabled(false);

            // Set selectedDivision langsung (karena listener mungkin tidak fire saat disabled)
            selectedDivision = spinnerDivision.getItemAtPosition(foundIndex).toString();
            updateNipHint();
        }
    }
    private void selectDivisionByName(String name) {
        if (spinnerDivision.getAdapter() == null || spinnerDivision.getAdapter().getCount() == 0) {
            return;
        }
        String targetLower = name.toLowerCase();
        for (int i = 0; i < spinnerDivision.getAdapter().getCount(); i++) {
            Object item = spinnerDivision.getAdapter().getItem(i);
            if (item == null) continue;
            String itemStr = item.toString().toLowerCase();

            // Cocok kalau mengandung nama target,
            // atau sama-sama mengandung "admin" (untuk toleransi penamaan server)
            if (itemStr.contains(targetLower)
                    || (targetLower.contains("admin") && itemStr.contains("admin"))) {
                if (spinnerDivision.getSelectedItemPosition() != i) {
                    spinnerDivision.setSelection(i);
                }
                return;
            }
        }
    }

    // Method untuk setup spinner dengan data dari server
    private void setupDivisionSpinner() {
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Pilih Divisi");
        spinnerItems.addAll(divisiList);

        // === BARU: pastikan Admin Operator selalu ada ===
        boolean hasAdminOperator = false;
        for (String d : spinnerItems) {
            String dl = d.toLowerCase();
            if (dl.contains("admin") && dl.contains("operator")) {
                hasAdminOperator = true;
                break;
            }
        }
        if (!hasAdminOperator) {
            spinnerItems.add("Admin Operator");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivision.setAdapter(adapter);

        spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedDivision = parent.getItemAtPosition(position).toString();
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

        // === BARU: kalau user sudah mengetik NIP (diawali AO) sebelum server merespon
        String currentNip = editTextNoNip.getText().toString().trim().toUpperCase();
        if (currentNip.startsWith("MI")) {
            applyAutoDivision("Marketing Inhouse");
        } else if (currentNip.startsWith("MF")) {
            applyAutoDivision("Marketing Freelance");
        } else if (currentNip.startsWith("AO")) {
            applyAutoDivision("Admin Operator");
        }
    }
    // Method untuk setup spinner default jika gagal mengambil dari server
    private void setupDefaultDivisionSpinner() {
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Pilih Divisi");
        spinnerItems.add("Marketing Inhouse");
        spinnerItems.add("Marketing Freelance");
        spinnerItems.add("Admin Operator");   // === BARU ===

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivision.setAdapter(adapter);

        spinnerDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedDivision = parent.getItemAtPosition(position).toString();
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

        // === BARU ===
        String currentNip = editTextNoNip.getText().toString().trim().toUpperCase();
        if (currentNip.startsWith("AO")) {
            selectDivisionByName("Admin Operator");
        }
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
        String nipCode = nip.length() >= 2 ? nip.substring(0, 2).toUpperCase() : "";
        String divLower = division.toLowerCase();

        if (divLower.contains("inhouse")) {
            return nipCode.equals("MI");
        } else if (divLower.contains("freelance")) {
            return nipCode.equals("MF");
        } else if (divLower.contains("admin") || divLower.contains("operator")) {
            return nipCode.equals("AO");
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
        }else {
            buttonBuatAkun.setText("Buat Akun");
            buttonBuatAkun.setEnabled(true);
            buttonKembali.setEnabled(true);
            editTextUsername.setEnabled(true);
            editTextEmail.setEnabled(true);
            editTextNoNip.setEnabled(true);
            editTextPassword.setEnabled(true);
            editTextPassword2.setEnabled(true);

            // === BARU: spinner hanya enable kalau NIP tidak punya prefix MI/MF/AO ===
            String nipNow = editTextNoNip.getText().toString().trim().toUpperCase();
            boolean autoLock = nipNow.startsWith("MI")
                    || nipNow.startsWith("MF")
                    || nipNow.startsWith("AO");
            spinnerDivision.setEnabled(!autoLock);
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