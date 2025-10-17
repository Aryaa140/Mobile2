package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahUserpActivity extends AppCompatActivity {

    private static final String TAG = "TambahUserpActivity";

    private Spinner spinnerProspek, spinnerProyek, spinnerHunian, spinnerTipeUnit;
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp, editTextAlamat, editTextUangTandaJadi;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar TopAppBar;
    private BottomNavigationView bottomNavigationView;

    private List<Prospek2> prospekList = new ArrayList<>();
    private List<Proyek> proyekList = new ArrayList<>();
    private List<Kavling> kavlingList = new ArrayList<>();
    private List<String> hunianList = new ArrayList<>();
    private List<String> tipeUnitList = new ArrayList<>();

    private int selectedProspekId = -1;
    private String selectedProspekNPWP = "", selectedProspekBPJS = "";
    private String selectedProyek = "", selectedHunian = "", selectedTipeUnit = "";

    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambahuserp);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initViews();
        setupNavigation();
        setupSpinnerListeners();
        setupButtonListeners();

        // Load data
        String username = sharedPreferences.getString("username", "");
        if (!username.isEmpty()) {
            editTextPenginput.setText(username);
            loadProspekData(username);
            loadProyekData();
        } else {
            Toast.makeText(this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        TopAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        spinnerProspek = findViewById(R.id.spinnerRoleProspek);
        spinnerProyek = findViewById(R.id.spinnerRoleRefrensiProyek);
        spinnerHunian = findViewById(R.id.hunian);
        spinnerTipeUnit = findViewById(R.id.spinnerTipeUnit);
        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);
        editTextUangTandaJadi = findViewById(R.id.editTextUangPengadaan);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);

        editTextPenginput.setEnabled(false);
        setHunianSpinnerEnabled(false);
        setTipeUnitSpinnerEnabled(false);
    }

    // === METHOD PROYEK ===
    private void loadProyekData() {
        Log.d(TAG, "loadProyekData: Memulai");

        Call<ProyekResponse> call = apiService.getProyekData("getProyek");
        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                Log.d(TAG, "Response received - Code: " + response.code());

                if (response.isSuccessful()) {
                    ProyekResponse proyekResponse = response.body();
                    if (proyekResponse != null && proyekResponse.isSuccess()) {
                        List<Proyek> data = proyekResponse.getData();
                        if (data != null && !data.isEmpty()) {
                            proyekList.clear();
                            proyekList.addAll(data);

                            Log.d(TAG, "Data proyek loaded: " + proyekList.size() + " items");

                            runOnUiThread(() -> {
                                setupProyekSpinner();
                                Toast.makeText(TambahUserpActivity.this,
                                        "Proyek loaded: " + proyekList.size(), Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Log.e(TAG, "Data kosong");
                            useDummyProyekData();
                        }
                    } else {
                        Log.e(TAG, "Response tidak success");
                        useDummyProyekData();
                    }
                } else {
                    Log.e(TAG, "Response tidak successful: " + response.code());
                    useDummyProyekData();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                useDummyProyekData();
            }
        });
    }

    private void useDummyProyekData() {
        Log.d(TAG, "Using dummy data");
        proyekList.clear();
        proyekList.add(new Proyek(1, "The Quality Riverside"));
        proyekList.add(new Proyek(2, "The Quality Harmony"));

        runOnUiThread(() -> {
            setupProyekSpinner();
            Toast.makeText(this, "Menggunakan data dummy", Toast.LENGTH_LONG).show();
        });
    }

    private void setupProyekSpinner() {
        Log.d(TAG, "setupProyekSpinner: " + proyekList.size() + " items");

        List<String> proyekNames = new ArrayList<>();
        proyekNames.add("Pilih Proyek");

        for (Proyek proyek : proyekList) {
            if (proyek != null && proyek.getNamaProyek() != null) {
                proyekNames.add(proyek.getNamaProyek());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proyekNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(adapter);

        Log.d(TAG, "Spinner proyek setup completed");
    }

    // === METHOD KAVLING ===
    private void loadKavlingData(String namaProyek) {
        Log.d(TAG, "loadKavlingData: Memuat data kavling untuk proyek: " + namaProyek);

        if (namaProyek == null || namaProyek.trim().isEmpty()) {
            Log.e(TAG, "Nama proyek null atau kosong");
            return;
        }

        Call<KavlingResponse> call = apiService.getKavlingByProyek("getKavling", namaProyek);
        call.enqueue(new Callback<KavlingResponse>() {
            @Override
            public void onResponse(Call<KavlingResponse> call, Response<KavlingResponse> response) {
                Log.d(TAG, "onResponse Kavling - Code: " + response.code() + ", Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    KavlingResponse kavlingResponse = response.body();
                    if (kavlingResponse.isSuccess() && kavlingResponse.getData() != null) {
                        kavlingList.clear();
                        kavlingList.addAll(kavlingResponse.getData());
                        Log.d(TAG, "Data kavling berhasil dimuat: " + kavlingList.size() + " item");

                        // Filter hunian yang tersedia
                        filterHunianData();
                    } else {
                        Log.e(TAG, "Response tidak success atau data null");
                        Toast.makeText(TambahUserpActivity.this,
                                "Tidak ada data kavling tersedia untuk proyek ini", Toast.LENGTH_SHORT).show();
                        clearHunianAndTipeUnitSpinners();
                    }
                } else {
                    Log.e(TAG, "Response error: " + response.message());
                    Toast.makeText(TambahUserpActivity.this,
                            "Gagal memuat data kavling", Toast.LENGTH_SHORT).show();
                    clearHunianAndTipeUnitSpinners();
                }
            }

            @Override
            public void onFailure(Call<KavlingResponse> call, Throwable t) {
                Log.e(TAG, "onFailure Kavling: " + t.getMessage(), t);
                Toast.makeText(TambahUserpActivity.this,
                        "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                clearHunianAndTipeUnitSpinners();
            }
        });
    }

    private void filterHunianData() {
        hunianList.clear();
        tipeUnitList.clear();

        // PERBAIKAN: Hanya tampilkan hunian yang masih memiliki unit dengan status "On Progress"
        for (Kavling kavling : kavlingList) {
            if ("On Progress".equalsIgnoreCase(kavling.getStatusPenjualan())) {
                if (!hunianList.contains(kavling.getHunian())) {
                    hunianList.add(kavling.getHunian());
                    Log.d(TAG, "Hunian tersedia: " + kavling.getHunian() + " - Status: On Progress");
                }
            }
        }

        // PERBAIKAN: Jika tidak ada hunian dengan status "On Progress", tampilkan pesan
        if (hunianList.isEmpty()) {
            Log.d(TAG, "Tidak ada hunian dengan status On Progress");

            // Cek apakah ada hunian yang sudah terjual semua
            for (Kavling kavling : kavlingList) {
                if (!hunianList.contains(kavling.getHunian())) {
                    hunianList.add(kavling.getHunian() + " (SOLD OUT)");
                    Log.d(TAG, "Hunian SOLD OUT: " + kavling.getHunian());
                }
            }
        }

        runOnUiThread(() -> {
            setupHunianSpinner();
            clearTipeUnitSpinner();

            if (hunianList.isEmpty()) {
                Toast.makeText(TambahUserpActivity.this,
                        "Tidak ada hunian tersedia untuk proyek ini", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TambahUserpActivity.this,
                        "Pilih hunian terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTipeUnitByHunian(String hunian) {
        Log.d(TAG, "loadTipeUnitByHunian: Memuat tipe unit untuk hunian: " + hunian);

        if (hunian == null || hunian.trim().isEmpty()) {
            Log.e(TAG, "Hunian null atau kosong");
            return;
        }

        // PERBAIKAN: Hapus "(SOLD OUT)" dari nama hunian untuk pencarian
        String cleanHunian = hunian.replace(" (SOLD OUT)", "").trim();

        tipeUnitList.clear();

        // PERBAIKAN: Cek apakah hunian ini sold out
        boolean isHunianSoldOut = hunian.contains("(SOLD OUT)");

        if (isHunianSoldOut) {
            // Jika hunian sold out, tampilkan semua tipe unit dengan status sold out
            for (Kavling kavling : kavlingList) {
                if (cleanHunian.equals(kavling.getHunian())) {
                    if (!tipeUnitList.contains(kavling.getTipeHunian() + " (SOLD OUT)")) {
                        tipeUnitList.add(kavling.getTipeHunian() + " (SOLD OUT)");
                        Log.d(TAG, "Tipe unit SOLD OUT: " + kavling.getTipeHunian());
                    }
                }
            }
        } else {
            // Jika hunian masih available, hanya tampilkan tipe unit dengan status "On Progress"
            for (Kavling kavling : kavlingList) {
                if (cleanHunian.equals(kavling.getHunian()) &&
                        "On Progress".equalsIgnoreCase(kavling.getStatusPenjualan())) {
                    if (!tipeUnitList.contains(kavling.getTipeHunian())) {
                        tipeUnitList.add(kavling.getTipeHunian());
                        Log.d(TAG, "Tipe unit tersedia: " + kavling.getTipeHunian() + " - Status: On Progress");
                    }
                }
            }

            // PERBAIKAN: Jika tidak ada tipe unit dengan status "On Progress", tampilkan yang sold out
            if (tipeUnitList.isEmpty()) {
                for (Kavling kavling : kavlingList) {
                    if (cleanHunian.equals(kavling.getHunian())) {
                        if (!tipeUnitList.contains(kavling.getTipeHunian() + " (SOLD OUT)")) {
                            tipeUnitList.add(kavling.getTipeHunian() + " (SOLD OUT)");
                            Log.d(TAG, "Tipe unit SOLD OUT: " + kavling.getTipeHunian());
                        }
                    }
                }
            }
        }

        runOnUiThread(() -> {
            setupTipeUnitSpinner();

            if (tipeUnitList.isEmpty()) {
                Toast.makeText(TambahUserpActivity.this,
                        "Tidak ada tipe unit tersedia untuk hunian ini", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Tipe unit berhasil dimuat: " + tipeUnitList.size() + " item");

                // PERBAIKAN: Nonaktifkan spinner jika semua tipe unit sold out
                if (isHunianSoldOut || (tipeUnitList.size() > 0 && tipeUnitList.get(0).contains("(SOLD OUT)"))) {
                    spinnerTipeUnit.setEnabled(false);
                    Toast.makeText(TambahUserpActivity.this,
                            "Semua unit untuk hunian ini sudah terjual", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // === METHOD PROSPEK ===
    private void loadProspekData(String penginput) {
        Log.d(TAG, "loadProspekData: Memuat data prospek untuk penginput: " + penginput);

        if (penginput == null || penginput.trim().isEmpty()) {
            Log.e(TAG, "Penginput null atau kosong");
            Toast.makeText(this, "Username tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ProspekResponse> call = apiService.getProspekData("getProspek", penginput);
        call.enqueue(new Callback<ProspekResponse>() {
            @Override
            public void onResponse(Call<ProspekResponse> call, Response<ProspekResponse> response) {
                Log.d(TAG, "onResponse Prospek - Code: " + response.code() + ", Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ProspekResponse prospekResponse = response.body();
                    Log.d(TAG, "Prospek Response - Success: " + prospekResponse.isSuccess());

                    if (prospekResponse.isSuccess() && prospekResponse.getData() != null) {
                        prospekList.clear();

                        for (Prospek2 prospek : prospekResponse.getData()) {
                            if (prospek != null) {
                                String namaProspek = prospek.getNamaProspek();
                                if (namaProspek != null && !namaProspek.trim().isEmpty()) {
                                    prospekList.add(prospek);
                                } else {
                                    prospek.setNamaProspek("Prospek ID " + prospek.getIdProspek());
                                    prospekList.add(prospek);
                                }
                            }
                        }

                        Log.d(TAG, "Total prospek dalam list: " + prospekList.size());

                        runOnUiThread(() -> {
                            setupProspekSpinner();

                            if (prospekList.isEmpty()) {
                                Toast.makeText(TambahUserpActivity.this,
                                        "Tidak ada data prospek untuk " + penginput, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(TambahUserpActivity.this,
                                        "Data berhasil dimuat: " + prospekList.size() + " prospek", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Log.e(TAG, "Response tidak success atau data null");
                        runOnUiThread(() ->
                                Toast.makeText(TambahUserpActivity.this,
                                        "Tidak ada data prospek tersedia", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "Response error: " + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(TambahUserpActivity.this,
                                    "Gagal memuat data: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<ProspekResponse> call, Throwable t) {
                Log.e(TAG, "onFailure Prospek: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(TambahUserpActivity.this,
                            "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    setupProspekSpinner();
                });
            }
        });
    }

    private void setupProspekSpinner() {
        Log.d(TAG, "setupProspekSpinner: Mengatur spinner dengan " + prospekList.size() + " item");

        try {
            List<String> prospekNames = new ArrayList<>();
            prospekNames.add("Pilih Prospek");

            for (Prospek2 prospek : prospekList) {
                if (prospek != null) {
                    String nama = prospek.getNamaProspek();
                    if (nama != null && !nama.trim().isEmpty()) {
                        prospekNames.add(nama);
                    } else {
                        prospekNames.add("Prospek tanpa nama");
                    }
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, prospekNames) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof android.widget.TextView) {
                        android.widget.TextView textView = (android.widget.TextView) view;
                        String item = getItem(position);
                        textView.setText(item != null ? item : "Pilih Prospek");
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    if (view instanceof android.widget.TextView) {
                        android.widget.TextView textView = (android.widget.TextView) view;
                        String item = getItem(position);
                        textView.setText(item != null ? item : "Pilih Prospek");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProspek.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error dalam setupProspekSpinner: " + e.getMessage(), e);

            List<String> fallbackList = new ArrayList<>();
            fallbackList.add("Pilih Prospek");
            fallbackList.add("Tidak ada data");

            ArrayAdapter<String> fallbackAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, fallbackList);
            fallbackAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProspek.setAdapter(fallbackAdapter);

            Toast.makeText(this, "Error mengatur spinner: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // === METHOD SPINNER SETUP ===
    private void setupHunianSpinner() {
        try {
            List<String> hunianOptions = new ArrayList<>();
            hunianOptions.add("Pilih Hunian");
            hunianOptions.addAll(hunianList);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, hunianOptions) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof android.widget.TextView) {
                        android.widget.TextView textView = (android.widget.TextView) view;
                        String item = getItem(position);
                        textView.setText(item != null ? item : "Pilih Hunian");
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    if (view instanceof android.widget.TextView) {
                        android.widget.TextView textView = (android.widget.TextView) view;
                        String item = getItem(position);
                        textView.setText(item != null ? item : "Pilih Hunian");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHunian.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error dalam setupHunianSpinner: " + e.getMessage(), e);
            Toast.makeText(this, "Error mengatur spinner hunian", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTipeUnitSpinner() {
        try {
            List<String> tipeUnitOptions = new ArrayList<>();
            tipeUnitOptions.add("Pilih Tipe Unit");
            tipeUnitOptions.addAll(tipeUnitList);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, tipeUnitOptions) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof android.widget.TextView) {
                        android.widget.TextView textView = (android.widget.TextView) view;
                        String item = getItem(position);
                        textView.setText(item != null ? item : "Pilih Tipe Unit");
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    if (view instanceof android.widget.TextView) {
                        android.widget.TextView textView = (android.widget.TextView) view;
                        String item = getItem(position);
                        textView.setText(item != null ? item : "Pilih Tipe Unit");
                    }
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTipeUnit.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error dalam setupTipeUnitSpinner: " + e.getMessage(), e);
            Toast.makeText(this, "Error mengatur spinner tipe unit", Toast.LENGTH_SHORT).show();
        }
    }

    private void setHunianSpinnerEnabled(boolean enabled) {
        spinnerHunian.setEnabled(enabled);
        if (!enabled) {
            clearHunianSpinner();
        }
    }

    private void setTipeUnitSpinnerEnabled(boolean enabled) {
        spinnerTipeUnit.setEnabled(enabled);
        if (!enabled) {
            clearTipeUnitSpinner();
        }
    }

    private void clearHunianSpinner() {
        try {
            hunianList.clear();
            List<String> emptyList = new ArrayList<>();
            emptyList.add("Pilih Hunian");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, emptyList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHunian.setAdapter(adapter);
            selectedHunian = "";
        } catch (Exception e) {
            Log.e(TAG, "Error dalam clearHunianSpinner: " + e.getMessage(), e);
        }
    }

    private void clearTipeUnitSpinner() {
        try {
            tipeUnitList.clear();
            List<String> emptyList = new ArrayList<>();
            emptyList.add("Pilih Tipe Unit");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, emptyList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTipeUnit.setAdapter(adapter);
            selectedTipeUnit = "";
        } catch (Exception e) {
            Log.e(TAG, "Error dalam clearTipeUnitSpinner: " + e.getMessage(), e);
        }
    }

    private void clearHunianAndTipeUnitSpinners() {
        clearHunianSpinner();
        clearTipeUnitSpinner();
        setHunianSpinnerEnabled(false);
        setTipeUnitSpinnerEnabled(false);
    }

    // === SPINNER LISTENERS ===
    private void setupSpinnerListeners() {
        spinnerProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && proyekList.size() > position - 1) {
                    selectedProyek = proyekList.get(position - 1).getNamaProyek();
                    Log.d(TAG, "Proyek selected: " + selectedProyek);
                    setHunianSpinnerEnabled(true);
                    setTipeUnitSpinnerEnabled(false);
                    loadKavlingData(selectedProyek);
                } else {
                    selectedProyek = "";
                    setHunianSpinnerEnabled(false);
                    setTipeUnitSpinnerEnabled(false);
                    clearHunianAndTipeUnitSpinners();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProyek = "";
                setHunianSpinnerEnabled(false);
                setTipeUnitSpinnerEnabled(false);
                clearHunianAndTipeUnitSpinners();
            }
        });

        spinnerHunian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && hunianList.size() > position - 1) {
                    selectedHunian = hunianList.get(position - 1);
                    Log.d(TAG, "Hunian selected: " + selectedHunian);

                    // PERBAIKAN: Nonaktifkan spinner tipe unit jika hunian sold out
                    if (selectedHunian.contains("(SOLD OUT)")) {
                        setTipeUnitSpinnerEnabled(false);
                        clearTipeUnitSpinner();
                        Toast.makeText(TambahUserpActivity.this,
                                "Hunian ini sudah terjual semua", Toast.LENGTH_SHORT).show();
                    } else {
                        setTipeUnitSpinnerEnabled(true);
                        loadTipeUnitByHunian(selectedHunian);
                    }
                } else {
                    selectedHunian = "";
                    setTipeUnitSpinnerEnabled(false);
                    clearTipeUnitSpinner();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHunian = "";
                setTipeUnitSpinnerEnabled(false);
                clearTipeUnitSpinner();
            }
        });

        spinnerTipeUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && tipeUnitList.size() > position - 1) {
                    selectedTipeUnit = tipeUnitList.get(position - 1);
                    Log.d(TAG, "Tipe Unit selected: " + selectedTipeUnit);

                    // PERBAIKAN: Validasi jika tipe unit sold out
                    if (selectedTipeUnit.contains("(SOLD OUT)")) {
                        Toast.makeText(TambahUserpActivity.this,
                                "Tipe unit ini sudah terjual", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedTipeUnit = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTipeUnit = "";
            }
        });

        spinnerProspek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && prospekList.size() > position - 1) {
                    Prospek2 selectedProspek = prospekList.get(position - 1);
                    selectedProspekId = selectedProspek.getIdProspek();
                    selectedProspekNPWP = selectedProspek.getStatusNpwp();
                    selectedProspekBPJS = selectedProspek.getStatusBpjs();
                    fillProspekData(selectedProspek);
                } else {
                    clearProspekData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                clearProspekData();
            }
        });
    }

    // === METHOD NAVIGASI ===
    private void setupNavigation() {
        TopAppBar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, NewBeranda.class));
            finish();
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
            return true;
        });
    }

    private void setupButtonListeners() {
        btnSimpan.setOnClickListener(v -> simpanUserProspek());
        btnBatal.setOnClickListener(v -> finish());
    }

    // === METHOD HELPER ===
    private void fillProspekData(Prospek2 prospek) {
        editTextNama.setText(prospek.getNamaProspek());
        editTextEmail.setText(prospek.getEmail());
        editTextNoHp.setText(prospek.getNoHp());
        editTextAlamat.setText(prospek.getAlamat());
        setAutoFillFieldsEnabled(false);
    }

    private void clearProspekData() {
        selectedProspekId = -1;
        selectedProspekNPWP = "";
        selectedProspekBPJS = "";
        editTextNama.setText("");
        editTextEmail.setText("");
        editTextNoHp.setText("");
        editTextAlamat.setText("");
        setAutoFillFieldsEnabled(true);
    }

    private void setAutoFillFieldsEnabled(boolean enabled) {
        editTextNama.setEnabled(enabled);
        editTextEmail.setEnabled(enabled);
        editTextNoHp.setEnabled(enabled);
        editTextAlamat.setEnabled(enabled);
    }

    // === METHOD SIMPAN DATA ===
    private void simpanUserProspek() {
        // Validasi input
        if (selectedProspekId == -1) {
            Toast.makeText(this, "Pilih prospek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedProyek.isEmpty()) {
            Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedHunian.isEmpty()) {
            Toast.makeText(this, "Pilih hunian terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTipeUnit.isEmpty()) {
            Toast.makeText(this, "Pilih tipe unit terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // PERBAIKAN: Validasi tambahan untuk hunian dan tipe unit yang sold out
        if (selectedHunian.contains("(SOLD OUT)")) {
            Toast.makeText(this, "Hunian ini sudah terjual semua", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTipeUnit.contains("(SOLD OUT)")) {
            Toast.makeText(this, "Tipe unit ini sudah terjual", Toast.LENGTH_SHORT).show();
            return;
        }

        String nama = editTextNama.getText().toString().trim();
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String uangTandaJadiStr = editTextUangTandaJadi.getText().toString().trim();

        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (noHp.isEmpty()) {
            Toast.makeText(this, "No HP harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (alamat.isEmpty()) {
            Toast.makeText(this, "Alamat harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (uangTandaJadiStr.isEmpty()) {
            Toast.makeText(this, "Uang tanda jadi harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int uangTandaJadi = Integer.parseInt(uangTandaJadiStr);
            String email = editTextEmail.getText().toString().trim();

            // PERBAIKAN: Bersihkan nama hunian dan tipe unit dari label "SOLD OUT"
            String cleanHunian = selectedHunian.replace(" (SOLD OUT)", "").trim();
            String cleanTipeUnit = selectedTipeUnit.replace(" (SOLD OUT)", "").trim();

            UserProspekData userData = new UserProspekData(
                    selectedProspekId,
                    nama,
                    editTextPenginput.getText().toString(),
                    email,
                    noHp,
                    alamat,
                    selectedProyek,
                    cleanHunian,
                    cleanTipeUnit,
                    uangTandaJadi,
                    selectedProspekBPJS,
                    selectedProspekNPWP
            );

            UserProspekRequest request = new UserProspekRequest("addUserProspek", userData);
            sendDataToServer(request);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Format uang tanda jadi tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendDataToServer(UserProspekRequest request) {
        Log.d(TAG, "sendDataToServer: Mengirim data ke server");

        Call<BasicResponse> call = apiService.addUserProspek(request);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                Log.d(TAG, "onResponse - Code: " + response.code());
                Log.d(TAG, "onResponse - Success: " + response.isSuccessful());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        BasicResponse basicResponse = response.body();
                        Log.d(TAG, "Response body - Success: " + basicResponse.isSuccess());
                        Log.d(TAG, "Response body - Message: " + basicResponse.getMessage());

                        if (basicResponse.isSuccess()) {
                            Toast.makeText(TambahUserpActivity.this,
                                    "Data berhasil ditambahkan ke user prospek", Toast.LENGTH_SHORT).show();
                            updateStatusKavling();
                        } else {
                            Toast.makeText(TambahUserpActivity.this,
                                    "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(TambahUserpActivity.this,
                                "Gagal menyimpan data: Response body null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response tidak successful: " + response.code());
                    Toast.makeText(TambahUserpActivity.this,
                            "Gagal menyimpan data: " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage(), t);
                Toast.makeText(TambahUserpActivity.this,
                        "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // === METHOD UPDATE STATUS KAVLING ===
    private void updateStatusKavling() {
        Log.d(TAG, "updateStatusKavling: Mengupdate status kavling");
        Log.d(TAG, "Proyek: " + selectedProyek + ", Hunian: " + selectedHunian + ", Tipe: " + selectedTipeUnit);

        // Validasi
        if (selectedProyek == null || selectedProyek.isEmpty() ||
                selectedHunian == null || selectedHunian.isEmpty() ||
                selectedTipeUnit == null || selectedTipeUnit.isEmpty()) {
            Toast.makeText(this, "Data kavling belum lengkap", Toast.LENGTH_SHORT).show();
            return;
        }

        // PERBAIKAN: Bersihkan nama hunian dan tipe unit dari label "SOLD OUT"
        String cleanHunian = selectedHunian.replace(" (SOLD OUT)", "").trim();
        String cleanTipeUnit = selectedTipeUnit.replace(" (SOLD OUT)", "").trim();

        // Show loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Mengupdate...");

        // Debug log
        Log.d(TAG, "Mengirim request update dengan parameter:");
        Log.d(TAG, "Proyek: " + selectedProyek);
        Log.d(TAG, "Hunian: " + cleanHunian);
        Log.d(TAG, "Tipe Unit: " + cleanTipeUnit);

        Call<UpdateStatusResponse> call = apiService.updateStatusKavling(
                "updateStatusKavling",
                selectedProyek,
                cleanHunian,
                cleanTipeUnit
        );

        call.enqueue(new Callback<UpdateStatusResponse>() {
            @Override
            public void onResponse(Call<UpdateStatusResponse> call, Response<UpdateStatusResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                Log.d(TAG, "Response Code: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        UpdateStatusResponse updateResponse = response.body();
                        Log.d(TAG, "Response Success: " + updateResponse.isSuccess());
                        Log.d(TAG, "Response Message: " + updateResponse.getMessage());
                        Log.d(TAG, "Affected Rows: " + updateResponse.getAffectedRows());

                        if (updateResponse.isSuccess()) {
                            Toast.makeText(TambahUserpActivity.this,
                                    updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "UPDATE BERHASIL: " + updateResponse.getMessage());
                        } else {
                            Toast.makeText(TambahUserpActivity.this,
                                    "Gagal: " + updateResponse.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "UPDATE GAGAL: " + updateResponse.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(TambahUserpActivity.this,
                                "Error: Response body null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response tidak successful: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error Body: " + errorBody);

                        if (errorBody.contains("JSON invalid") || errorBody.contains("syntax error") || errorBody.contains("Expected BEGIN_OBJECT")) {
                            Toast.makeText(TambahUserpActivity.this,
                                    "Error format data dari server. Silakan coba lagi.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TambahUserpActivity.this,
                                    "Error: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                        Toast.makeText(TambahUserpActivity.this,
                                "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                navigateToHome();
            }

            @Override
            public void onFailure(Call<UpdateStatusResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");

                Log.e(TAG, "Network error: " + t.getMessage(), t);
                Toast.makeText(TambahUserpActivity.this,
                        "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                navigateToHome();
            }
        });
    }

    private void navigateToHome() {
        runOnUiThread(() -> {
            Intent intent = new Intent(TambahUserpActivity.this, NewBeranda.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}