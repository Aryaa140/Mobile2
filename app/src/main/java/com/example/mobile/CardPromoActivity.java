package com.example.mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import android.util.Log;
public class CardPromoActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private MaterialToolbar topAppBar;
    private EditText editTextProspek, editTextNama;
    private Spinner spinnerRole;
    private Button btnInputPromo, btnSimpan, btnBatal;

    private DatabaseHelper databaseHelper;
    private FirebaseDatabaseHelper firebaseHelper;
    private Uri imageUri;

    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_promo);

        // Inisialisasi helpers
        databaseHelper = new DatabaseHelper(this);
        firebaseHelper = new FirebaseDatabaseHelper(this);

        // Dapatkan username dari SharedPreferences
        loggedInUsername = MainActivity.getLoggedInUser(this).getUsername();

        // Inisialisasi views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup form
        setupForm();

        // Setup buttons
        setupButtons();
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        editTextProspek = findViewById(R.id.editTextProspek);
        editTextNama = findViewById(R.id.editTextNama);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnInputPromo = findViewById(R.id.btnInputPromo);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal);
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupForm() {
        // Set username yang login secara otomatis
        editTextProspek.setText(loggedInUsername);
        editTextProspek.setEnabled(false); // Nonaktifkan edit

        // Load data proyek dari SQLite ke spinner
        loadProyekData();
    }

    private void loadProyekData() {
        List<DatabaseHelper.Proyek> proyekList = databaseHelper.getAllProyek();
        String[] proyekNames = new String[proyekList.size()];

        for (int i = 0; i < proyekList.size(); i++) {
            proyekNames[i] = proyekList.get(i).getNamaProyek();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                proyekNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void setupButtons() {
        btnInputPromo.setOnClickListener(v -> selectImage());

        btnSimpan.setOnClickListener(v -> savePromo());

        btnBatal.setOnClickListener(v -> finish());
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            btnInputPromo.setText("Gambar Terpilih");
            Toast.makeText(this, "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePromo() {
        String namaPenginput = editTextProspek.getText().toString().trim();
        String namaGambar = editTextNama.getText().toString().trim();
        String referensiProyek = spinnerRole.getSelectedItem().toString();

        // Validasi input
        if (namaGambar.isEmpty()) {
            editTextNama.setError("Nama promo harus diisi");
            editTextNama.requestFocus();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan loading
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        // Debug log
        Log.d("FirebaseDebug", "Attempting to save: " + namaGambar);
        Log.d("FirebaseDebug", "Image URI: " + imageUri.toString());

        // Simpan ke Firebase
        firebaseHelper.savePromoWithImage(
                imageUri,
                namaGambar,
                referensiProyek,
                namaPenginput,
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String promoId) {
                        runOnUiThread(() -> {
                            btnSimpan.setEnabled(true);
                            btnSimpan.setText("Simpan");
                            Toast.makeText(CardPromoActivity.this, "Promo berhasil disimpan", Toast.LENGTH_SHORT).show();
                            Log.d("FirebaseDebug", "Successfully saved with ID: " + promoId);
                            finish();
                        });
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            btnSimpan.setEnabled(true);
                            btnSimpan.setText("Simpan");
                            String errorMsg = "Gagal menyimpan: " + e.getMessage();
                            Toast.makeText(CardPromoActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            Log.e("FirebaseError", "Save failed: " + e.getMessage(), e);

                            // Tampilkan detail error lebih spesifik
                            if (e.getMessage().contains("object does not exist")) {
                                Toast.makeText(CardPromoActivity.this,
                                        "Error: Firebase tidak terhubung. Periksa koneksi internet dan konfigurasi Firebase",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}