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
    private Spinner spinnerProyek, spinnerHunian, spinnerTipeUnit, spinnerNPWP, spinnerBPJS, spinnerStatusSPS;

    // EditTexts
    private EditText editTextPenginput, editTextNama, editTextEmail, editTextNoHp, editTextAlamat;

    // Selected values
    private String selectedProyek = "", selectedHunian = "", selectedTipeUnit = "",
            selectedNPWP = "", selectedBPJS = "", selectedStatusSPS = "";

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
        String statusNPWP = intent.getStringExtra("STATUS_NPWP");
        String statusBPJS = intent.getStringExtra("STATUS_BPJS");
        String statusSPS = intent.getStringExtra("STATUS_SPS");  // Status SPS dari intent

        // Simpan tipe hunian lama
        oldTipeHunian = tipeHunian != null ? tipeHunian : "";

        initializeViews();
        setupBottomNavigation();

        // Set data to views
        setDataToViews(penginput, nama, email, noHp, alamat, proyek, hunian, tipeHunian,
                statusNPWP, statusBPJS, statusSPS);

        // Load initial data
        loadProyekData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        editTextPenginput = findViewById(R.id.editTextPenginput);
        editTextNama = findViewById(R.id.editTextNama);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNoHp = findViewById(R.id.editTextNoHp);
        editTextAlamat = findViewById(R.id.editTextAlamat);

        spinnerProyek = findViewById(R.id.spinnerProyek);
        spinnerHunian = findViewById(R.id.hunian);
        spinnerTipeUnit = findViewById(R.id.spinnerTipeUnit);
        spinnerNPWP = findViewById(R.id.spinnerNPWP);
        spinnerBPJS = findViewById(R.id.spinnerBPJS);
        spinnerStatusSPS = findViewById(R.id.spinnerStatusSPS);

        setupStatusSpinners();
        setupStatusSPSpinner();

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupStatusSpinners() {
        ArrayAdapter<CharSequence> npwpAdapter = ArrayAdapter.createFromResource(this,
                R.array.opsi_spinnerNPWP, android.R.layout.simple_spinner_item);
        npwpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNPWP.setAdapter(npwpAdapter);

        ArrayAdapter<CharSequence> bpjsAdapter = ArrayAdapter.createFromResource(this,
                R.array.opsi_spinnerBPJS, android.R.layout.simple_spinner_item);
        bpjsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBPJS.setAdapter(bpjsAdapter);

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

    private void setupStatusSPSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_sps_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusSPS.setAdapter(adapter);

        spinnerStatusSPS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatusSPS = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedStatusSPS = "";
            }
        });
    }

    private void setDataToViews(String penginput, String nama, String email, String noHp,
                                String alamat, String proyek, String hunian, String tipeHunian,
                                String statusNPWP, String statusBPJS, String statusSPS) {
        editTextPenginput.setText(penginput != null ? penginput : "");
        editTextNama.setText(nama != null ? nama : "");
        editTextEmail.setText(email != null ? email : "");
        editTextNoHp.setText(noHp != null ? noHp : "");
        editTextAlamat.setText(alamat != null ? alamat : "");

        if (statusNPWP != null) setSpinnerSelection(spinnerNPWP, statusNPWP);
        if (statusBPJS != null) setSpinnerSelection(spinnerBPJS, statusBPJS);
        if (statusSPS != null) setSpinnerSelection(spinnerStatusSPS, statusSPS);

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

                        if (selectedProyek != null && !selectedProyek.isEmpty()) {
                            setSpinnerSelection(spinnerProyek, selectedProyek);
                            int proyekId = getProyekIdByName(selectedProyek);
                            if (proyekId != -1) {
                                loadHunianData(proyekId);
                            }
                        }
                    } else {
                        Toast.makeText(EditDataUserpActivity.this, "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataUserpActivity.this, "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Toast.makeText(EditDataUserpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProyekSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proyekNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProyek.setAdapter(adapter);

        spinnerProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProyek = parent.getItemAtPosition(position).toString();
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
                        if (data != null) hunianNameList.addAll(data);
                        setupHunianSpinner();

                        if (selectedHunian != null && !selectedHunian.isEmpty()) {
                            setSpinnerSelection(spinnerHunian, selectedHunian);
                            loadKavlingData(selectedProyek);
                        }
                    } else {
                        Toast.makeText(EditDataUserpActivity.this, "Gagal memuat data hunian: " + hunianResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataUserpActivity.this, "Gagal memuat data hunian", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HunianResponse> call, Throwable t) {
                Toast.makeText(EditDataUserpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupHunianSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hunianNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHunian.setAdapter(adapter);

        spinnerHunian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHunian = parent.getItemAtPosition(position).toString();
                tipeUnitNameList.clear();
                selectedTipeUnit = "";
                if (adapterTipeUnit != null) adapterTipeUnit.notifyDataSetChanged();
                filterTipeUnitData(selectedProyek, selectedHunian);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHunian = "";
            }
        });
    }

    private void loadKavlingData(String proyek) {
        Log.d("EditDataUserp", "Loading kavling data - Proyek: " + proyek);
        Call<KavlingResponse> call = apiService.getKavlingByProyek("getKavling", proyek);
        call.enqueue(new Callback<KavlingResponse>() {
            @Override
            public void onResponse(Call<KavlingResponse> call, Response<KavlingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KavlingResponse kavlingResponse = response.body();
                    if (kavlingResponse.isSuccess()) {
                        List<Kavling> data = kavlingResponse.getData();
                        kavlingList.clear();
                        if (data != null) kavlingList.addAll(data);
                        filterTipeUnitData(selectedProyek, selectedHunian);
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

    private void filterTipeUnitData(String proyek, String hunian) {
        tipeUnitNameList.clear();
        if (kavlingList.isEmpty()) {
            loadKavlingData(proyek);
            return;
        }

        for (Kavling kavling : kavlingList) {
            String tipeHunian = kavling.getTipeHunian();
            String kavlingHunian = kavling.getHunian();
            String kavlingProyek = kavling.getProyek();

            boolean hunianMatches = kavlingHunian != null && kavlingHunian.equalsIgnoreCase(hunian);
            boolean proyekMatches = kavlingProyek != null && kavlingProyek.equalsIgnoreCase(proyek);

            if (hunianMatches && proyekMatches && tipeHunian != null && !tipeHunian.isEmpty()) {
                String status = kavling.getStatusPenjualan();
                boolean isOnProgress = "On Progress".equalsIgnoreCase(status);
                boolean isOldData = tipeHunian.equals(oldTipeHunian);
                if (isOnProgress || isOldData) {
                    String displayName = isOnProgress ? tipeHunian : tipeHunian + " (SOLD OUT)";
                    if (!tipeUnitNameList.contains(displayName)) {
                        tipeUnitNameList.add(displayName);
                    }
                }
            }
        }

        setupTipeUnitSpinner();

        if (selectedTipeUnit != null && !selectedTipeUnit.isEmpty()) {
            String displayNameToSelect = selectedTipeUnit;
            for (String displayName : tipeUnitNameList) {
                if (displayName.replace(" (SOLD OUT)", "").equals(selectedTipeUnit)) {
                    displayNameToSelect = displayName;
                    break;
                }
            }
            setSpinnerSelection(spinnerTipeUnit, displayNameToSelect);
        } else if (!tipeUnitNameList.isEmpty()) {
            spinnerTipeUnit.setSelection(0);
            selectedTipeUnit = tipeUnitNameList.get(0).replace(" (SOLD OUT)", "");
        }

        if (tipeUnitNameList.isEmpty()) {
            Toast.makeText(this, "Tidak ada tipe unit untuk hunian: " + hunian, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTipeUnitSpinner() {
        if (tipeUnitNameList.isEmpty()) tipeUnitNameList.add("Tidak ada tipe unit tersedia");
        adapterTipeUnit = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipeUnitNameList);
        adapterTipeUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipeUnit.setAdapter(adapterTipeUnit);

        spinnerTipeUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDisplayName = parent.getItemAtPosition(position).toString();
                selectedTipeUnit = selectedDisplayName.replace(" (SOLD OUT)", "").trim();
                if (selectedTipeUnit.equals("Tidak ada tipe unit tersedia")) selectedTipeUnit = "";

                boolean isSoldOut = selectedDisplayName.contains("(SOLD OUT)");
                boolean isOldData = selectedTipeUnit.equals(oldTipeHunian);
                if (isSoldOut && !isOldData) {
                    Toast.makeText(EditDataUserpActivity.this, "Tipe unit ini sudah terjual", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTipeUnit = "";
            }
        });
    }

    private int getProyekIdByName(String proyekName) {
        for (Proyek proyek : proyekObjectList) {
            if (proyek.getNamaProyek().equals(proyekName)) return proyek.getIdProyek();
        }
        return -1;
    }

    private void setupButtonListeners() {
        Button btnSimpan = findViewById(R.id.btnSimpan);
        Button btnBatal = findViewById(R.id.btnBatal);
        btnSimpan.setOnClickListener(v -> updateUserProspek());
        btnBatal.setOnClickListener(v -> finish());
    }

    private void updateUserProspek() {
        String penginput = editTextPenginput.getText().toString().trim();
        String nama = editTextNama.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim(); // tidak wajib
        String noHp = editTextNoHp.getText().toString().trim();
        String alamat = editTextAlamat.getText().toString().trim();
        String statusSPS = selectedStatusSPS;

        // Log untuk debugging
        Log.d("UpdateUserProspek", "penginput: '" + penginput + "'");
        Log.d("UpdateUserProspek", "nama: '" + nama + "'");
        Log.d("UpdateUserProspek", "noHp: '" + noHp + "'");
        Log.d("UpdateUserProspek", "alamat: '" + alamat + "'");
        Log.d("UpdateUserProspek", "selectedProyek: '" + selectedProyek + "'");
        Log.d("UpdateUserProspek", "selectedHunian: '" + selectedHunian + "'");
        Log.d("UpdateUserProspek", "selectedTipeUnit: '" + selectedTipeUnit + "'");
        Log.d("UpdateUserProspek", "statusSPS: '" + statusSPS + "'");

        // Validasi per field (email tidak wajib)
        if (penginput.isEmpty()) {
            Toast.makeText(this, "Nama penginput tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nama.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (noHp.isEmpty()) {
            Toast.makeText(this, "No HP tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (alamat.isEmpty()) {
            Toast.makeText(this, "Alamat tidak boleh kosong", Toast.LENGTH_SHORT).show();
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
        if (statusSPS.isEmpty() || statusSPS.equals("Pilih Status SPS")) {
            Toast.makeText(this, "Pilih status SPS terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDisplayName = getSelectedTipeUnitDisplayName();
        boolean isSoldOut = selectedDisplayName.contains("(SOLD OUT)");
        boolean isOldData = selectedTipeUnit.equals(oldTipeHunian);
        if (isSoldOut && !isOldData) {
            Toast.makeText(this, "Tipe unit yang dipilih sudah terjual", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userProspekId == -1) {
            Toast.makeText(this, "Error: ID UserProspek tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("UpdateUserProspek", "=== DATA YANG DIKIRIM ===");
        Log.d("UpdateUserProspek", "id_userprospek: " + userProspekId);
        Log.d("UpdateUserProspek", "nama_penginput: " + penginput);
        Log.d("UpdateUserProspek", "nama_user: " + nama);
        Log.d("UpdateUserProspek", "email: " + email);
        Log.d("UpdateUserProspek", "no_hp: " + noHp);
        Log.d("UpdateUserProspek", "alamat: " + alamat);
        Log.d("UpdateUserProspek", "proyek: " + selectedProyek);
        Log.d("UpdateUserProspek", "hunian: " + selectedHunian);
        Log.d("UpdateUserProspek", "tipe_hunian: " + selectedTipeUnit);
        Log.d("UpdateUserProspek", "status_bpjs: " + selectedBPJS);
        Log.d("UpdateUserProspek", "status_npwp: " + selectedNPWP);
        Log.d("UpdateUserProspek", "status_sps: " + statusSPS);

        Call<ResponseBody> call = apiService.updateUserProspekWithHistori(
                "update_userprospek",
                userProspekId,
                penginput,
                nama,
                email,
                noHp,
                alamat,
                selectedProyek,
                selectedHunian,
                selectedTipeUnit,
                selectedBPJS,
                selectedNPWP,
                statusSPS
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseString);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        StringBuilder toastMessage = new StringBuilder(message);
                        if (jsonResponse.has("debug")) {
                            JSONObject debug = jsonResponse.getJSONObject("debug");
                            if (debug.has("kavling_update_results")) {
                                JSONObject kavlingResults = debug.getJSONObject("kavling_update_results");
                                String kavlingMessage = kavlingResults.getString("message");
                                if (!kavlingMessage.isEmpty()) toastMessage.append("\n").append(kavlingMessage);
                            }
                        }

                        if (success) {
                            Toast.makeText(EditDataUserpActivity.this, toastMessage.toString(), Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(() -> {
                                setResult(RESULT_OK);
                                finish();
                            }, 2000);
                        } else {
                            Toast.makeText(EditDataUserpActivity.this, "Gagal: " + toastMessage.toString(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("UpdateUserProspek", "Response error: " + response.code());
                        Toast.makeText(EditDataUserpActivity.this, "Error response: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("UpdateUserProspek", "Error parsing response", e);
                    Toast.makeText(EditDataUserpActivity.this, "Error parsing: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UpdateUserProspek", "Network Error: " + t.getMessage());
                Toast.makeText(EditDataUserpActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

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
            if (id == R.id.nav_home) startActivity(new Intent(this, NewBeranda.class));
            else if (id == R.id.nav_folder) startActivity(new Intent(this, LihatDataActivity.class));
            else if (id == R.id.nav_news) startActivity(new Intent(this, NewsActivity.class));
            else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
        });
    }
}