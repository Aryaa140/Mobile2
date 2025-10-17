package com.example.mobile;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputNipActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private Spinner spinnerDivisi;
    private EditText editTextNoNIP;
    private Button btnSimpan, btnBatal, btnDateOut;
    private String selectedDivisi = "Operator Inhouse";
    private String kodeDivisi = "MI";
    private int nextIdNumber = 1;
    private String selectedDateOut = null;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_nip_activity);

        initViews();
        setupToolbar();
        setupSpinner();
        setupClickListeners();
        setupDatePicker();
        loadLatestNipId();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        spinnerDivisi = findViewById(R.id.spinnerDivisi);
        editTextNoNIP = findViewById(R.id.editTextNoNIP);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        btnDateOut = findViewById(R.id.btnDateOut);
        calendar = Calendar.getInstance();
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupSpinner() {
        List<String> divisiList = new ArrayList<>();
        divisiList.add("Operator Inhouse");
        divisiList.add("Operator Freelance");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, divisiList) {
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
                selectedDivisi = divisiList.get(position);
                if (selectedDivisi.equals("Operator Inhouse")) {
                    kodeDivisi = "MI-";
                } else {
                    kodeDivisi = "MF-";
                }
                updateNipField();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDivisi = "Operator Inhouse";
                kodeDivisi = "MI-";
                updateNipField();
            }
        });
    }

    private void setupDatePicker() {
        btnDateOut.setOnClickListener(v -> {
            showDatePickerDialog();
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        selectedDateOut = dateFormat.format(calendar.getTime());

                        btnDateOut.setText("Tanggal Keluar: " + selectedDateOut);
                        btnDateOut.setHint(selectedDateOut);

                        Log.d("InputNipActivity", "Tanggal dipilih: " + selectedDateOut);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void loadLatestNipId() {
        showLoading(true);
        Log.d("InputNipActivity", "Mengambil ID NIP terbesar dari database");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<LatestIdResponse> call = apiService.getLatestNipId();

        call.enqueue(new Callback<LatestIdResponse>() {
            @Override
            public void onResponse(Call<LatestIdResponse> call, Response<LatestIdResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LatestIdResponse idResponse = response.body();

                    if (idResponse.isSuccess()) {
                        nextIdNumber = idResponse.getLatestId() + 1;

                        Log.d("InputNipActivity", "ID NIP terbesar: " + idResponse.getLatestId());
                        Log.d("InputNipActivity", "Next ID Number: " + nextIdNumber);

                        runOnUiThread(() -> {
                            updateNipField();
                            Toast.makeText(InputNipActivity.this,
                                    "ID terbesar: " + idResponse.getLatestId() + ", Next: " + nextIdNumber,
                                    Toast.LENGTH_LONG).show();
                        });
                    } else {
                        handleError();
                    }
                } else {
                    handleError();
                }
            }

            @Override
            public void onFailure(Call<LatestIdResponse> call, Throwable t) {
                showLoading(false);
                Log.e("InputNipActivity", "Error: " + t.getMessage());
                handleError();
            }
        });
    }

    private void handleError() {
        nextIdNumber = 1;
        runOnUiThread(() -> {
            updateNipField();
            Toast.makeText(InputNipActivity.this, "Menggunakan ID default: 1", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateNipField() {
        String nipValue = kodeDivisi + nextIdNumber;
        editTextNoNIP.setText(nipValue);
        editTextNoNIP.setSelection(nipValue.length());
        editTextNoNIP.setHint("NIP: " + nipValue);

        Log.d("InputNipActivity", "NIP di-set ke: " + nipValue);
    }

    private void setupClickListeners() {
        btnSimpan.setOnClickListener(v -> {
            simpanNIP();
        });

        btnBatal.setOnClickListener(v -> {
            onBackPressed();
        });

        editTextNoNIP.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String currentText = editTextNoNIP.getText().toString();
                if (currentText.startsWith(kodeDivisi)) {
                    editTextNoNIP.setSelection(kodeDivisi.length() + String.valueOf(nextIdNumber).length());
                }
            }
        });
    }

    private void simpanNIP() {
        String noNIP = editTextNoNIP.getText().toString().trim();

        if (TextUtils.isEmpty(noNIP)) {
            editTextNoNIP.setError("No. NIP tidak boleh kosong");
            editTextNoNIP.requestFocus();
            return;
        }

        if (noNIP.length() < 3) {
            editTextNoNIP.setError("No. NIP minimal 3 digit");
            editTextNoNIP.requestFocus();
            return;
        }

        showLoading(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Buat request object dengan date_out
        NipRequest nipRequest = new NipRequest(noNIP, selectedDateOut);

        Call<BasicResponse> call = apiService.inputNIP(nipRequest);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        String successMessage = "NIP berhasil disimpan: " + noNIP;
                        if (selectedDateOut != null) {
                            successMessage += " dengan tanggal keluar: " + selectedDateOut;
                        }
                        Toast.makeText(InputNipActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                        // Reset form
                        editTextNoNIP.setText("");
                        selectedDateOut = null;
                        btnDateOut.setText("Masukkan Tanggal Keluar");
                        btnDateOut.setHint("Masukkan Tanggal Keluar");
                        editTextNoNIP.clearFocus();

                        // Load ulang ID untuk data berikutnya
                        loadLatestNipId();
                    } else {
                        Toast.makeText(InputNipActivity.this, "Gagal menyimpan NIP: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InputNipActivity.this, "Error response server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(InputNipActivity.this, "Gagal menyimpan NIP: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("InputNipActivity", "Save NIP error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSimpan.setText("Menyimpan...");
            btnSimpan.setEnabled(false);
            btnBatal.setEnabled(false);
            spinnerDivisi.setEnabled(false);
            editTextNoNIP.setEnabled(false);
            btnDateOut.setEnabled(false);
        } else {
            btnSimpan.setText("Simpan");
            btnSimpan.setEnabled(true);
            btnBatal.setEnabled(true);
            spinnerDivisi.setEnabled(true);
            editTextNoNIP.setEnabled(true);
            btnDateOut.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}