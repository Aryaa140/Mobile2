package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.android.material.appbar.MaterialToolbar;
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
    }

    private void initViews() {
        topAppBar = findViewById(R.id.topAppBar);

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

        // Setup click listeners untuk gambar dan tombol
        setupImageClickListeners();
        setupButtonListeners();

        // Pastikan mode baca aktif saat inisialisasi
        setEditMode(false);
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

            editNamaHunian.setVisibility(View.VISIBLE);
            editDeskripsi.setVisibility(View.VISIBLE);

            // Tampilkan tombol tambah fasilitas
            btnTambahFasilitas.setVisibility(View.VISIBLE);

        } else {
            // Mode baca - tampilkan TextView, sembunyikan EditText
            txtNamaHunian.setVisibility(View.VISIBLE);
            txtDeskripsi.setVisibility(View.VISIBLE);

            editNamaHunian.setVisibility(View.GONE);
            editDeskripsi.setVisibility(View.GONE);

            // Sembunyikan tombol tambah fasilitas
            btnTambahFasilitas.setVisibility(View.GONE);
        }

        // Refresh tampilan fasilitas
        displayFasilitasData(tempFasilitasList);
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

        // Extract hanya angka dari luas tanah dan bangunan
        String luasTanahValue = String.valueOf(currentHunian.getLuasTanah());
        String luasBangunanValue = String.valueOf(currentHunian.getLuasBangunan());

        editLuasTanah.setText(luasTanahValue);
        editLuasBangunan.setText(luasBangunanValue);

        if (currentHunian.getDeskripsiHunian() != null && !currentHunian.getDeskripsiHunian().isEmpty()) {
            editDeskripsi.setText(currentHunian.getDeskripsiHunian());
        } else {
            editDeskripsi.setText("");
        }

        // Update menu visibility
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

    private void saveHunianChanges() {
        String newNamaHunian = editNamaHunian.getText().toString().trim();
        String newLuasTanahStr = editLuasTanah.getText().toString().trim();
        String newLuasBangunanStr = editLuasBangunan.getText().toString().trim();
        String newDeskripsi = editDeskripsi.getText().toString().trim();

        // Validation
        if (newNamaHunian.isEmpty()) {
            Toast.makeText(this, "Nama hunian tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newLuasTanahStr.isEmpty() || !newLuasTanahStr.matches("\\d+")) {
            Toast.makeText(this, "Luas tanah harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newLuasBangunanStr.isEmpty() || !newLuasBangunanStr.matches("\\d+")) {
            Toast.makeText(this, "Luas bangunan harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        int newLuasTanah = Integer.parseInt(newLuasTanahStr);
        int newLuasBangunan = Integer.parseInt(newLuasBangunanStr);

        // Prepare base64 strings for images
        final String[] gambarUnitBase64Holder = new String[1];
        final String[] gambarDenahBase64Holder = new String[1];

        if (selectedUnitBitmap != null) {
            gambarUnitBase64Holder[0] = bitmapToBase64(selectedUnitBitmap);
        } else {
            gambarUnitBase64Holder[0] = "";
        }

        if (selectedDenahBitmap != null) {
            gambarDenahBase64Holder[0] = bitmapToBase64(selectedDenahBitmap);
        } else {
            gambarDenahBase64Holder[0] = "";
        }

        // Show loading
        Toast.makeText(this, "Menyimpan perubahan...", Toast.LENGTH_SHORT).show();

        Call<BasicResponse> call = apiService.updateHunianComprehensive(
                "updateHunian",
                currentHunian.getIdHunian(),
                currentHunian.getNamaHunian(),
                newNamaHunian,
                newLuasTanah,
                newLuasBangunan,
                newDeskripsi,
                gambarUnitBase64Holder[0],
                gambarDenahBase64Holder[0]
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        // Jika hunian berhasil disimpan, lanjut simpan fasilitas
                        updateCurrentHunianData(newNamaHunian, newLuasTanah, newLuasBangunan, newDeskripsi,
                                gambarUnitBase64Holder[0], gambarDenahBase64Holder[0]);

                        // Simpan perubahan fasilitas jika ada
                        if (!pendingOperations.isEmpty()) {
                            saveAllFasilitasChanges();
                        } else {
                            // Jika tidak ada perubahan fasilitas, langsung exit edit mode
                            exitEditModeAfterSave();
                        }

                    } else {
                        Toast.makeText(DetailUnitProyekActivity.this, "Gagal menyimpan: " + updateResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(DetailUnitProyekActivity.this, "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(DetailUnitProyekActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        currentHunian.setNamaHunian(newNamaHunian);
        currentHunian.setLuasTanah(newLuasTanah);
        currentHunian.setLuasBangunan(newLuasBangunan);
        currentHunian.setDeskripsiHunian(newDeskripsi);

        if (!gambarUnitBase64.isEmpty()) {
            currentHunian.setGambarUnit(gambarUnitBase64);
        }
        if (!gambarDenahBase64.isEmpty()) {
            currentHunian.setGambarDenah(gambarDenahBase64);
        }
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