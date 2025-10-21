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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class InputKavlingActivity extends AppCompatActivity {

    private Spinner spinnerProyek, spinnerHunian, spinnerStatus;
    private EditText editTextKavling;
    private Button btnSimpan, btnBatal;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;

    private List<Proyek> listProyek = new ArrayList<>();
    private List<String> listHunian = new ArrayList<>();

    private String selectedProyek = "";
    private String selectedHunian = "";
    private String selectedStatus = "On Progress";
    private int selectedIdProyek = 0;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_kavling);

        // Initialize Retrofit
        apiService = RetrofitClient.getClient().create(ApiService.class);

        initializeViews();
        setupNavigation(); // TAMBAHKAN SETUP NAVIGASI
        setupSpinners();
        loadProyekData();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        spinnerProyek = findViewById(R.id.spinnerRoleProyek);
        spinnerHunian = findViewById(R.id.spinnerRoleHunian);
        spinnerStatus = findViewById(R.id.spinnerRoleStatusPenjualan);
        editTextKavling = findViewById(R.id.editTextKavling);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        topAppBar = findViewById(R.id.topAppBar); // TAMBAHKAN INI
        bottomNavigationView = findViewById(R.id.bottom_navigation); // TAMBAHKAN INI

        // Disable spinner hunian dan status awal
        spinnerHunian.setEnabled(false);
        spinnerStatus.setEnabled(false);
        editTextKavling.setEnabled(false);
    }

    // TAMBAHKAN METHOD SETUP NAVIGASI
    private void setupNavigation() {
        // Navigasi toolbar - back button
        topAppBar.setNavigationOnClickListener(v -> {
            navigateToHome();
        });

        // Bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, NewBeranda.class));
                finish();
                return true;
            } else if (id == R.id.nav_folder) {
                startActivity(new Intent(this, LihatDataActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, NewsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupSpinners() {
        // Setup Status Penjualan Spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.status_penjualan_array,
                android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Spinner Proyek listener
        spinnerProyek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Proyek selectedProyekObj = listProyek.get(position - 1);
                    selectedProyek = selectedProyekObj.getNamaProyek();
                    selectedIdProyek = selectedProyekObj.getIdProyek();
                    loadHunianData(selectedIdProyek);
                    spinnerHunian.setEnabled(true);
                } else {
                    selectedProyek = "";
                    selectedIdProyek = 0;
                    spinnerHunian.setEnabled(false);
                    spinnerStatus.setEnabled(false);
                    editTextKavling.setEnabled(false);
                    clearHunianSpinner();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerHunian.setEnabled(false);
                spinnerStatus.setEnabled(false);
                editTextKavling.setEnabled(false);
            }
        });

        // Spinner Hunian listener
        spinnerHunian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedHunian = listHunian.get(position - 1);
                    spinnerStatus.setEnabled(true);
                    editTextKavling.setEnabled(true);
                } else {
                    selectedHunian = "";
                    spinnerStatus.setEnabled(false);
                    editTextKavling.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerStatus.setEnabled(false);
                editTextKavling.setEnabled(false);
            }
        });

        // Spinner Status listener
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpanDataKavling();
            }
        });

        // UBAH btnBatal MENJADI navigateToHome()
        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
            }
        });
    }

    private void loadProyekData() {
        Call<ProyekResponse> call = apiService.getProyek();
        call.enqueue(new Callback<ProyekResponse>() {
            @Override
            public void onResponse(Call<ProyekResponse> call, Response<ProyekResponse> response) {
                System.out.println("Response Code: " + response.code());
                System.out.println("Response Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    ProyekResponse proyekResponse = response.body();
                    System.out.println("Success: " + proyekResponse.isSuccess());
                    System.out.println("Message: " + proyekResponse.getMessage());

                    if (proyekResponse.isSuccess() && proyekResponse.getData() != null) {
                        listProyek = proyekResponse.getData();

                        List<String> proyekNames = new ArrayList<>();
                        proyekNames.add("Pilih Proyek");

                        for (Proyek proyek : listProyek) {
                            proyekNames.add(proyek.getNamaProyek());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                InputKavlingActivity.this,
                                android.R.layout.simple_spinner_item,
                                proyekNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProyek.setAdapter(adapter);

                        System.out.println("Data Proyek loaded: " + listProyek.size());

                    } else {
                        Toast.makeText(InputKavlingActivity.this,
                                "Data proyek kosong: " + (proyekResponse.getMessage() != null ? proyekResponse.getMessage() : "Tidak ada data"),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputKavlingActivity.this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProyekResponse> call, Throwable t) {
                Toast.makeText(InputKavlingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("Error: " + t.getMessage());
            }
        });
    }

    private void loadHunianData(int idProyek) {
        System.out.println("Loading hunian untuk proyek ID: " + idProyek);

        Call<HunianResponse> call = apiService.getHunianByProyek(idProyek);
        call.enqueue(new Callback<HunianResponse>() {
            @Override
            public void onResponse(Call<HunianResponse> call, Response<HunianResponse> response) {
                System.out.println("Hunian Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    HunianResponse hunianResponse = response.body();
                    System.out.println("Hunian Success: " + hunianResponse.isSuccess());
                    System.out.println("Hunian Message: " + hunianResponse.getMessage());

                    if (hunianResponse.isSuccess() && hunianResponse.getData() != null) {
                        listHunian = hunianResponse.getData();

                        List<String> hunianNames = new ArrayList<>();
                        hunianNames.add("Pilih Hunian");

                        for (String hunian : listHunian) {
                            hunianNames.add(hunian);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                InputKavlingActivity.this,
                                android.R.layout.simple_spinner_item,
                                hunianNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerHunian.setAdapter(adapter);

                        System.out.println("Data Hunian loaded: " + listHunian.size());

                        // Enable spinner jika ada data
                        if (listHunian.size() > 1) {
                            spinnerHunian.setEnabled(true);
                        }

                    } else {
                        Toast.makeText(InputKavlingActivity.this,
                                "Data hunian kosong: " + (hunianResponse.getMessage() != null ? hunianResponse.getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                        clearHunianSpinner();
                    }
                } else {
                    Toast.makeText(InputKavlingActivity.this, "Gagal memuat data hunian", Toast.LENGTH_SHORT).show();
                    clearHunianSpinner();
                }
            }

            @Override
            public void onFailure(Call<HunianResponse> call, Throwable t) {
                Toast.makeText(InputKavlingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("Hunian API Error: " + t.getMessage());
                clearHunianSpinner();
            }
        });
    }

    private void clearHunianSpinner() {
        listHunian.clear();
        List<String> hunianNames = new ArrayList<>();
        hunianNames.add("Pilih Hunian");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                hunianNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHunian.setAdapter(adapter);
    }

    private void simpanDataKavling() {
        String tipeHunian = editTextKavling.getText().toString().trim();

        // Validasi
        if (selectedProyek.isEmpty()) {
            Toast.makeText(this, "Pilih proyek terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedHunian.isEmpty()) {
            Toast.makeText(this, "Pilih hunian terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipeHunian.isEmpty()) {
            Toast.makeText(this, "Masukkan tipe hunian", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<BasicResponse> call = apiService.tambahKavlingForm(
                "tambah_kavling",
                tipeHunian,        // tipe_hunian - ambil dari editTextKavling
                selectedHunian,    // hunian - ambil dari spinner
                selectedProyek,    // proyek
                selectedStatus,    // status_penjualan
                ""                 // kode_kavling - dikosongkan
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                System.out.println("Response Code: " + response.code());
                System.out.println("Response Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Toast.makeText(InputKavlingActivity.this,
                                basicResponse.getMessage() != null ? basicResponse.getMessage() : "Data kavling berhasil disimpan",
                                Toast.LENGTH_SHORT).show();
                        resetForm();

                        // Redirect ke home setelah berhasil simpan (opsional)
                        new android.os.Handler().postDelayed(
                                () -> navigateToHome(),
                                1500
                        );
                    } else {
                        Toast.makeText(InputKavlingActivity.this,
                                basicResponse.getMessage() != null ? basicResponse.getMessage() : "Gagal menyimpan data",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Gagal menyimpan data - ";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg += "Response error: " + response.code();
                        }
                    } else {
                        errorMsg += "Response error: " + response.code();
                    }
                    Toast.makeText(InputKavlingActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(InputKavlingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetForm() {
        spinnerProyek.setSelection(0);
        spinnerHunian.setSelection(0);
        spinnerStatus.setSelection(0);
        editTextKavling.setText("");

        spinnerHunian.setEnabled(false);
        spinnerStatus.setEnabled(false);
        editTextKavling.setEnabled(false);

        selectedProyek = "";
        selectedHunian = "";
        selectedStatus = "On Progress";
        selectedIdProyek = 0;
    }

    // TAMBAHKAN METHOD NAVIGASI KE HOME
    private void navigateToHome() {
        Intent intent = new Intent(InputKavlingActivity.this, NewBeranda.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // TAMBAHKAN onBackPressed()
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome();
    }
}