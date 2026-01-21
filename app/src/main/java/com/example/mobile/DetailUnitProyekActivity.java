package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailUnitProyekActivity extends AppCompatActivity {

    private static final String TAG = "DetailUnitProyek";
    private static final int PICK_IMAGE_UNIT = 1;
    private static final int PICK_IMAGE_DENAH = 2;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    // Views untuk Card 1 - Detail Unit
    private ImageView imgUnit, imgDenah;
    private TextView txtNamaHunian, txtNamaProyek, txtLuasTanah, txtLuasBangunan, txtDeskripsi;
    private TextView txtNoFasilitas;
    private LinearLayout containerFasilitas;
    private Button btnTambahFasilitas;

    // EditText untuk mode edit
    private EditText editNamaHunian, editLuasTanah, editLuasBangunan, editDeskripsi;

    // Data
    private Hunian currentHunian;
    private ApiService apiService;
    private List<FasilitasHunianItem> fasilitasList;

    // Edit mode variables
    private boolean isEditMode = false;
    private MenuItem menuEdit, menuSave, menuCancel;

    // Selected images
    private Bitmap selectedUnitBitmap = null;
    private Bitmap selectedDenahBitmap = null;

    // User level
    private String userLevel;
    private SharedPreferences sharedPreferences;

    // TAMBAHKAN VARIABEL UNTUK TEMPORARY DATA
    private List<FasilitasHunianItem> tempFasilitasList = new ArrayList<>();
    private List<FasilitasOperation> pendingOperations = new ArrayList<>();
    private boolean hasUnsavedChanges = false;
    private int tempIdCounter = -1; // Untuk generate ID sementara
    private GridLayout gridLayoutRead, gridLayoutEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_unit_proyek);

        Log.d(TAG, "Activity onCreate started");

        // Ambil data hunian dari intent
        currentHunian = (Hunian) getIntent().getSerializableExtra("HUNIAN");
        if (currentHunian == null) {
            Toast.makeText(this, "Data hunian tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Menerima hunian: " + currentHunian.getNamaHunian() + " dari proyek: " + currentHunian.getNamaProyek());

        // Ambil user level dari SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        userLevel = sharedPreferences.getString("level", "Operator");
        Log.d(TAG, "User Level: " + userLevel);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi list
        fasilitasList = new ArrayList<>();

        initViews();
        setupTopAppBar();
        loadHunianData();
        loadFasilitasData();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            checkForChanges();
        }
    };
    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Card 1 - Detail Unit - TextView (mode baca)
        imgUnit = findViewById(R.id.imgUnit);
        imgDenah = findViewById(R.id.imgDenah);
        txtNamaHunian = findViewById(R.id.txtNamaHunian);
        txtNamaProyek = findViewById(R.id.txtNamaProyek);
        txtLuasTanah = findViewById(R.id.txtLuasTanah);
        txtLuasBangunan = findViewById(R.id.txtLuasBangunan);
        txtDeskripsi = findViewById(R.id.txtDeskripsi);

        // Fasilitas views
        containerFasilitas = findViewById(R.id.containerFasilitas);
        txtNoFasilitas = findViewById(R.id.txtNoFasilitas);
        btnTambahFasilitas = findViewById(R.id.btnTambahFasilitas);

        // EditText (mode edit)
        editNamaHunian = findViewById(R.id.editNamaHunian);
        editLuasTanah = findViewById(R.id.editLuasTanah);
        editLuasBangunan = findViewById(R.id.editLuasBangunan);
        editDeskripsi = findViewById(R.id.editDeskripsi);
        setupTextWatchers();
        // Setup click listeners untuk gambar dan tombol
        setupImageClickListeners();
        setupButtonListeners();
        // TAMBAHKAN INISIALISASI GRID LAYOUT
        gridLayoutRead = findViewById(R.id.gridLayoutRead);
        gridLayoutEdit = findViewById(R.id.gridLayoutEdit);
        // Pastikan mode baca aktif saat inisialisasi
        setEditMode(false);
    }
    private void setupTextWatchers() {
        editNamaHunian.addTextChangedListener(textWatcher);
        editLuasTanah.addTextChangedListener(textWatcher);
        editLuasBangunan.addTextChangedListener(textWatcher);
        editDeskripsi.addTextChangedListener(textWatcher);
    }

    private void setupButtonListeners() {
        btnTambahFasilitas.setOnClickListener(v -> {
            if (isEditMode && "Admin".equals(userLevel)) {
                Intent intent = new Intent(DetailUnitProyekActivity.this, TambahFasilitasHunianActivity.class);
                intent.putExtra("NAMA_HUNIAN", currentHunian.getNamaHunian());
                startActivityForResult(intent, 100);
            }
        });
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;

        if (editMode) {
            // Mode edit - tampilkan EditText, sembunyikan TextView
            txtNamaHunian.setVisibility(View.GONE);
            txtDeskripsi.setVisibility(View.GONE);

            // PERBAIKAN: Atur visibilitas GridLayout
            gridLayoutRead.setVisibility(View.GONE);
            gridLayoutEdit.setVisibility(View.VISIBLE);

            editNamaHunian.setVisibility(View.VISIBLE);
            editDeskripsi.setVisibility(View.VISIBLE);

            // Tampilkan tombol tambah fasilitas
            btnTambahFasilitas.setVisibility(View.VISIBLE);

        } else {
            // Mode baca - tampilkan TextView, sembunyikan EditText
            txtNamaHunian.setVisibility(View.VISIBLE);
            txtDeskripsi.setVisibility(View.VISIBLE);

            // PERBAIKAN: Atur visibilitas GridLayout
            gridLayoutRead.setVisibility(View.VISIBLE);
            gridLayoutEdit.setVisibility(View.GONE);

            editNamaHunian.setVisibility(View.GONE);
            editDeskripsi.setVisibility(View.GONE);

            // Sembunyikan tombol tambah fasilitas
            btnTambahFasilitas.setVisibility(View.GONE);
        }

        // Refresh tampilan fasilitas
        displayFasilitasData(tempFasilitasList);
    }


    private void checkForChanges() {
        if (!isEditMode) return;

        boolean hasTextChanges = !editNamaHunian.getText().toString().equals(currentHunian.getNamaHunian()) ||
                !editLuasTanah.getText().toString().equals(String.valueOf(currentHunian.getLuasTanah())) ||
                !editLuasBangunan.getText().toString().equals(String.valueOf(currentHunian.getLuasBangunan())) ||
                !editDeskripsi.getText().toString().equals(currentHunian.getDeskripsiHunian() != null ? currentHunian.getDeskripsiHunian() : "");

        boolean hasImageChanges = selectedUnitBitmap != null || selectedDenahBitmap != null;
        boolean hasFacilityChanges = !pendingOperations.isEmpty();

        if (hasTextChanges || hasImageChanges || hasFacilityChanges) {
            markHasUnsavedChanges();
        }
    }

    private void setupTopAppBar() {
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail " + currentHunian.getNamaHunian());
        }

        topAppBar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_unit, menu);

        menuEdit = menu.findItem(R.id.action_edit);
        menuSave = menu.findItem(R.id.action_save);
        menuCancel = menu.findItem(R.id.action_cancel);

        // Sembunyikan menu edit jika user bukan Admin
        if (!"Admin".equals(userLevel)) {
            menuEdit.setVisible(false);
        }

        // Initially hide save and cancel buttons
        updateMenuVisibility();

        return true;
    }

    private void updateMenuVisibility() {
        if (menuEdit != null) {
            menuEdit.setVisible(!isEditMode && "Admin".equals(userLevel));
        }
        if (menuSave != null) {
            menuSave.setVisible(isEditMode);
            // Enable/disable save button based on unsaved changes
            menuSave.setEnabled(hasUnsavedChanges);
        }
        if (menuCancel != null) menuCancel.setVisible(isEditMode);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            if (!"Admin".equals(userLevel)) {
                Toast.makeText(this, "Hanya Admin yang dapat mengedit", Toast.LENGTH_SHORT).show();
                return true;
            }
            enterEditMode();
            return true;
        } else if (id == R.id.action_save) {
            saveAllChangesToServer();
            return true;
        } else if (id == R.id.action_cancel) {
            exitEditMode();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enterEditMode() {
        if (!"Admin".equals(userLevel)) {
            Toast.makeText(this, "Hanya Admin yang dapat mengedit", Toast.LENGTH_SHORT).show();
            return;
        }

        isEditMode = true;

        // Set edit mode views
        setEditMode(true);

        // Populate edit fields with current data
        editNamaHunian.setText(currentHunian.getNamaHunian());

        // PERBAIKAN: Set nilai langsung tanpa ekstraksi
        editLuasTanah.setText(String.valueOf(currentHunian.getLuasTanah()));
        editLuasBangunan.setText(String.valueOf(currentHunian.getLuasBangunan()));

        if (currentHunian.getDeskripsiHunian() != null && !currentHunian.getDeskripsiHunian().isEmpty()) {
            editDeskripsi.setText(currentHunian.getDeskripsiHunian());
        } else {
            editDeskripsi.setText("");
        }

        // Reset status perubahan
        hasUnsavedChanges = false;
        updateMenuVisibility();

        // Enable image interaction
        imgUnit.setClickable(true);
        imgDenah.setClickable(true);

        // Add edit indicator to images
        imgUnit.setAlpha(0.8f);
        imgDenah.setAlpha(0.8f);

        Toast.makeText(this, "Mode Edit: Klik gambar untuk mengubah", Toast.LENGTH_LONG).show();
    }
    private void exitEditMode() {
        Log.d(TAG, "Exiting edit mode and ensuring UI is updated");

        // Reset temporary data jika ada perubahan yang belum disimpan
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(this)
                    .setTitle("Perubahan Belum Disimpan")
                    .setMessage("Ada perubahan yang belum disimpan. Yakin ingin membatalkan?")
                    .setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                        resetTemporaryData();
                        completeExitEditMode();
                    })
                    .setNegativeButton("Lanjut Edit", null)
                    .show();
        } else {
            resetTemporaryData();
            completeExitEditMode();
        }
    }

    private void resetTemporaryData() {
        pendingOperations.clear();
        hasUnsavedChanges = false;
        tempFasilitasList = new ArrayList<>(fasilitasList);
        tempIdCounter = -1;
    }

    private void completeExitEditMode() {
        isEditMode = false;

        // Set read-only mode views
        setEditMode(false);

        // Update menu visibility
        updateMenuVisibility();

        // Reset selected images
        selectedUnitBitmap = null;
        selectedDenahBitmap = null;

        // Disable image interaction
        imgUnit.setClickable(false);
        imgDenah.setClickable(false);

        // Remove edit indicator from images
        imgUnit.setAlpha(1.0f);
        imgDenah.setAlpha(1.0f);

        // Load data asli dari server
        loadHunianData();
        loadFasilitasData();

        Log.d(TAG, "Edit mode exited and UI should be updated");
    }

    private void loadHunianData() {
        Log.d(TAG, "Loading hunian data to UI");

        if (currentHunian == null) {
            Log.e(TAG, "currentHunian is null!");
            return;
        }

        runOnUiThread(() -> {
            try {
                // Tampilkan data hunian
                txtNamaHunian.setText(currentHunian.getNamaHunian());
                txtNamaProyek.setText(currentHunian.getNamaProyek());

                // PERBAIKAN: Format yang benar untuk TextView
                txtLuasTanah.setText("Luas Tanah: " + currentHunian.getLuasTanah() + " m²");
                txtLuasBangunan.setText("Luas Bangunan: " + currentHunian.getLuasBangunan() + " m²");

                if (currentHunian.getDeskripsiHunian() != null && !currentHunian.getDeskripsiHunian().isEmpty()) {
                    txtDeskripsi.setText(currentHunian.getDeskripsiHunian());
                } else {
                    txtDeskripsi.setText("Tidak ada deskripsi");
                }

                // Set gambar unit
                if (currentHunian.getGambarUnit() != null && !currentHunian.getGambarUnit().isEmpty()) {
                    setImageFromBase64(currentHunian.getGambarUnit(), imgUnit);
                } else {
                    imgUnit.setImageResource(R.drawable.ic_placeholder);
                }

                // Set gambar denah
                if (currentHunian.getGambarDenah() != null && !currentHunian.getGambarDenah().isEmpty()) {
                    setImageFromBase64(currentHunian.getGambarDenah(), imgDenah);
                } else {
                    imgDenah.setImageResource(R.drawable.ic_placeholder);
                }

                Log.d(TAG, "UI successfully updated with hunian data");
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage());
            }
        });
    }

    private void setupImageClickListeners() {
        // Image Unit - bisa diklik untuk edit gambar
        imgUnit.setOnClickListener(v -> {
            if (isEditMode && "Admin".equals(userLevel)) {
                openImagePicker(PICK_IMAGE_UNIT);
            }
        });

        // Image Denah - bisa diklik untuk edit gambar
        imgDenah.setOnClickListener(v -> {
            if (isEditMode && "Admin".equals(userLevel)) {
                openImagePicker(PICK_IMAGE_DENAH);
            }
        });
    }

    private void loadFasilitasData() {
        Log.d(TAG, "Memuat data fasilitas untuk hunian: " + currentHunian.getNamaHunian());

        Call<FasilitasHunianResponse> call = apiService.getFasilitasByHunian(
                "getFasilitasByHunian",
                currentHunian.getNamaHunian()
        );

        call.enqueue(new Callback<FasilitasHunianResponse>() {
            @Override
            public void onResponse(Call<FasilitasHunianResponse> call, Response<FasilitasHunianResponse> response) {
                Log.d(TAG, "Facilities API response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    FasilitasHunianResponse fasilitasResponse = response.body();
                    if (fasilitasResponse.isSuccess()) {
                        fasilitasList = fasilitasResponse.getData();
                        tempFasilitasList = new ArrayList<>(fasilitasList); // Salin ke temporary
                        displayFasilitasData(tempFasilitasList);
                        Log.d(TAG, "Data fasilitas berhasil dimuat: " + (fasilitasList != null ? fasilitasList.size() : 0) + " items");
                    } else {
                        Log.e(TAG, "Gagal memuat fasilitas: " + fasilitasResponse.getMessage());
                        showNoFasilitasMessage();
                    }
                } else {
                    Log.e(TAG, "Response tidak sukses: " + response.code());
                    showNoFasilitasMessage();
                }
            }

            @Override
            public void onFailure(Call<FasilitasHunianResponse> call, Throwable t) {
                Log.e(TAG, "Gagal memuat data fasilitas: " + t.getMessage());
                showNoFasilitasMessage();
            }
        });
    }

    private void displayFasilitasData(List<FasilitasHunianItem> fasilitasList) {
        runOnUiThread(() -> {
            containerFasilitas.removeAllViews();

            if (fasilitasList == null || fasilitasList.isEmpty()) {
                showNoFasilitasMessage();
                return;
            }

            Log.d(TAG, "Menampilkan " + fasilitasList.size() + " fasilitas");
            txtNoFasilitas.setVisibility(View.GONE);

            for (FasilitasHunianItem fasilitas : fasilitasList) {
                addFasilitasToView(fasilitas);
            }
        });
    }

    private void addFasilitasToView(FasilitasHunianItem fasilitas) {
        // Buat CardView untuk setiap fasilitas
        CardView cardFasilitas = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 8);
        cardFasilitas.setLayoutParams(cardParams);
        cardFasilitas.setCardElevation(4);
        cardFasilitas.setRadius(12);
        cardFasilitas.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardFasilitas.setContentPadding(16, 16, 16, 16);

        // Buat LinearLayout untuk konten
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);

        // TextView untuk nama dan jumlah fasilitas
        TextView textFasilitas = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textFasilitas.setLayoutParams(textParams);
        textFasilitas.setText(fasilitas.getNamaFasilitas() + " (" + fasilitas.getJumlah() + ")");
        textFasilitas.setTextColor(getResources().getColor(android.R.color.black));
        textFasilitas.setTextSize(16);

        contentLayout.addView(textFasilitas);

        // Container untuk tombol aksi
        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Tombol edit (tampil di mode edit untuk Admin)
        if (isEditMode && "Admin".equals(userLevel)) {
            MaterialButton btnEdit = new MaterialButton(this);
            LinearLayout.LayoutParams btnEditParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnEditParams.setMargins(0, 0, 8, 0);
            btnEdit.setLayoutParams(btnEditParams);
            btnEdit.setText("Edit");
            btnEdit.setBackgroundColor(getResources().getColor(R.color.teal_700));
            btnEdit.setTextColor(getResources().getColor(android.R.color.white));
            btnEdit.setCornerRadius(20);
            btnEdit.setPadding(16, 8, 16, 8);

            btnEdit.setOnClickListener(v -> {
                editFasilitas(fasilitas);
            });

            actionLayout.addView(btnEdit);
        }

        // Tombol hapus (hanya tampil di mode edit untuk Admin)
        if (isEditMode && "Admin".equals(userLevel)) {
            MaterialButton btnHapus = new MaterialButton(this);
            LinearLayout.LayoutParams btnHapusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnHapus.setLayoutParams(btnHapusParams);
            btnHapus.setText("Hapus");
            btnHapus.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            btnHapus.setTextColor(getResources().getColor(android.R.color.white));
            btnHapus.setCornerRadius(20);
            btnHapus.setPadding(16, 8, 16, 8);

            btnHapus.setOnClickListener(v -> {
                showDeleteFasilitasDialog(fasilitas);
            });

            actionLayout.addView(btnHapus);
        }

        contentLayout.addView(actionLayout);
        cardFasilitas.addView(contentLayout);
        containerFasilitas.addView(cardFasilitas);
    }

    private void editFasilitas(FasilitasHunianItem fasilitas) {
        Intent intent = new Intent(DetailUnitProyekActivity.this, EditFasilitasHunianActivity.class);
        intent.putExtra("NAMA_HUNIAN", currentHunian.getNamaHunian());
        intent.putExtra("FASILITAS_DATA", fasilitas);
        startActivityForResult(intent, 101);
    }

    private void showDeleteFasilitasDialog(FasilitasHunianItem fasilitas) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Fasilitas")
                .setMessage("Apakah Anda yakin ingin menghapus fasilitas \"" + fasilitas.getNamaFasilitas() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteFasilitas(fasilitas);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // TAMBAHKAN METHOD UNTUK TEMPORARY DELETE
    private void deleteFasilitas(FasilitasHunianItem fasilitas) {
        Log.d(TAG, "Menghapus fasilitas sementara: " + fasilitas.getNamaFasilitas());

        // Tambahkan ke pending operations
        pendingOperations.add(new FasilitasOperation(FasilitasOperation.OPERATION_DELETE, fasilitas));

        // Apply changes temporarily
        applyPendingOperations();
        markHasUnsavedChanges();

        Toast.makeText(this, "Fasilitas dihapus sementara. Tekan SIMPAN untuk menyimpan permanen.", Toast.LENGTH_SHORT).show();
    }

    // TAMBAHKAN METHOD UNTUK APPLY PENDING OPERATIONS
    private void applyPendingOperations() {
        // Reset temp list ke data asli
        tempFasilitasList = new ArrayList<>(fasilitasList);

        // Apply semua operasi yang tertunda
        for (FasilitasOperation operation : pendingOperations) {
            switch (operation.getOperationType()) {
                case FasilitasOperation.OPERATION_ADD:
                    tempFasilitasList.add(operation.getFasilitas());
                    break;
                case FasilitasOperation.OPERATION_UPDATE:
                    // Ganti item lama dengan yang baru
                    for (int i = 0; i < tempFasilitasList.size(); i++) {
                        if (tempFasilitasList.get(i).getIdFasilitas() == operation.getFasilitas().getIdFasilitas()) {
                            tempFasilitasList.set(i, operation.getFasilitas());
                            break;
                        }
                    }
                    break;
                case FasilitasOperation.OPERATION_DELETE:
                    // Hapus dari temporary list
                    tempFasilitasList.removeIf(item ->
                            item.getIdFasilitas() == operation.getFasilitas().getIdFasilitas());
                    break;
            }
        }
        displayFasilitasData(tempFasilitasList);
    }

    // TAMBAHKAN METHOD UNTUK MARK UNSAVED CHANGES
    private void markHasUnsavedChanges() {
        hasUnsavedChanges = true;
        if (menuSave != null) {
            menuSave.setEnabled(true);
        }
    }

    private void showNoFasilitasMessage() {
        runOnUiThread(() -> {
            txtNoFasilitas.setVisibility(View.VISIBLE);
            containerFasilitas.removeAllViews();
        });
    }

    private void setImageFromBase64(String base64String, ImageView imageView) {
        try {
            String cleanBase64 = base64String.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            cleanBase64 = cleanBase64.replaceAll("\\s", "");

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decoding image: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_UNIT || requestCode == PICK_IMAGE_DENAH) {
                // Handle image picker
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                    if (requestCode == PICK_IMAGE_UNIT) {
                        selectedUnitBitmap = bitmap;
                        imgUnit.setImageBitmap(bitmap);
                        markHasUnsavedChanges();
                        Toast.makeText(this, "Gambar unit berhasil diubah", Toast.LENGTH_SHORT).show();
                    } else if (requestCode == PICK_IMAGE_DENAH) {
                        selectedDenahBitmap = bitmap;
                        imgDenah.setImageBitmap(bitmap);
                        markHasUnsavedChanges();
                        Toast.makeText(this, "Gambar denah berhasil diubah", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error loading image: " + e.getMessage());
                    Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
                }
            }
            else if (requestCode == 100) {
                // Handle hasil dari TambahFasilitasHunianActivity - TAMBAH SEMENTARA
                FasilitasHunianItem newFasilitas = (FasilitasHunianItem) data.getSerializableExtra("NEW_FASILITAS");
                if (newFasilitas != null) {
                    // Generate ID sementara untuk item baru
                    newFasilitas.setIdFasilitas(tempIdCounter--);
                    pendingOperations.add(new FasilitasOperation(FasilitasOperation.OPERATION_ADD, newFasilitas));
                    applyPendingOperations();
                    markHasUnsavedChanges();
                    Toast.makeText(this, "Fasilitas ditambahkan sementara. Tekan SIMPAN untuk menyimpan permanen.", Toast.LENGTH_SHORT).show();
                }
            }
            else if (requestCode == 101) {
                // Handle hasil dari EditFasilitasHunianActivity - UPDATE SEMENTARA
                FasilitasHunianItem updatedFasilitas = (FasilitasHunianItem) data.getSerializableExtra("UPDATED_FASILITAS");
                FasilitasHunianItem oldFasilitas = (FasilitasHunianItem) data.getSerializableExtra("OLD_FASILITAS");

                if (updatedFasilitas != null) {
                    pendingOperations.add(new FasilitasOperation(
                            FasilitasOperation.OPERATION_UPDATE,
                            updatedFasilitas,
                            oldFasilitas
                    ));
                    applyPendingOperations();
                    markHasUnsavedChanges();
                    Toast.makeText(this, "Fasilitas diupdate sementara. Tekan SIMPAN untuk menyimpan permanen.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // MODIFIKASI METHOD SAVE ALL CHANGES
    private void saveAllChangesToServer() {
        // Simpan perubahan hunian dulu
        saveHunianChanges();
    }

    private void sendNewsActivityBroadcast() {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "HUNIAN_UPDATED");
            broadcastIntent.putExtra("TYPE", "hunian");
            broadcastIntent.putExtra("NAMA_HUNIAN", currentHunian.getNamaHunian());
            broadcastIntent.putExtra("NAMA_PROYEK", currentHunian.getNamaProyek());

            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "");
            String namaLengkap = prefs.getString("nama_lengkap", username);
            broadcastIntent.putExtra("PENGINPUT", namaLengkap);

            sendBroadcast(broadcastIntent);
            Log.d(TAG, "📢 Broadcast sent to refresh NewsActivity");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending broadcast: " + e.getMessage());
        }
    }

    private void saveHunianChanges() {
        String newNamaHunian = editNamaHunian.getText().toString().trim();
        String newLuasTanahStr = editLuasTanah.getText().toString().trim();
        String newLuasBangunanStr = editLuasBangunan.getText().toString().trim();
        String newDeskripsi = editDeskripsi.getText().toString().trim();

        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String updatedBy = sharedPreferences.getString("nama_lengkap", username);

        // Validasi
        if (newNamaHunian.isEmpty()) {
            Toast.makeText(this, "Nama hunian tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newLuasTanahStr.isEmpty()) {
            Toast.makeText(this, "Luas tanah tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newLuasBangunanStr.isEmpty()) {
            Toast.makeText(this, "Luas bangunan tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validasi numeric
        int newLuasTanah;
        int newLuasBangunan;
        try {
            newLuasTanah = Integer.parseInt(newLuasTanahStr);
            newLuasBangunan = Integer.parseInt(newLuasBangunanStr);

            if (newLuasTanah <= 0 || newLuasBangunan <= 0) {
                Toast.makeText(this, "Luas tanah dan bangunan harus lebih dari 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Luas tanah dan bangunan harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Menyimpan perubahan hunian...", Toast.LENGTH_SHORT).show();

        // ✅ PERBAIKAN: Jangan kirim 'null' string, kirim string kosong jika tidak ada perubahan gambar
        String gambarUnitBase64 = "";
        String gambarDenahBase64 = "";

        if (selectedUnitBitmap != null) {
            gambarUnitBase64 = bitmapToBase64(selectedUnitBitmap);
            Log.d(TAG, "✅ Gambar unit akan diupdate, size: " + gambarUnitBase64.length());
        } else {
            Log.d(TAG, "🔄 Gambar unit tidak diubah, mengirim string kosong");
        }

        if (selectedDenahBitmap != null) {
            gambarDenahBase64 = bitmapToBase64(selectedDenahBitmap);
            Log.d(TAG, "✅ Gambar denah akan diupdate, size: " + gambarDenahBase64.length());
        } else {
            Log.d(TAG, "🔄 Gambar denah tidak diubah, mengirim string kosong");
        }

        final int hunianId = currentHunian.getIdHunian();

        Log.d(TAG, "📡 Mengirim request update hunian ke server...");
        Log.d(TAG, "📊 Data: Nama=" + newNamaHunian +
                ", LuasTanah=" + newLuasTanah +
                ", LuasBangunan=" + newLuasBangunan +
                ", UpdatedBy=" + updatedBy +
                ", Username=" + username +
                ", GambarUnit=" + (gambarUnitBase64.isEmpty() ? "empty" : "base64") +
                ", GambarDenah=" + (gambarDenahBase64.isEmpty() ? "empty" : "base64"));

        Log.d(TAG, "📡 Mengirim request update hunian ke server...");
        Log.d(TAG, "📊 Data: Nama=" + newNamaHunian +
                ", LuasTanah=" + newLuasTanah +
                ", LuasBangunan=" + newLuasBangunan +
                ", UpdatedBy=" + updatedBy +
                ", Username=" + username);

        Call<BasicResponse> call = apiService.updateHunianComprehensive(
                "updateHunian",
                hunianId,
                currentHunian.getNamaHunian(),
                newNamaHunian,
                newLuasTanah,
                newLuasBangunan,
                newDeskripsi,
                gambarUnitBase64,
                gambarDenahBase64,
                username,      // ✅ PARAMETER KE-9: username
                updatedBy      // ✅ PARAMETER KE-10: updated_by
        );

        String finalGambarUnitBase6 = gambarUnitBase64;
        String finalGambarDenahBase6 = gambarDenahBase64;
        String finalGambarUnitBase7 = gambarUnitBase64;
        String finalGambarDenahBase7 = gambarDenahBase64;
        String finalGambarUnitBase8 = gambarUnitBase64;
        String finalGambarDenahBase8 = gambarDenahBase64;
        String finalGambarUnitBase9 = gambarUnitBase64;
        String finalGambarDenahBase9 = gambarDenahBase64;
        String finalGambarUnitBase10 = gambarUnitBase64;
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    try {
                        if (response.body() != null) {
                            BasicResponse updateResponse = response.body();
                            Log.d(TAG, "✅ Update response: " + updateResponse.isSuccess());
                            Log.d(TAG, "📝 Update message: " + updateResponse.getMessage());

                            if (updateResponse.isSuccess()) {
                                // ✅ PERBAIKAN: Update data lokal SEBELUM UI
                                updateCurrentHunianData(newNamaHunian, newLuasTanah, newLuasBangunan,
                                        newDeskripsi,
                                        finalGambarUnitBase9.isEmpty() ? currentHunian.getGambarUnit() : finalGambarUnitBase9,
                                        finalGambarDenahBase9.isEmpty() ? currentHunian.getGambarDenah() : finalGambarDenahBase9);

                                // ✅ PERBAIKAN: Update UI di main thread
                                runOnUiThread(() -> {
                                    // 1. Update toolbar title
                                    if (topAppBar != null) {
                                        topAppBar.setTitle("Detail " + newNamaHunian);
                                    }

                                    // 2. Update TextView data
                                    txtNamaHunian.setText(newNamaHunian);
                                    txtLuasTanah.setText("Luas Tanah: " + newLuasTanah + " m²");
                                    txtLuasBangunan.setText("Luas Bangunan: " + newLuasBangunan + " m²");
                                    txtDeskripsi.setText(newDeskripsi.isEmpty() ? "Tidak ada deskripsi" : newDeskripsi);

                                    // 3. Update gambar jika ada
                                    if (!finalGambarUnitBase9.isEmpty()) {
                                        setImageFromBase64(finalGambarUnitBase9, imgUnit);
                                    }
                                    if (!finalGambarDenahBase9.isEmpty()) {
                                        setImageFromBase64(finalGambarDenahBase9, imgDenah);
                                    }

                                    Toast.makeText(DetailUnitProyekActivity.this,
                                            "Data hunian berhasil disimpan",
                                            Toast.LENGTH_SHORT).show();
                                });

                                saveHunianUpdateHistori(
                                        hunianId,
                                        newNamaHunian,
                                        currentHunian.getNamaProyek(),
                                        newLuasTanah,
                                        newLuasBangunan,
                                        updatedBy,
                                        !finalGambarUnitBase10.isEmpty() ? finalGambarUnitBase10 : null
                                );

                                sendNewsActivityBroadcast();

                                // ✅ PERBAIKAN: Kirim broadcast untuk refresh NewsActivity
                                sendUpdateBroadcast(newNamaHunian, currentHunian.getNamaProyek());

                                // Simpan perubahan fasilitas jika ada
                                if (!pendingOperations.isEmpty()) {
                                    saveAllFasilitasChanges();
                                } else {
                                    exitEditModeAfterSave();
                                }

                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(DetailUnitProyekActivity.this,
                                            "Gagal: " + updateResponse.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                                Log.e(TAG, "❌ Update response failed: " + updateResponse.getMessage());
                            }
                        } else if (response.code() == 500) {
                            // HTTP 500: Server error
                            Log.e(TAG, "❌ HTTP 500 Error, but data might be saved");

                            // ✅ PERBAIKAN: Tetap update UI meskipun HTTP 500
                            runOnUiThread(() -> {
                                // Update UI dengan data baru
                                txtNamaHunian.setText(newNamaHunian);
                                txtLuasTanah.setText("Luas Tanah: " + newLuasTanah + " m²");
                                txtLuasBangunan.setText("Luas Bangunan: " + newLuasBangunan + " m²");
                                txtDeskripsi.setText(newDeskripsi.isEmpty() ? "Tidak ada deskripsi" : newDeskripsi);

                                // Update data lokal
                                updateCurrentHunianData(newNamaHunian, newLuasTanah, newLuasBangunan,
                                        newDeskripsi,
                                        finalGambarUnitBase9.isEmpty() ? currentHunian.getGambarUnit() : finalGambarUnitBase9,
                                        finalGambarDenahBase9.isEmpty() ? currentHunian.getGambarDenah() : finalGambarDenahBase9);

                                Toast.makeText(DetailUnitProyekActivity.this,
                                        "✅ Data hunian berhasil diperbarui (meskipun ada error server)",
                                        Toast.LENGTH_SHORT).show();

                                // Kirim broadcast untuk refresh NewsActivity
                                sendUpdateBroadcast(newNamaHunian, currentHunian.getNamaProyek());

                                // Keluar dari mode edit
                                exitEditModeAfterSave();
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing response: " + e.getMessage());
                    }
                } else {
                    // Handle other HTTP errors
                    String errorMsg = "HTTP Error: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "❌ Error reading error body", e);
                        }
                    }

                    final String finalErrorMsg = errorMsg;
                    runOnUiThread(() -> {
                        Toast.makeText(DetailUnitProyekActivity.this,
                                "Server error: " + finalErrorMsg,
                                Toast.LENGTH_LONG).show();
                    });
                    Log.e(TAG, "❌ HTTP Error: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error: " + t.getMessage(), t);
                runOnUiThread(() -> {
                    Toast.makeText(DetailUnitProyekActivity.this,
                            "Gagal terhubung ke server: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ✅ METHOD BARU: Kirim broadcast untuk refresh NewsActivity
    private void sendUpdateBroadcast(String namaHunian, String namaProyek) {
        try {
            // Dapatkan informasi penginput
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "");
            String namaLengkap = prefs.getString("nama_lengkap", username);

            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "HUNIAN_UPDATED");
            broadcastIntent.putExtra("TYPE", "hunian");
            broadcastIntent.putExtra("NAMA_HUNIAN", namaHunian);
            broadcastIntent.putExtra("NAMA_PROYEK", namaProyek);
            broadcastIntent.putExtra("PENGINPUT", namaLengkap);

            sendBroadcast(broadcastIntent);
            Log.d(TAG, "📢 Broadcast sent for hunian update: " + namaHunian);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending broadcast: " + e.getMessage());
        }
    }

    // TAMBAHKAN METHOD UNTUK SAVE FASILITAS CHANGES
    private void saveAllFasilitasChanges() {
        if (pendingOperations.isEmpty()) {
            exitEditModeAfterSave();
            return;
        }

        Toast.makeText(this, "Menyimpan perubahan fasilitas...", Toast.LENGTH_SHORT).show();

        final int totalOperations = pendingOperations.size();
        final AtomicInteger completedOperations = new AtomicInteger(0);
        final AtomicInteger successCount = new AtomicInteger(0);

        for (FasilitasOperation operation : pendingOperations) {
            switch (operation.getOperationType()) {
                case FasilitasOperation.OPERATION_ADD:
                    Call<BasicResponse> addCall = apiService.addFasilitasHunian(
                            "addFasilitas",
                            currentHunian.getNamaHunian(),
                            operation.getFasilitas().getNamaFasilitas(),
                            operation.getFasilitas().getJumlah()
                    );

                    addCall.enqueue(new Callback<BasicResponse>() {
                        @Override
                        public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                            handleOperationResponse(response, completedOperations, successCount, totalOperations, "ditambahkan");
                        }

                        @Override
                        public void onFailure(Call<BasicResponse> call, Throwable t) {
                            handleOperationFailure(completedOperations, totalOperations, "ditambahkan", t.getMessage());
                        }
                    });
                    break;

                case FasilitasOperation.OPERATION_UPDATE:
                    Call<BasicResponse> updateCall = apiService.updateFasilitasHunian(
                            "updateFasilitas",
                            operation.getFasilitas().getIdFasilitas(),
                            operation.getFasilitas().getNamaFasilitas(),
                            operation.getFasilitas().getJumlah()
                    );

                    updateCall.enqueue(new Callback<BasicResponse>() {
                        @Override
                        public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                            handleOperationResponse(response, completedOperations, successCount, totalOperations, "diupdate");
                        }

                        @Override
                        public void onFailure(Call<BasicResponse> call, Throwable t) {
                            handleOperationFailure(completedOperations, totalOperations, "diupdate", t.getMessage());
                        }
                    });
                    break;

                case FasilitasOperation.OPERATION_DELETE:
                    Call<BasicResponse> deleteCall = apiService.deleteFasilitasHunian(
                            "deleteFasilitas",
                            operation.getFasilitas().getIdFasilitas()
                    );

                    deleteCall.enqueue(new Callback<BasicResponse>() {
                        @Override
                        public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                            handleOperationResponse(response, completedOperations, successCount, totalOperations, "dihapus");
                        }

                        @Override
                        public void onFailure(Call<BasicResponse> call, Throwable t) {
                            handleOperationFailure(completedOperations, totalOperations, "dihapus", t.getMessage());
                        }
                    });
                    break;
            }
        }
    }

    private void handleOperationResponse(Response<BasicResponse> response, AtomicInteger completedOperations,
                                         AtomicInteger successCount, int totalOperations, String operationType) {
        completedOperations.incrementAndGet();

        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
            successCount.incrementAndGet();
            Log.d(TAG, "Fasilitas berhasil " + operationType);
        } else {
            Log.e(TAG, "Gagal " + operationType + " fasilitas");
        }

        checkAllOperationsCompleted(completedOperations, successCount, totalOperations);
    }

    private void handleOperationFailure(AtomicInteger completedOperations, int totalOperations,
                                        String operationType, String errorMessage) {
        completedOperations.incrementAndGet();
        Log.e(TAG, "Error " + operationType + " fasilitas: " + errorMessage);
        checkAllOperationsCompleted(completedOperations, new AtomicInteger(0), totalOperations);
    }

    private void checkAllOperationsCompleted(AtomicInteger completedOperations, AtomicInteger successCount, int totalOperations) {
        if (completedOperations.get() >= totalOperations) {
            runOnUiThread(() -> {
                if (successCount.get() == totalOperations) {
                    // Semua operasi berhasil
                    Toast.makeText(this, "Semua perubahan fasilitas berhasil disimpan", Toast.LENGTH_SHORT).show();
                    exitEditModeAfterSave();
                } else {
                    Toast.makeText(this,
                            "Beberapa perubahan gagal: " + successCount.get() + " dari " + totalOperations + " berhasil",
                            Toast.LENGTH_LONG).show();
                    // Tetap exit edit mode tapi beri warning
                    exitEditModeAfterSave();
                }
            });
        }
    }

    private void exitEditModeAfterSave() {
        pendingOperations.clear();
        hasUnsavedChanges = false;
        loadFasilitasData(); // Reload data dari server
        exitEditMode();
    }

    private void updateCurrentHunianData(String newNamaHunian, int newLuasTanah, int newLuasBangunan,
                                         String newDeskripsi, String gambarUnitBase64, String gambarDenahBase64) {
        // Update object currentHunian dengan data baru
        currentHunian.setNamaHunian(newNamaHunian);
        currentHunian.setLuasTanah(newLuasTanah);
        currentHunian.setLuasBangunan(newLuasBangunan);
        currentHunian.setDeskripsiHunian(newDeskripsi);

        if (!gambarUnitBase64.isEmpty() && !gambarUnitBase64.equals("null")) {
            currentHunian.setGambarUnit(gambarUnitBase64);
        }

        if (!gambarDenahBase64.isEmpty() && !gambarDenahBase64.equals("null")) {
            currentHunian.setGambarDenah(gambarDenahBase64);
        }

        Log.d(TAG, "✅ Current hunian data updated in memory");
    }

    private String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to base64: " + e.getMessage());
            return "";
        }
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }


    // PERBAIKAN: Di DetailUnitProyekActivity.java - Method untuk save hunian update histori
    private void saveHunianUpdateHistori(int hunianId, String newNamaHunian, String namaProyek,
                                         int luasTanah, int luasBangunan, String updatedBy, String imageData) {

        Log.d(TAG, "📝 Saving hunian update histori...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // ✅ PERBAIKAN: Validasi gambar untuk update
        String imageDataForHistori = "";
        if (imageData != null && !imageData.isEmpty() && !imageData.equals("null")) {
            String cleanImage = imageData.trim();
            if (cleanImage.length() >= 30 &&
                    !cleanImage.equals("NULL") &&
                    !cleanImage.endsWith("...") &&
                    !cleanImage.endsWith("..")) {

                imageDataForHistori = cleanImage;
                Log.d(TAG, "✅ Using image for hunian update histori: " + imageDataForHistori.length() + " chars");
            }
        }

        Call<BasicResponse> call = apiService.addHunianHistori(
                "add_hunian_histori",
                hunianId,
                newNamaHunian,
                namaProyek,
                luasTanah,
                luasBangunan,
                updatedBy,
                "Diubah",
                imageDataForHistori
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse historiResponse = response.body();
                    if (historiResponse.isSuccess()) {
                        Log.d(TAG, "✅ Hunian update histori saved: " + newNamaHunian);

                        // Kirim broadcast
                        sendUpdateBroadcast(newNamaHunian, namaProyek);
                    } else {
                        Log.e(TAG, "❌ Failed to save hunian update histori: " + historiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "❌ HTTP error saving hunian update histori: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Network error saving hunian update histori: " + t.getMessage());
                // Tetap kirim broadcast meskipun histori gagal
                sendUpdateBroadcast(newNamaHunian, namaProyek);
            }
        });
    }

    // ✅ METHOD BARU: Validasi gambar untuk histori
    private boolean isValidImageForHistori(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            Log.d(TAG, "❌ Image data is null or empty");
            return false;
        }

        String cleanData = imageData.trim();

        // Kriteria untuk gambar histori
        boolean isValid = cleanData.length() >= 100 &&
                !cleanData.equals("null") &&
                !cleanData.equals("NULL") &&
                cleanData.matches("^[A-Za-z0-9+/]*={0,2}$");

        Log.d(TAG, "🖼️ Histori Image Validation - Length: " + cleanData.length() +
                ", Is 'null': " + cleanData.equals("null") +
                ", Valid: " + isValid);

        return isValid;
    }



    @Override
    public void onBackPressed() {
        if (isEditMode) {
            if (hasUnsavedChanges) {
                new AlertDialog.Builder(this)
                        .setTitle("Perubahan Belum Disimpan")
                        .setMessage("Ada perubahan yang belum disimpan. Yakin ingin keluar?")
                        .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                            exitEditMode();
                            super.onBackPressed();
                        })
                        .setNegativeButton("Tetap Edit", null)
                        .show();
            } else {
                exitEditMode();
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}