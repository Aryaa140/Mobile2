package com.example.mobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputNipActivity extends BaseActivity {

    // UI Components
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private Spinner spinnerDivisi;
    private EditText editTextNoNIP;
    private Button btnSimpan, btnBatal, btnDateOut;
    private LinearLayout layoutUserFields;
    private EditText editTextUsername, editTextPassword, editTextEmail;

    // Data
    private List<LevelItem> levelList = new ArrayList<>();
    private LevelItem selectedLevelItem;
    private String currentUserLevel;
    private int nextSuffix = 1;
    private String selectedDateOut = null;
    private Calendar calendar;

    // Mapping level ke prefix (sudah sesuai dengan data tabel)
    private static final Map<String, String> LEVEL_PREFIX_MAP = new HashMap<>();
    static {
        LEVEL_PREFIX_MAP.put("Operator Freelance", "MF");
        LEVEL_PREFIX_MAP.put("Operator Inhouse", "MI");
        LEVEL_PREFIX_MAP.put("Admin Operator", "AO");
        LEVEL_PREFIX_MAP.put("Admin Pembuat", "AP");
        LEVEL_PREFIX_MAP.put("Operator Kordinator", "KOR");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_nip_activity);

        currentUserLevel = getIntent().getStringExtra("user_level");
        if (currentUserLevel == null) currentUserLevel = "";

        initViews();
        setupToolbar();
        setupClickListeners();
        setupDatePicker();
        fetchLevels();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        setupBottomNavigation();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        spinnerDivisi = findViewById(R.id.spinnerDivisi);
        editTextNoNIP = findViewById(R.id.editTextNoNIP);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        btnDateOut = findViewById(R.id.btnDateOut);
        layoutUserFields = findViewById(R.id.layoutUserFields);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        calendar = Calendar.getInstance();
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupBottomNavigation() {
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

    private void setupDatePicker() {
        btnDateOut.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDateOut = dateFormat.format(calendar.getTime());
                    btnDateOut.setText("Tanggal Keluar: " + selectedDateOut);
                    btnDateOut.setHint(selectedDateOut);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void fetchLevels() {
        showLoading(true);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<LevelResponse> call = api.getLevels();
        call.enqueue(new Callback<LevelResponse>() {
            @Override
            public void onResponse(Call<LevelResponse> call, Response<LevelResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    levelList = response.body().getData();
                    filterAndSetupSpinner();
                } else {
                    Toast.makeText(InputNipActivity.this, "Gagal memuat data divisi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LevelResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(InputNipActivity.this, "Koneksi error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndSetupSpinner() {
        List<LevelItem> filtered = new ArrayList<>();

        if (currentUserLevel.equals("Admin Pembuat")) {
            filtered.addAll(levelList);
        } else {
            for (LevelItem item : levelList) {
                if (!item.getLevel().equals(currentUserLevel)) {
                    filtered.add(item);
                }
            }
        }

        ArrayAdapter<LevelItem> adapter = new ArrayAdapter<LevelItem>(
                this, android.R.layout.simple_spinner_item, filtered) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivisi.setAdapter(adapter);

        spinnerDivisi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLevelItem = (LevelItem) parent.getItemAtPosition(position);
                String levelName = selectedLevelItem.getLevel();
                if (levelName.equals("Admin Operator") || levelName.equals("Admin Pembuat")) {
                    layoutUserFields.setVisibility(View.VISIBLE);
                } else {
                    layoutUserFields.setVisibility(View.GONE);
                }
                String prefix = getPrefixFromLevel(levelName);
                fetchLatestSuffix(prefix);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // Mapping dengan logging dan fallback
    private String getPrefixFromLevel(String levelName) {
        if (levelName == null) return "";
        String trimmed = levelName.trim();
        Log.d("NIP_DEBUG", "Original levelName: '" + levelName + "', trimmed: '" + trimmed + "'");

        String prefix = LEVEL_PREFIX_MAP.get(trimmed);
        if (prefix == null) {
            Log.e("NIP_DEBUG", "Tidak ditemukan prefix untuk: '" + levelName + "'");
            for (Map.Entry<String, String> entry : LEVEL_PREFIX_MAP.entrySet()) {
                if (trimmed.contains(entry.getKey()) || entry.getKey().contains(trimmed)) {
                    prefix = entry.getValue();
                    Log.d("NIP_DEBUG", "Fallback match: " + entry.getKey() + " -> " + prefix);
                    break;
                }
            }
            if (prefix == null) {
                return "";
            }
        }
        return prefix;
    }

    private void fetchLatestSuffix(String prefix) {
        if (prefix.isEmpty()) {
            nextSuffix = 1;
            updateNipField();
            return;
        }
        showLoading(true);
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        Call<LatestSuffixResponse> call = api.getLatestNipSuffix(prefix);
        call.enqueue(new Callback<LatestSuffixResponse>() {
            @Override
            public void onResponse(Call<LatestSuffixResponse> call, Response<LatestSuffixResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    nextSuffix = response.body().getMaxSuffix() + 1;
                } else {
                    nextSuffix = 1;
                }
                updateNipField();
            }

            @Override
            public void onFailure(Call<LatestSuffixResponse> call, Throwable t) {
                showLoading(false);
                nextSuffix = 1;
                updateNipField();
                Toast.makeText(InputNipActivity.this, "Gagal mengambil nomor urut, gunakan 1", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNipField() {
        if (selectedLevelItem == null) return;
        String prefix = getPrefixFromLevel(selectedLevelItem.getLevel());
        if (prefix.isEmpty()) {
            Toast.makeText(this, "Kode divisi tidak dikenal: " + selectedLevelItem.getLevel(), Toast.LENGTH_SHORT).show();
            return;
        }
        String generatedNip = prefix + "-" + nextSuffix;
        editTextNoNIP.setText(generatedNip);
        editTextNoNIP.setSelection(generatedNip.length());
        editTextNoNIP.setHint("NIP: " + generatedNip);
    }

    private void setupClickListeners() {
        btnSimpan.setOnClickListener(v -> simpanNIP());
        btnBatal.setOnClickListener(v -> onBackPressed());
    }

    private void simpanNIP() {
        if (selectedLevelItem == null) {
            Toast.makeText(this, "Pilih divisi terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String noNIP = editTextNoNIP.getText().toString().trim();
        if (TextUtils.isEmpty(noNIP)) {
            editTextNoNIP.setError("No. NIP tidak boleh kosong");
            editTextNoNIP.requestFocus();
            return;
        }

        String levelName = selectedLevelItem.getLevel();
        boolean createUser = levelName.equals("Admin Operator") || levelName.equals("Admin Pembuat");
        String username = null, password = null, email = null;

        if (createUser) {
            username = editTextUsername.getText().toString().trim();
            password = editTextPassword.getText().toString().trim();
            email = editTextEmail.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Username, password, dan email harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                editTextPassword.setError("Password minimal 6 karakter");
                editTextPassword.requestFocus();
                return;
            }
        }

        // ✅ BARU: Assign ke variabel final untuk inner class (Callback)
        final String finalUsername = username;
        final String finalPassword = password;
        final String finalEmail = email;

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        String prefix = getPrefixFromLevel(levelName);
        NipRequest request;
        if (createUser) {
            request = new NipRequest(noNIP, selectedDateOut, username, password, email,
                    selectedLevelItem.getLevel(),
                    selectedLevelItem.getDivisi());
        } else {
            request = new NipRequest(noNIP, selectedDateOut, selectedLevelItem.getDivisi());
        }

        Call<BasicResponse> call = apiService.inputNIP(request);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {

                        // ✅ BARU: Log aksi CREATE NIP (pakai finalUsername agar aman di inner class)
                        try {
                            ActivityLogger.log(
                                    InputNipActivity.this,
                                    ActivityLogger.AKSI_CREATE,
                                    ActivityLogger.FITUR_USER,
                                    "Tambah NIP baru: " + noNIP
                                            + " | Level: " + levelName
                                            + " | Divisi: " + selectedLevelItem.getDivisi()
                                            + (createUser ? " | User: " + finalUsername : ""),
                                    "InputNipActivity",
                                    null,
                                    noNIP
                            );
                        } catch (Exception logEx) {
                            Log.e("InputNipActivity", "Error saat log NIP: " + logEx.getMessage());
                        }
                        // ===============================================

                        Toast.makeText(InputNipActivity.this, basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        editTextNoNIP.setText("");
                        selectedDateOut = null;
                        btnDateOut.setText("Masukkan Tanggal Keluar");
                        btnDateOut.setHint("Masukkan Tanggal Keluar");
                        editTextUsername.setText("");
                        editTextPassword.setText("");
                        editTextEmail.setText("");
                        String currentPrefix = getPrefixFromLevel(selectedLevelItem.getLevel());
                        fetchLatestSuffix(currentPrefix);
                    } else {
                        Toast.makeText(InputNipActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputNipActivity.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(InputNipActivity.this, "Gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        btnSimpan.setEnabled(!isLoading);
        btnBatal.setEnabled(!isLoading);
        spinnerDivisi.setEnabled(!isLoading);
        editTextNoNIP.setEnabled(!isLoading);
        btnDateOut.setEnabled(!isLoading);
        if (layoutUserFields.getVisibility() == View.VISIBLE) {
            editTextUsername.setEnabled(!isLoading);
            editTextPassword.setEnabled(!isLoading);
            editTextEmail.setEnabled(!isLoading);
        }
        btnSimpan.setText(isLoading ? "Menyimpan..." : "Simpan");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}