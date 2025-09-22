package com.example.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditDataPromoActivity extends AppCompatActivity {

    private EditText editTextNamaPromo, editTextPenginput;
    private Spinner spinnerReferensi;
    private Button btnSimpan, btnBatal, btnPilihGambar;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_USERNAME = "username";

    private int promoId;
    private String currentImageBase64;
    private static final int PICK_IMAGE_REQUEST = 100;
    private Bitmap selectedBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_data_promo);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Inisialisasi view
        initViews();

        // Terima data dari intent
        receiveIntentData();

        // Setup data otomatis
        setupAutoData();

        // Setup listeners
        setupListeners();
    }

    private void initViews() {
        editTextNamaPromo = findViewById(R.id.editTextNama);
        editTextPenginput = findViewById(R.id.editTextProspek);
        spinnerReferensi = findViewById(R.id.spinnerRole);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
        btnPilihGambar = findViewById(R.id.btnInputPromo);
    }

    private void receiveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            promoId = intent.getIntExtra("PROMO_ID", -1);
            String promoTitle = intent.getStringExtra("PROMO_TITLE");
            String promoInputter = intent.getStringExtra("PROMO_INPUTTER");
            String promoReference = intent.getStringExtra("PROMO_REFERENCE");
            currentImageBase64 = intent.getStringExtra("PROMO_IMAGE");

            // Set data ke form
            editTextNamaPromo.setText(promoTitle);
            editTextPenginput.setText(promoInputter);

            // Set spinner selection berdasarkan data yang diterima
            setSpinnerSelection(promoReference);

            Log.d("EditPromo", "Editing Promo ID: " + promoId);
        }
    }

    private void setupAutoData() {
        // Auto-fill username dari SharedPreferences (Remember Me)
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        if (!username.isEmpty()) {
            editTextPenginput.setText(username);
        }

        // Spinner sudah memiliki item dari XML, jadi tidak perlu setup lagi
        // Hanya set selection berdasarkan data yang ada
    }

    private void setSpinnerSelection(String reference) {
        if (reference != null && !reference.isEmpty()) {
            // Cari item yang sesuai dalam spinner
            for (int i = 0; i < spinnerReferensi.getCount(); i++) {
                String item = spinnerReferensi.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase(reference)) {
                    spinnerReferensi.setSelection(i);
                    Log.d("Spinner", "Set selection to: " + item + " at position: " + i);
                    return;
                }
            }

            // Jika referensi tidak ditemukan, set ke default atau tambahkan ke spinner
            Log.w("Spinner", "Referensi '" + reference + "' tidak ditemukan dalam spinner");
            // Optional: bisa tambahkan item baru ke spinner jika diperlukan
            // addItemToSpinner(reference);
        }
    }

    // Optional: Method untuk menambah item ke spinner jika diperlukan
    private void addItemToSpinner(String newItem) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerReferensi.getAdapter();
        adapter.add(newItem);
        adapter.notifyDataSetChanged();
        spinnerReferensi.setSelection(adapter.getPosition(newItem));
    }

    private void setupListeners() {
        btnSimpan.setOnClickListener(v -> {
            updatePromo();
        });

        btnBatal.setOnClickListener(v -> {
            finish();
        });

        btnPilihGambar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);

                // PERBAIKAN: Decode bitmap dengan options untuk avoid memory issues
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Reduce image size to avoid OOM
                selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options);

                if (selectedBitmap != null) {
                    // PERBAIKAN: Compress dengan quality yang sesuai
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Reduced quality
                    byte[] imageBytes = baos.toByteArray();
                    currentImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                    Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();

                    // PERBAIKAN: Clear memory
                    baos.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } else {
                    Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePromo() {
        String namaPromo = editTextNamaPromo.getText().toString().trim();
        String penginput = editTextPenginput.getText().toString().trim();
        String referensi = spinnerReferensi.getSelectedItem().toString();

        if (namaPromo.isEmpty() || penginput.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
            return;
        }

        // PERBAIKAN: Validasi spinner yang benar
        // Jangan validasi berdasarkan position, tapi berdasarkan value
        if (referensi.equals("Pilih Referensi Proyek") || referensi.isEmpty()) {
            Toast.makeText(this, "Harap pilih referensi proyek", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        showLoading(true);

        // Panggil API untuk update promo
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.updatePromo(
                promoId,
                namaPromo,
                penginput,
                referensi,
                currentImageBase64 // Gunakan gambar yang sama jika tidak diubah
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();

                    if (basicResponse.isSuccess()) {
                        Toast.makeText(EditDataPromoActivity.this, "Promo berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // PERBAIKAN: Kirim kembali data yang diupdate ke activity sebelumnya
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("UPDATED_PROMO_ID", promoId);
                        resultIntent.putExtra("UPDATED_IMAGE", currentImageBase64);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    } else {
                        Toast.makeText(EditDataPromoActivity.this, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditDataPromoActivity.this, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EditDataPromoActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnSimpan.setEnabled(false);
            btnSimpan.setText("Loading...");
        } else {
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Ubah");
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}