package com.example.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataUserpActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ApiService apiService;
    private int userProspekId;

    // Spinners
    private Spinner spinnerProyek, spinnerHunian, spinnerTipeUnit, spinnerNPWP, spinnerBPJS;

    // EditTexts
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp,
            editTextAlamat, editTextUangTandaJadi;

    // Selected values
    private String selectedProyek = "", selectedHunian = "", selectedTipeUnit = "",
            selectedNPWP = "", selectedBPJS = "";

    // Data lists
    private List<Proyek> proyekObjectList = new ArrayList<>();
    private List<String> hunianNameList = new ArrayList<>();
    private List<String> tipeUnitNameList = new ArrayList<>();
    private List<Kavling> kavlingList = new ArrayList<>();

    // List untuk spinner
    private List<String> proyekNameList = new ArrayList<>();
    private ArrayAdapter<String> adapterTipeUnit;

    // Simpan data lama untuk perbandingan
    private String oldTipeHunian = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data_userp);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Get data from intent
        Intent intent = getIntent();
        userProspekId = intent.getIntExtra("USER_PROSPEK_ID", -1);
        String penginput = intent.getStringExtra("PENGINPUT");
        String nama = intent.getStringExtra("NAMA");
        String email = intent.getStringExtra("EMAIL");
        String noHp = intent.getStringExtra("NO_HP");
        String alamat = intent.getStringExtra("ALAMAT");
        String proyek = intent.getStringExtra("PROYEK");
        String hunian = intent.getStringExtra("HUNIAN");
        String tipeHunian = intent.getStringExtra("TIPE_HUNIAN");
        int dp = intent.getIntExtra("DP", 0);
        String statusNPWP = intent.getStringExtra("STATUS_NPWP");
        String statusBPJS = intent.getStringExtra("STATUS_BPJS");

        // Simpan tipe hunian lama
        oldTipeHunian = tipeHunian != null ? tipeHunian : "";

        initializeViews();
        setupBottomNavigation();

        // Set data to views
        setDataToViews(penginput, nama, email, noHp, alamat, proyek, hunian, tipeHunian, dp, statusNPWP, statusBPJS);

        // Load initial data
        loadProyekData();
        setupSpinnerListeners();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Initialize EditTexts
        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextUangTandaJadi = findViewById(R.id.editTextUangPengadaan);

        // Initialize Spinners
        spinnerProyek = findViewById(R.id.spinnerProyek);
        spinnerHunian = findViewById(R.id.hunian);
        spinnerTipeUnit = findViewById(R.id.spinnerTipeUnit);
        spinnerNPWP = findViewById(R.id.spinnerNPWP);
        spinnerBPJS = findViewById(R.id.spinnerBPJS);

        // Setup NPWP and BPJS spinners
        setupStatusSpinners();

        // Set toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupStatusSpinners() {
        // NPWP Spinner
        ArrayAdapter<CharSequence> npwpAdapter = ArrayAdapter.createFromResource(this,
                R.array.opsi_spinnerNPWP, android.R.layout.simple_spinner_item);
        npwpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNPWP.setAdapter(npwpAdapter);

        // BPJS Spinner
        ArrayAdapter<CharSequence> bpjsAdapter = ArrayAdapter.createFromResource(this,
                R.array.opsi_spinnerBPJS, android.R.layout.simple_spinner_item);
        bpjsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBPJS.setAdapter(bpjsAdapter);

        // Set listeners for status spinners
        spinnerNPWP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNPWP = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedNPWP = "";
            }
        });

        spinnerBPJS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBPJS = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBPJS = "";
            }
        });
    }

    private void setDataToViews(String penginput, String nama, String email, String noHp,
                                String alamat, String proyek, String hunian, String tipeHunian,
                                int dp, String statusNPWP, String statusBPJS) {
        editTextPenginput.setText(penginput != null ? penginput : "");
        editTextNama.setText(nama != null ? nama : "");
        editTextEmail.setText(email != null ? email : "");
        editTextNoHp.setText(noHp != null ? noHp : "");
        editTextAlamat.setText(alamat != null ? alamat : "");
        editTextUangTandaJadi.setText(String.valueOf(dp));

        // Set status spinners dengan null safety
        if (statusNPWP != null) {
            setSpinnerSelection(spinnerNPWP, statusNPWP);
        }
        if (statusBPJS != null) {
            setSpinnerSelection(spinnerBPJS, statusBPJS);
        }

        // Store initial values for project, hunian, and tipe unit
        selectedProyek = proyek != null ? proyek : "";
        selectedHunian = hunian != null ? hunian : "";
        selectedTipeUnit = tipeHunian != null ? tipeHunian : "";

        Log.d("EditDataUserp", "Data dari intent - Proyek: " + selectedProyek +
                ", Hunian: " + selectedHunian + ", Tipe: " + selectedTipeUnit +
                ", Old Tipe: " + oldTipeHunian);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;

        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void loadProyekData() {
        Call<ProyekResponse> call = apiService.getProyek();
        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProyekResponse proyekResponse = response.body();

                    if (proyekResponse.isSuccess()) {
                        List<Proyek> data = proyekResponse.getData();
                        proyekObjectList.clear();
                        proyekNameList.clear();

                        for (Proyek proyek : data) {
                            proyekObjectList.add(proyek);
                            proyekNameList.add(proyek.getNamaProyek());
                        }

                        setupProyekSpinner();

                        // Set selected proyek after spinner is populated
                        if (selectedProyek != null && !selectedProyek.isEmpty()) {
                            setSpinnerSelection(spinnerProyek, selectedProyek);
                            // Cari ID proyek berdasarkan nama untuk load hunian
                            int proyekId = getProyekIdByName(selectedProyek);
                            if (proyekId != -1) {
                                loadHunianData(proyekId);
                            }
                        }

                    } else {
                        Toast.makeText(EditDataUserpActivity.this,
                                "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataUserpActivity.this,
                            "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Toast.makeText(EditDataUserpActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProyekSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, proyekNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(adapter);

        spinnerProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProyek = parent.getItemAtPosition(position).toString();
                // Cari ID proyek berdasarkan nama untuk load hunian
                int proyekId = getProyekIdByName(selectedProyek);
                if (proyekId != -1) {
                    loadHunianData(proyekId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProyek = "";
            }
        });
    }

    private void loadHunianData(int proyekId) {
        Call<HunianResponse> call = apiService.getHunianByProyek(proyekId);
        call.enqueue(new Callback<HunianResponse>() {
            @Override
            public void onResponse(Call<HunianResponse> call, Response<HunianResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HunianResponse hunianResponse = response.body();

                    if (hunianResponse.isSuccess()) {
                        List<String> data = hunianResponse.getData();
                        hunianNameList.clear();

                        if (data != null) {
                            hunianNameList.addAll(data);
                        }

                        setupHunianSpinner();

                        // Set selected hunian after spinner is populated
                        if (selectedHunian != null && !selectedHunian.isEmpty()) {
                            setSpinnerSelection(spinnerHunian, selectedHunian);
                            // Load kavling data untuk filter tipe unit
                            loadKavlingData(selectedProyek);
                        }

                    } else {
                        Toast.makeText(EditDataUserpActivity.this,
                                "Gagal memuat data hunian: " + hunianResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataUserpActivity.this,
                            "Gagal memuat data hunian", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HunianResponse> call, Throwable t) {
                Toast.makeText(EditDataUserpActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHunianSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, hunianNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHunian.setAdapter(adapter);

        spinnerHunian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHunian = parent.getItemAtPosition(position).toString();
                Log.d("EditDataUserp", "Hunian selected: " + selectedHunian);

                // Reset Tipe Unit saat Hunian berubah
                tipeUnitNameList.clear();
                selectedTipeUnit = "";
                if (adapterTipeUnit != null) {
                    adapterTipeUnit.notifyDataSetChanged();
                }

                // Filter tipe unit berdasarkan hunian yang dipilih
                filterTipeUnitData(selectedProyek, selectedHunian);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHunian = "";
            }
        });
    }

    // Method untuk load data kavling
    private void loadKavlingData(String proyek) {
        Log.d("EditDataUserp", "Loading kavling data - Proyek: " + proyek);

        Call<KavlingResponse> call = apiService.getKavlingByProyek("getKavling", proyek);
        call.enqueue(new Callback<KavlingResponse>() {
            @Override
            public void onResponse(Call<KavlingResponse> call, Response<KavlingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KavlingResponse kavlingResponse = response.body();
                    Log.d("EditDataUserp", "Kavling Response Success: " + kavlingResponse.isSuccess());

                    if (kavlingResponse.isSuccess()) {
                        List<Kavling> data = kavlingResponse.getData();
                        kavlingList.clear();

                        if (data != null) {
                            kavlingList.addAll(data);
                            Log.d("EditDataUserp", "Total kavling data loaded: " + kavlingList.size());

                            // Setelah kavling data dimuat, filter tipe unit
                            filterTipeUnitData(selectedProyek, selectedHunian);
                        }
                    } else {
                        Log.e("EditDataUserp", "API Error: " + kavlingResponse.getMessage());
                    }
                } else {
                    Log.e("EditDataUserp", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<KavlingResponse> call, Throwable t) {
                Log.e("EditDataUserp", "API Error: ", t);
            }
        });
    }

    // Method untuk filter tipe unit
    private void filterTipeUnitData(String proyek, String hunian) {
        Log.d("EditDataUserp", "Filtering tipe unit - Proyek: " + proyek + ", Hunian: " + hunian);

        tipeUnitNameList.clear();

        if (kavlingList.isEmpty()) {
            Log.d("EditDataUserp", "Kavling list masih kosong, loading data...");
            loadKavlingData(proyek);
            return;
        }

        // Filter kavling berdasarkan proyek dan hunian
        for (Kavling kavling : kavlingList) {
            String tipeHunian = kavling.getTipeHunian();
            String kavlingHunian = kavling.getHunian();
            String kavlingProyek = kavling.getProyek();

            boolean hunianMatches = kavlingHunian != null && kavlingHunian.equalsIgnoreCase(hunian);
            boolean proyekMatches = kavlingProyek != null && kavlingProyek.equalsIgnoreCase(proyek);

            if (hunianMatches && proyekMatches && tipeHunian != null && !tipeHunian.isEmpty()) {
                // PERBAIKAN: Gunakan format status yang konsisten
                String status = kavling.getStatusPenjualan();
                boolean isOnProgress = "On Progress".equalsIgnoreCase(status) || "On progress".equalsIgnoreCase(status);
                boolean isSoldOut = "Sold Out".equalsIgnoreCase(status) || "Sold out".equalsIgnoreCase(status);
                boolean isOldData = tipeHunian.equals(oldTipeHunian);

                // Tampilkan semua yang On Progress ATAU yang merupakan data lama
                if (isOnProgress || isOldData) {
                    // Tambahkan label untuk yang bukan On Progress
                    String displayName = isOnProgress ? tipeHunian : tipeHunian + " (SOLD OUT)";

                    if (!tipeUnitNameList.contains(displayName)) {
                        tipeUnitNameList.add(displayName);
                        Log.d("EditDataUserp", "✅ Added tipe: " + displayName + " - Status: " + status);
                    }
                }
            }
        }

        Log.d("EditDataUserp", "Final tipe unit list size: " + tipeUnitNameList.size());

        setupTipeUnitSpinner();

        // Set selected tipe unit after spinner is populated
        if (selectedTipeUnit != null && !selectedTipeUnit.isEmpty()) {
            // Cari display name yang sesuai dengan selectedTipeUnit
            String displayNameToSelect = selectedTipeUnit;
            for (String displayName : tipeUnitNameList) {
                if (displayName.replace(" (SOLD OUT)", "").equals(selectedTipeUnit)) {
                    displayNameToSelect = displayName;
                    break;
                }
            }
            setSpinnerSelection(spinnerTipeUnit, displayNameToSelect);
            Log.d("EditDataUserp", "Set selected tipe: " + displayNameToSelect);
        } else if (!tipeUnitNameList.isEmpty()) {
            // Auto select first item jika tidak ada yang dipilih
            spinnerTipeUnit.setSelection(0);
            selectedTipeUnit = tipeUnitNameList.get(0).replace(" (SOLD OUT)", "");
            Log.d("EditDataUserp", "Auto selected first tipe: " + selectedTipeUnit);
        }

        // Tampilkan toast jika tidak ada data
        if (tipeUnitNameList.isEmpty()) {
            Toast.makeText(EditDataUserpActivity.this,
                    "Tidak ada tipe unit untuk hunian: " + hunian,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTipeUnitSpinner() {
        if (tipeUnitNameList.isEmpty()) {
            tipeUnitNameList.add("Tidak ada tipe unit tersedia");
        }

        adapterTipeUnit = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tipeUnitNameList);
        adapterTipeUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipeUnit.setAdapter(adapterTipeUnit);

        spinnerTipeUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDisplayName = parent.getItemAtPosition(position).toString();

                // Bersihkan dari label "SOLD OUT" untuk nilai sebenarnya
                selectedTipeUnit = selectedDisplayName.replace(" (SOLD OUT)", "").trim();

                Log.d("EditDataUserp", "Tipe unit selected: " + selectedTipeUnit +
                        " (Display: " + selectedDisplayName + ")");

                if (selectedTipeUnit.equals("Tidak ada tipe unit tersedia")) {
                    selectedTipeUnit = "";
                }

                // Nonaktifkan jika yang dipilih adalah SOLD OUT (kecuali data lama)
                boolean isSoldOut = selectedDisplayName.contains("(SOLD OUT)");
                boolean isOldData = selectedTipeUnit.equals(oldTipeHunian);

                if (isSoldOut && !isOldData) {
                    Toast.makeText(EditDataUserpActivity.this,
                            "Tipe unit ini sudah terjual", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTipeUnit = "";
            }
        });
    }

    // Method untuk mendapatkan ID proyek berdasarkan nama
    private int getProyekIdByName(String proyekName) {
        for (Proyek proyek : proyekObjectList) {
            if (proyek.getNamaProyek().equals(proyekName)) {
                return proyek.getIdProyek();
            }
        }
        return -1;
    }

    private void setupSpinnerListeners() {
        // Already set in individual setup methods
    }

    private void setupButtonListeners() {
        Button btnSimpan = findViewById(R.id.btnSimpan);
        Button btnBatal = findViewById(R.id.btnBatal);

        btnSimpan.setOnClickListener(v -> updateUserProspek());
        btnBatal.setOnClickListener(v -> finish());
    }

    private void updateUserProspek() {
        // Get values from form
        String penginput = editTextPenginput.getText().toString().trim();
        String nama = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String dpStr = editTextUangTandaJadi.getText().toString().trim();

        // Validation
        if (penginput.isEmpty() || nama.isEmpty() || noHp.isEmpty() ||
                alamat.isEmpty() || dpStr.isEmpty() || selectedProyek.isEmpty() ||
                selectedHunian.isEmpty() || selectedTipeUnit.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi tambahan untuk tipe unit yang SOLD OUT
        String selectedDisplayName = getSelectedTipeUnitDisplayName();
        boolean isSoldOut = selectedDisplayName.contains("(SOLD OUT)");
        boolean isOldData = selectedTipeUnit.equals(oldTipeHunian);

        if (isSoldOut && !isOldData) {
            Toast.makeText(this, "Tipe unit yang dipilih sudah terjual", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi userProspekId
        if (userProspekId == -1) {
            Toast.makeText(this, "Error: ID UserProspek tidak valid", Toast.LENGTH_SHORT).show();
            Log.e("UpdateUserProspek", "Invalid userProspekId: " + userProspekId);
            return;
        }

        try {
            int dp = Integer.parseInt(dpStr);

            // DEBUG: Log data yang akan dikirim
            Log.d("UpdateUserProspek", "=== DATA YANG DIKIRIM KE SERVER ===");
            Log.d("UpdateUserProspek", "Endpoint: update_userprospek_with_histori.php");
            Log.d("UpdateUserProspek", "id_userprospek: " + userProspekId);
            Log.d("UpdateUserProspek", "nama_penginput: " + penginput);
            Log.d("UpdateUserProspek", "nama_user: " + nama);
            Log.d("UpdateUserProspek", "email: " + email);
            Log.d("UpdateUserProspek", "no_hp: " + noHp);
            Log.d("UpdateUserProspek", "alamat: " + alamat);
            Log.d("UpdateUserProspek", "proyek: " + selectedProyek);
            Log.d("UpdateUserProspek", "hunian: " + selectedHunian);
            Log.d("UpdateUserProspek", "tipe_hunian: " + selectedTipeUnit);
            Log.d("UpdateUserProspek", "dp: " + dp);
            Log.d("UpdateUserProspek", "status_bpjs: " + selectedBPJS);
            Log.d("UpdateUserProspek", "status_npwp: " + selectedNPWP);
            Log.d("UpdateUserProspek", "old_tipe_hunian: " + oldTipeHunian);
            Log.d("UpdateUserProspek", "tipe_hunian_berubah: " + !selectedTipeUnit.equals(oldTipeHunian));

            // PERBAIKAN: Pastikan menggunakan endpoint yang benar
            Call<ResponseBody> call = apiService.updateUserProspekWithHistori(
                    "update_userprospek",  // action
                    userProspekId,         // id_userprospek
                    penginput,             // nama_penginput
                    nama,                  // nama_user
                    email,                 // email
                    noHp,                  // no_hp
                    alamat,                // alamat
                    selectedProyek,        // proyek
                    selectedHunian,        // hunian
                    selectedTipeUnit,      // tipe_hunian
                    dp,                    // dp
                    selectedBPJS,          // status_bpjs
                    selectedNPWP           // status_npwp
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseString = response.body().string();
                            Log.d("UpdateUserProspek", "RAW RESPONSE: " + responseString);

                            JSONObject jsonResponse = new JSONObject(responseString);
                            boolean success = jsonResponse.getBoolean("success");
                            String message = jsonResponse.getString("message");

                            // TAMPILKAN DEBUG INFO DARI SERVER
                            StringBuilder toastMessage = new StringBuilder();
                            toastMessage.append(message);

                            if (jsonResponse.has("debug")) {
                                JSONObject debug = jsonResponse.getJSONObject("debug");

                                // Debug received data
                                if (debug.has("received_input")) {
                                    Log.d("UpdateUserProspek", "Data received by server: " + debug.get("received_input"));
                                }

                                // Debug tipe hunian comparison
                                if (debug.has("tipe_hunian_comparison")) {
                                    JSONObject tipeComparison = debug.getJSONObject("tipe_hunian_comparison");
                                    boolean tipeBerubah = tipeComparison.getBoolean("berubah");
                                    String tipeLama = tipeComparison.getString("lama");
                                    String tipeBaru = tipeComparison.getString("baru");

                                    Log.d("UpdateUserProspek", "Tipe hunian berubah: " + tipeBerubah +
                                            " (" + tipeLama + " -> " + tipeBaru + ")");

                                    if (tipeBerubah) {
                                        toastMessage.append("\nTipe hunian berubah: ").append(tipeLama).append(" → ").append(tipeBaru);
                                    }
                                }

                                // Debug kavling check
                                if (debug.has("kavling_check")) {
                                    JSONObject kavlingCheck = debug.getJSONObject("kavling_check");
                                    boolean lamaExists = kavlingCheck.getBoolean("lama_exists");
                                    boolean baruExists = kavlingCheck.getBoolean("baru_exists");
                                    String lamaStatus = kavlingCheck.getString("lama_status");
                                    String baruStatus = kavlingCheck.getString("baru_status");

                                    Log.d("UpdateUserProspek", "Kavling lama exists: " + lamaExists + ", status: " + lamaStatus);
                                    Log.d("UpdateUserProspek", "Kavling baru exists: " + baruExists + ", status: " + baruStatus);

                                    if (!lamaExists) {
                                        toastMessage.append("\n⚠️ Kavling lama tidak ditemukan");
                                    }
                                    if (!baruExists) {
                                        toastMessage.append("\n⚠️ Kavling baru tidak ditemukan");
                                    }
                                }

                                // Debug kavling update results
                                if (debug.has("kavling_update_results")) {
                                    JSONObject kavlingResults = debug.getJSONObject("kavling_update_results");
                                    boolean lamaSuccess = kavlingResults.getBoolean("lama_success");
                                    int lamaAffected = kavlingResults.getInt("lama_affected");
                                    boolean baruSuccess = kavlingResults.getBoolean("baru_success");
                                    int baruAffected = kavlingResults.getInt("baru_affected");
                                    String kavlingMessage = kavlingResults.getString("message");

                                    Log.d("UpdateUserProspek", "Kavling update - Lama: " + lamaSuccess +
                                            "(" + lamaAffected + "), Baru: " + baruSuccess + "(" + baruAffected + ")");
                                    Log.d("UpdateUserProspek", "Kavling message: " + kavlingMessage);

                                    // Tampilkan pesan kavling yang detail
                                    if (!kavlingMessage.isEmpty()) {
                                        toastMessage.append("\n").append(kavlingMessage);
                                    }

                                    // Tampilkan pesan error spesifik
                                    if (lamaAffected == 0 && lamaSuccess) {
                                        toastMessage.append("\n⚠️ Tidak ada perubahan kavling lama");
                                    }
                                    if (baruAffected == 0 && baruSuccess) {
                                        toastMessage.append("\n⚠️ Tidak ada perubahan kavling baru");
                                    }
                                }

                                // Debug histori
                                if (debug.has("histori_success")) {
                                    boolean historiSuccess = debug.getBoolean("histori_success");
                                    if (historiSuccess) {
                                        toastMessage.append("\n✅ Histori tersimpan");
                                    } else {
                                        String historiError = debug.getString("histori_error");
                                        toastMessage.append("\n❌ Gagal simpan histori: ").append(historiError);
                                    }
                                }
                            }

                            // Tampilkan toast dengan informasi lengkap
                            if (success) {
                                Toast.makeText(EditDataUserpActivity.this, toastMessage.toString(), Toast.LENGTH_LONG).show();

                                // PERBAIKAN: Delay sebentar sebelum kembali agar user bisa baca pesan
                                new Handler().postDelayed(() -> {
                                    setResult(RESULT_OK);
                                    finish();
                                }, 2000);

                            } else {
                                Toast.makeText(EditDataUserpActivity.this, "Gagal: " + toastMessage.toString(), Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Log.e("UpdateUserProspek", "Response tidak successful: " + response.code());
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e("UpdateUserProspek", "Error body: " + errorBody);
                            Toast.makeText(EditDataUserpActivity.this,
                                    "Error response: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("UpdateUserProspek", "Error parsing response: " + e.getMessage());
                        Toast.makeText(EditDataUserpActivity.this,
                                "Error parsing: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("UpdateUserProspek", "Network Error: " + t.getMessage());
                    Toast.makeText(EditDataUserpActivity.this,
                            "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format DP tidak valid", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("UpdateUserProspek", "Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Method untuk mendapatkan display name tipe unit yang dipilih
    private String getSelectedTipeUnitDisplayName() {
        if (spinnerTipeUnit.getSelectedItem() != null) {
            return spinnerTipeUnit.getSelectedItem().toString();
        }
        return selectedTipeUnit;
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_folder);

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
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}