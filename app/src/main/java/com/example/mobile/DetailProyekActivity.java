package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailProyekActivity extends AppCompatActivity {

    private static final String TAG = "DetailProyekActivity";
    private static final int PICK_LOGO_REQUEST = 1;
    private static final int PICK_SITEPLAN_REQUEST = 2;
    private static final int PICK_FASILITAS_REQUEST = 3;

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private MaterialButton btnLihatUnit;
    private MaterialButton btnViewFull;

    // Views untuk Card 1 - Detail Proyek
    private ImageView imgProyek;
    private TextView txtNamaProyek;
    private TextView txtLokasiProyek;
    private TextView txtDeskripsiProyek;
    private EditText editNama;
    private EditText editLokasi;
    private EditText editDeskripsi;

    // Views untuk Card 2 - Fasilitas
    private RecyclerView recyclerViewFasilitas;
    private FasilitasAdapter fasilitasAdapter;
    private List<FasilitasItem> fasilitasList;
    private Button btnTambahFasilitas;

    // Views untuk Card 3 - Siteplan
    private ImageView imgSitePlan;
    private CardView cardSitePlan;
    private Proyek currentProyek;

    // Data
    private int idProyek;
    private ApiService apiService;

    // Edit mode variables
    private boolean isEditMode = false;
    private MenuItem menuEdit, menuSave, menuCancel;
    private Bitmap selectedLogoBitmap = null;
    private Bitmap selectedSiteplanBitmap = null;
    private Bitmap selectedFasilitasBitmap = null;

    // Variabel untuk dialog fasilitas
    private AlertDialog currentFasilitasDialog;
    private ImageView currentFasilitasDialogImageView;

    // User level
    private String userLevel;
    private SharedPreferences sharedPreferences;
    private CardView cardTambahFasilitas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_detail_proyek);
            Log.d(TAG, "Layout inflated successfully");

            // Ambil user level dari SharedPreferences
            sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            userLevel = sharedPreferences.getString("level", "Operator");
            Log.d(TAG, "User Level: " + userLevel);

            // HANYA ambil ID dari intent
            idProyek = getIntent().getIntExtra("ID_PROYEK", -1);
            Log.d(TAG, "Received ID: " + idProyek);

            apiService = RetrofitClient.getClient().create(ApiService.class);
            initViews();

            // Set toolbar sebagai action bar
            setSupportActionBar(topAppBar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            setupClickListeners();
            setupNavigation();

            // Load semua data dari API berdasarkan ID
            loadProyekData();

            Log.d(TAG, "Activity setup completed");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading page", Toast.LENGTH_SHORT).show();
            finish();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_proyek, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            // Tambahkan pengecekan level user sebelum masuk mode edit
            if (!"Admin".equals(userLevel)) {
                Toast.makeText(this, "Hanya Admin yang dapat mengedit proyek", Toast.LENGTH_SHORT).show();
                return true;
            }
            enterEditMode();
            return true;
        } else if (id == R.id.action_save) {
            saveProyekData();
            return true;
        } else if (id == R.id.action_cancel) {
            exitEditMode();
            return true;
        } else if (id == android.R.id.home) {
            // Handle back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMenuVisibility() {
        if (menuEdit != null) {
            // Hanya tampilkan menu edit untuk Admin
            menuEdit.setVisible(!isEditMode && "Admin".equals(userLevel));
        }
        if (menuSave != null) menuSave.setVisible(isEditMode);
        if (menuCancel != null) menuCancel.setVisible(isEditMode);
    }

    private void initViews() {
        try {
            topAppBar = findViewById(R.id.topAppBar);
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            btnLihatUnit = findViewById(R.id.btnLihatUnit);
            btnViewFull = findViewById(R.id.btnViewFull);

            // Card 1 - Detail Proyek
            imgProyek = findViewById(R.id.imgProyek);
            txtNamaProyek = findViewById(R.id.txtNama);
            txtLokasiProyek = findViewById(R.id.txtLokasi);
            txtDeskripsiProyek = findViewById(R.id.txtDeskripsi);

            // Edit views
            editNama = findViewById(R.id.editNama);
            editLokasi = findViewById(R.id.editLokasi);
            editDeskripsi = findViewById(R.id.editDeskripsi);

            // Card 2 - Fasilitas
            recyclerViewFasilitas = findViewById(R.id.recyclerViewFasilitas);
            fasilitasList = new ArrayList<>();

            // Inisialisasi adapter dengan listener untuk edit dan hapus
            fasilitasAdapter = new FasilitasAdapter(fasilitasList, new FasilitasAdapter.OnFasilitasActionListener() {
                @Override
                public void onEditFasilitas(FasilitasItem fasilitas) {
                    showEditFasilitasDialog(fasilitas);
                }

                @Override
                public void onDeleteFasilitas(FasilitasItem fasilitas) {
                    showDeleteFasilitasDialog(fasilitas);
                }

                @Override
                public void onViewFasilitas(FasilitasItem fasilitas) {
                    openFullScreenFasilitas(fasilitas);
                }
            });

            // GridLayoutManager dengan 2 kolom
            recyclerViewFasilitas.setLayoutManager(new GridLayoutManager(this, 2));

            // Spacing yang lebih kecil untuk hasil yang lebih rapi
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
            recyclerViewFasilitas.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels));

            recyclerViewFasilitas.setAdapter(fasilitasAdapter);

            // Inisialisasi btnTambahFasilitas
            cardTambahFasilitas = findViewById(R.id.cardTambahFasilitas);
            btnTambahFasilitas = findViewById(R.id.btnTambahFasilitas);

            // Card 3 - Siteplan
            imgSitePlan = findViewById(R.id.imgSitePlan);
            cardSitePlan = findViewById(R.id.cardSitePlanRiverside);

            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }

            Log.d(TAG, "Views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }



    private void showEditFasilitasDialog(FasilitasItem fasilitas) {
        // DEBUG: Validasi data fasilitas
        Log.d(TAG, "showEditFasilitasDialog - ID: " + fasilitas.getIdFasilitas() +
                ", Name: " + fasilitas.getNamaFasilitas());

        if (fasilitas.getIdFasilitas() <= 0) {
            Toast.makeText(this, "ERROR: ID Fasilitas tidak valid: " + fasilitas.getIdFasilitas(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid fasilitas ID in dialog: " + fasilitas.getIdFasilitas());
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fasilitas, null);

        EditText editNamaFasilitas = dialogView.findViewById(R.id.editNamaFasilitas);
        ImageView imageFasilitas = dialogView.findViewById(R.id.imageFasilitasDialog);
        Button btnPilihGambar = dialogView.findViewById(R.id.btnPilihGambarFasilitas);

        // Set data existing
        editNamaFasilitas.setText(fasilitas.getNamaFasilitas());
        selectedFasilitasBitmap = null;
        currentFasilitasDialogImageView = imageFasilitas;

        // Tampilkan gambar existing jika ada
        if (fasilitas.getGambarBase64() != null && !fasilitas.getGambarBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(fasilitas.getGambarBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageFasilitas.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Log.e(TAG, "Error loading existing fasilitas image: " + e.getMessage());
                imageFasilitas.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            imageFasilitas.setImageResource(R.drawable.ic_placeholder);
        }

        btnPilihGambar.setOnClickListener(v -> {
            openImagePicker(PICK_FASILITAS_REQUEST);
        });

        builder.setView(dialogView)
                .setTitle("Edit Fasilitas - ID: " + fasilitas.getIdFasilitas())
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String namaFasilitas = editNamaFasilitas.getText().toString().trim();
                    if (namaFasilitas.isEmpty()) {
                        Toast.makeText(this, "Nama fasilitas tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validasi sekali lagi sebelum update
                    if (fasilitas.getIdFasilitas() <= 0) {
                        Toast.makeText(this, "ERROR: ID Fasilitas masih tidak valid", Toast.LENGTH_LONG).show();
                        return;
                    }

                    updateFasilitas(fasilitas.getIdFasilitas(), namaFasilitas, selectedFasilitasBitmap);
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    currentFasilitasDialog = null;
                    currentFasilitasDialogImageView = null;
                });

        currentFasilitasDialog = builder.create();
        currentFasilitasDialog.show();
    }
    // Method untuk menampilkan dialog konfirmasi hapus fasilitas
    private void showDeleteFasilitasDialog(FasilitasItem fasilitas) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Fasilitas")
                .setMessage("Apakah Anda yakin ingin menghapus fasilitas \"" + fasilitas.getNamaFasilitas() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deleteFasilitas(fasilitas.getIdFasilitas());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void enterEditMode() {
        // Tambahkan pengecekan keamanan tambahan
        if (!"Admin".equals(userLevel)) {
            Toast.makeText(this, "Akses ditolak: Hanya Admin yang dapat mengedit", Toast.LENGTH_LONG).show();
            return;
        }

        isEditMode = true;

        // Show edit views, hide read-only views
        txtNamaProyek.setVisibility(View.GONE);
        txtLokasiProyek.setVisibility(View.GONE);
        txtDeskripsiProyek.setVisibility(View.GONE);

        editNama.setVisibility(View.VISIBLE);
        editLokasi.setVisibility(View.VISIBLE);
        editDeskripsi.setVisibility(View.VISIBLE);

        // Populate edit fields with current data
        if (currentProyek != null) {
            editNama.setText(currentProyek.getNamaProyek());
            editLokasi.setText(currentProyek.getLokasiProyek());
            editDeskripsi.setText(currentProyek.getDeskripsiProyek());
        }

        // Tampilkan tombol tambah fasilitas dan set mode edit di adapter
        if (cardTambahFasilitas != null) {
            cardTambahFasilitas.setVisibility(View.VISIBLE);
            btnTambahFasilitas.setOnClickListener(v -> {
                // PERBAIKAN: Keluar dari mode edit sebelum buka activity tambah fasilitas
                exitEditMode();

                // Buka activity tambah fasilitas proyek
                Intent intent = new Intent(DetailProyekActivity.this, TambahFasilitasProyekActivity.class);
                intent.putExtra("NAMA_PROYEK", currentProyek.getNamaProyek());
                startActivityForResult(intent, 200); // Request code baru
            });
        }

        // Set adapter ke mode edit
        fasilitasAdapter.setEditMode(true);

        // Update button text
        updateButtonText();

        // Update menu visibility
        updateMenuVisibility();

        // Enable image interaction
        imgProyek.setClickable(true);
        imgSitePlan.setClickable(true);

        // Add edit indicator to images
        imgProyek.setAlpha(0.8f);
        imgSitePlan.setAlpha(0.8f);

        Toast.makeText(this, "Mode Edit: Klik gambar untuk mengubah", Toast.LENGTH_LONG).show();
    }
    private void updateButtonText() {
        if (btnViewFull != null) {
            if (isEditMode) {
                btnViewFull.setText("Ubah Gambar");
                try {
                    btnViewFull.setIconResource(R.drawable.ic_edit);
                } catch (Exception e) {
                    Log.e(TAG, "Edit icon not found");
                    btnViewFull.setIcon(null);
                }
            } else {
                btnViewFull.setText("Lihat Full");
                try {
                    btnViewFull.setIconResource(R.drawable.ic_fullscreen);
                } catch (Exception e) {
                    Log.e(TAG, "Fullscreen icon not found");
                    btnViewFull.setIcon(null);
                }
            }
        }
    }

    private void exitEditMode() {
        isEditMode = false;

        // Show read-only views, hide edit views
        txtNamaProyek.setVisibility(View.VISIBLE);
        txtLokasiProyek.setVisibility(View.VISIBLE);
        txtDeskripsiProyek.setVisibility(View.VISIBLE);

        editNama.setVisibility(View.GONE);
        editLokasi.setVisibility(View.GONE);
        editDeskripsi.setVisibility(View.GONE);

        // Sembunyikan tombol tambah fasilitas dan set adapter ke mode normal
        if (cardTambahFasilitas != null) {
            cardTambahFasilitas.setVisibility(View.GONE);
        }
        fasilitasAdapter.setEditMode(false);

        // Update button text
        updateButtonText();

        // Update menu visibility
        updateMenuVisibility();

        // Reset selected images
        selectedLogoBitmap = null;
        selectedSiteplanBitmap = null;
        selectedFasilitasBitmap = null;

        // Disable image interaction
        imgProyek.setClickable(false);
        imgSitePlan.setClickable(false);

        // Remove edit indicator from images
        imgProyek.setAlpha(1.0f);
        imgSitePlan.setAlpha(1.0f);

        // Reload original data
        if (currentProyek != null) {
            setupData(currentProyek);
        }
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                if (requestCode == PICK_LOGO_REQUEST) {
                    selectedLogoBitmap = bitmap;
                    imgProyek.setImageBitmap(bitmap);
                    Toast.makeText(this, "Logo berhasil diubah", Toast.LENGTH_SHORT).show();
                } else if (requestCode == PICK_SITEPLAN_REQUEST) {
                    selectedSiteplanBitmap = bitmap;
                    imgSitePlan.setImageBitmap(bitmap);
                    Toast.makeText(this, "Siteplan berhasil diubah", Toast.LENGTH_SHORT).show();
                } else if (requestCode == PICK_FASILITAS_REQUEST) {
                    // Handle gambar fasilitas
                    selectedFasilitasBitmap = bitmap;
                    if (currentFasilitasDialogImageView != null) {
                        currentFasilitasDialogImageView.setImageBitmap(bitmap);
                    }
                    Toast.makeText(this, "Gambar fasilitas dipilih", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }

        // PERBAIKAN: Handle hasil dari TambahFasilitasProyekActivity - TANPA kondisi data != null
        if (requestCode == 200) {
            Log.d(TAG, "onActivityResult: Request code 200 diterima, resultCode: " + resultCode);

            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Fasilitas berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                // Pastikan sudah keluar dari mode edit
                if (isEditMode) {
                    Log.d(TAG, "Masih dalam mode edit, keluar dari mode edit");
                    exitEditMode();
                }

                // Reload data fasilitas
                if (currentProyek != null) {
                    Log.d(TAG, "Memuat ulang data fasilitas untuk proyek: " + currentProyek.getNamaProyek());
                    loadFasilitasData(currentProyek.getNamaProyek());
                } else {
                    Log.e(TAG, "currentProyek adalah null, tidak dapat memuat data fasilitas");
                }
            } else {
                Log.d(TAG, "TambahFasilitasProyekActivity dibatalkan atau gagal");
            }
        }
    }


    private void updateFasilitas(int idFasilitas, String namaFasilitas, Bitmap gambarBitmap) {
        Log.d(TAG, "=== UPDATE FASILITAS ===");
        Log.d(TAG, "ID Fasilitas: " + idFasilitas);
        Log.d(TAG, "Nama Fasilitas: " + namaFasilitas);
        Log.d(TAG, "Gambar Bitmap: " + (gambarBitmap != null ? "Ada" : "Tidak ada"));

        if (idFasilitas <= 0) {
            Toast.makeText(this, "ID Fasilitas tidak valid: " + idFasilitas, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid fasilitas ID: " + idFasilitas);
            return;
        }

        String gambarBase64 = "no_change"; // Default: tidak mengubah gambar


        // PERBAIKAN: Handle konversi bitmap ke base64 dengan lebih baik
        if (gambarBitmap != null) {
            try {
                gambarBase64 = bitmapToBase64(gambarBitmap);
                Log.d(TAG, "Gambar Base64 length: " + gambarBase64.length());

                // Validasi base64 string
                if (gambarBase64.length() < 100) {
                    Log.e(TAG, "Base64 string terlalu pendek, mungkin konversi gagal");
                    Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error converting bitmap to base64: " + e.getMessage());
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d(TAG, "Tidak ada gambar baru, menggunakan gambar existing (no_change)");
        }

        // Tampilkan loading
        Toast.makeText(this, "Mengupdate fasilitas...", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String namaUser = sharedPreferences.getString("nama_user", username);

        // ✅ BUAT CALL DENGAN PARAMETER USER INFO
        Call<BasicResponse> call = apiService.updateFasilitas(
                "updateFasilitas",
                idFasilitas,
                namaFasilitas,
                gambarBase64,
                username,      // ✅ TAMBAHKAN username
                namaUser       // ✅ TAMBAHKAN updated_by
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                // PERBAIKAN: Cek jika activity sudah destroyed
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "Activity sudah destroyed, mengabaikan response update fasilitas");
                    return;
                }

                Log.d(TAG, "Update fasilitas response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse updateResponse = response.body();
                    Log.d(TAG, "Update success: " + updateResponse.isSuccess());
                    Log.d(TAG, "Update message: " + updateResponse.getMessage());

                    if (updateResponse.isSuccess()) {
                        Toast.makeText(DetailProyekActivity.this, "Fasilitas berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // Tutup dialog
                        if (currentFasilitasDialog != null && currentFasilitasDialog.isShowing()) {
                            currentFasilitasDialog.dismiss();
                        }
                        currentFasilitasDialog = null;
                        currentFasilitasDialogImageView = null;

                        // PERBAIKAN: Keluar dari mode edit setelah update berhasil
                        exitEditMode();

                        // Reload fasilitas dengan delay kecil untuk memastikan data terupdate
                        new Handler().postDelayed(() -> {
                            if (currentProyek != null) {
                                loadFasilitasData(currentProyek.getNamaProyek());
                            }
                        }, 1000);

                    } else {
                        String errorMsg = "Gagal update fasilitas: " + updateResponse.getMessage();
                        Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, errorMsg);
                    }
                } else {
                    String errorMsg = "Error response dari server: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                // PERBAIKAN: Cek jika activity sudah destroyed
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "Activity sudah destroyed, mengabaikan failure update fasilitas");
                    return;
                }

                String errorMsg = "Error: " + t.getMessage();
                Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, errorMsg, t);
            }
        });
    }

    private void deleteFasilitas(int idFasilitas) {
        Log.d(TAG, "=== DELETE FASILITAS ===");
        Log.d(TAG, "ID Fasilitas: " + idFasilitas);

        if (idFasilitas <= 0) {
            Toast.makeText(this, "ID Fasilitas tidak valid: " + idFasilitas, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Invalid fasilitas ID for deletion: " + idFasilitas);
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String namaUser = sharedPreferences.getString("nama_user", username);

        // ✅ DAPATKAN NAMA FASILITAS (jika perlu)
        String fasilitasName = "";
        String proyekName = currentProyek != null ? currentProyek.getNamaProyek() : "";

        // ✅ BUAT CALL DENGAN PARAMETER USER INFO
        Call<BasicResponse> call = apiService.deleteFasilitas(
                "deleteFasilitas",
                idFasilitas,
                username,      // ✅ TAMBAHKAN username
                namaUser,      // ✅ TAMBAHKAN deleted_by
                proyekName     // ✅ TAMBAHKAN proyek_name
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                // PERBAIKAN: Cek jika activity sudah destroyed
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "Activity sudah destroyed, mengabaikan response delete fasilitas");
                    return;
                }

                Log.d(TAG, "Delete fasilitas response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse deleteResponse = response.body();
                    Log.d(TAG, "Delete success: " + deleteResponse.isSuccess());
                    Log.d(TAG, "Delete message: " + deleteResponse.getMessage());

                    if (deleteResponse.isSuccess()) {
                        Toast.makeText(DetailProyekActivity.this, "Fasilitas berhasil dihapus", Toast.LENGTH_SHORT).show();
                        // PERBAIKAN: Keluar dari mode edit setelah hapus berhasil
                        exitEditMode();
                        // Reload fasilitas
                        loadFasilitasData(currentProyek.getNamaProyek());
                    } else {
                        String errorMsg = "Gagal hapus fasilitas: " + deleteResponse.getMessage();
                        Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, errorMsg);
                    }
                } else {
                    String errorMsg = "Error response dari server: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                // PERBAIKAN: Cek jika activity sudah destroyed
                if (isFinishing() || isDestroyed()) {
                    Log.w(TAG, "Activity sudah destroyed, mengabaikan failure delete fasilitas");
                    return;
                }

                String errorMsg = "Error: " + t.getMessage();
                Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                Log.e(TAG, errorMsg, t);
            }
        });
    }

    // DI DetailProyekActivity.java
    private void sendProyekUpdateBroadcast() {
        try {
            Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
            broadcastIntent.putExtra("ACTION", "PROYEK_UPDATED");
            broadcastIntent.putExtra("TYPE", "proyek");
            broadcastIntent.putExtra("NAMA_PROYEK", currentProyek.getNamaProyek());
            broadcastIntent.putExtra("LOKASI_PROYEK", currentProyek.getLokasiProyek());

            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "");
            String namaLengkap = prefs.getString("nama_lengkap", username);
            broadcastIntent.putExtra("PENGINPUT", namaLengkap);

            sendBroadcast(broadcastIntent);
            Log.d(TAG, "📢 Broadcast sent for proyek update");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending proyek broadcast: " + e.getMessage());
        }
    }

    // Panggil method ini setelah update proyek berhasil di saveProyekData()
    private void saveProyekData() {
        if (currentProyek == null) {
            Toast.makeText(this, "Data proyek tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        String newNama = editNama.getText().toString().trim();
        String newLokasi = editLokasi.getText().toString().trim();
        String newDeskripsi = editDeskripsi.getText().toString().trim();

        // Validation
        if (newNama.isEmpty() || newLokasi.isEmpty() || newDeskripsi.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ DAPATKAN USER INFO UNTUK NOTIFIKASI
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String namaUser = sharedPreferences.getString("nama_user", username);

        if (namaUser.isEmpty()) {
            namaUser = username;
        }

        Log.d(TAG, "User info - Username: " + username + ", Nama: " + namaUser);

        // SOLUSI: Gunakan array untuk menghindari masalah inner class
        final String[] logoBase64Holder = new String[1];
        final String[] siteplanBase64Holder = new String[1];

        // Prepare base64 strings for images
        if (selectedLogoBitmap != null) {
            logoBase64Holder[0] = bitmapToBase64(selectedLogoBitmap);
            Log.d(TAG, "New logo base64 prepared, length: " + logoBase64Holder[0].length());
        } else {
            // Jika tidak memilih gambar baru, kirim string kosong
            logoBase64Holder[0] = "";
            Log.d(TAG, "No new logo selected, sending empty string");
        }

        if (selectedSiteplanBitmap != null) {
            siteplanBase64Holder[0] = bitmapToBase64(selectedSiteplanBitmap);
            Log.d(TAG, "New siteplan base64 prepared, length: " + siteplanBase64Holder[0].length());
        } else {
            // Jika tidak memilih gambar baru, kirim string kosong
            siteplanBase64Holder[0] = "";
            Log.d(TAG, "No new siteplan selected, sending empty string");
        }

        // Show loading
        Toast.makeText(this, "Menyimpan perubahan...", Toast.LENGTH_SHORT).show();

        // ✅ PERBAIKAN: Deklarasikan variabel final untuk inner class
        // ✅ PERBAIKAN: Deklarasikan variabel final untuk inner class
        final int proyekId = currentProyek.getIdProyek();
        final String lokasiProyekUpdate = currentProyek.getLokasiProyek(); // Ambil dari currentProyek
        username = sharedPreferences.getString("username", "");
        final String updatedBy = sharedPreferences.getString("nama_lengkap", username);

        Log.d(TAG, "Sending update request - ID: " + proyekId +
                ", Old Name: " + currentProyek.getNamaProyek() +
                ", New Name: " + newNama +
                ", Updated By: " + updatedBy +
                ", Username: " + username);

        Call<BasicResponse> call = apiService.updateProyekComprehensive(
                proyekId,
                currentProyek.getNamaProyek(),
                newNama,
                newLokasi,
                newDeskripsi,
                logoBase64Holder[0],
                siteplanBase64Holder[0],
                updatedBy
        );


        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    BasicResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        Toast.makeText(DetailProyekActivity.this, "Proyek berhasil diupdate", Toast.LENGTH_SHORT).show();

                        // ✅ TAMPILKAN INFO FCM JIKA ADA
                        if (updateResponse.getFcmNotification() != null) {
                            Log.d(TAG, "FCM Notification Result: " + updateResponse.getFcmNotification());
                        }

                        // Update current project data
                        currentProyek.setNamaProyek(newNama);
                        currentProyek.setLokasiProyek(newLokasi);
                        currentProyek.setDeskripsiProyek(newDeskripsi);

                        // Update gambar jika ada perubahan
                        if (!logoBase64Holder[0].isEmpty()) {
                            currentProyek.setLogoBase64(logoBase64Holder[0]);
                        }
                        if (!siteplanBase64Holder[0].isEmpty()) {
                            currentProyek.setSiteplanBase64(siteplanBase64Holder[0]);
                        }

                        // Update toolbar title
                        if (topAppBar != null) {
                            topAppBar.setTitle("Detail " + newNama);
                        }

                        // ✅ PERBAIKAN: JANGAN panggil saveProyekUpdateToHistori() lagi
                        // Histori sudah disimpan otomatis di PHP

                        sendProyekUpdateBroadcast();

                        exitEditMode();

                        // Reload facilities with new project name
                        loadFasilitasData(newNama);

                    } else {
                        Toast.makeText(DetailProyekActivity.this, "Gagal update: " + updateResponse.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Update failed: " + updateResponse.getMessage());
                    }
                } else {
                    String errorMsg = "Error response dari server: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(DetailProyekActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, errorMsg);
                }
            }
            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "Network error updating project: " + t.getMessage(), t);
                Toast.makeText(DetailProyekActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null in bitmapToBase64");
            return "";
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Kompres gambar dengan kualitas optimal
            // PERBAIKAN: Gunakan JPEG untuk kompresi yang lebih baik
            boolean compressSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

            if (!compressSuccess) {
                Log.e(TAG, "Bitmap compress failed");
                return "";
            }

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Log.d(TAG, "Bitmap converted to base64, original size: " + bitmap.getByteCount() +
                    " bytes, compressed size: " + byteArray.length + " bytes, base64 length: " + base64.length() + " chars");

            // Clean up
            byteArrayOutputStream.close();

            return base64;
        } catch (Exception e) {
            Log.e(TAG, "Error converting bitmap to base64: " + e.getMessage(), e);
            return "";
        }
    }

    private void loadProyekData() {
        if (idProyek == -1) {
            Toast.makeText(this, "ID Proyek tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading project data for ID: " + idProyek);

        // Load data proyek dari API
        Call<List<Proyek>> call = apiService.getAllProyek("getProyek");
        call.enqueue(new Callback<List<Proyek>>() {
            @Override
            public void onResponse(Call<List<Proyek>> call, Response<List<Proyek>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Proyek> proyekList = response.body();
                    Log.d(TAG, "Received " + proyekList.size() + " projects from API");

                    // Cari proyek dengan ID yang sesuai
                    Proyek selectedProyek = null;
                    for (Proyek proyek : proyekList) {
                        Log.d(TAG, "Checking project - ID: " + proyek.getIdProyek() + ", Name: " + proyek.getNamaProyek());

                        if (proyek.getIdProyek() == idProyek) {
                            selectedProyek = proyek;

                            // Debug detailed information about the found project
                            Log.d(TAG, "=== FOUND PROJECT DETAILS ===");
                            Log.d(TAG, "ID: " + proyek.getIdProyek());
                            Log.d(TAG, "Name: " + proyek.getNamaProyek());
                            Log.d(TAG, "Location: " + proyek.getLokasiProyek());
                            Log.d(TAG, "Logo Base64 exists: " + (proyek.getLogoBase64() != null));
                            Log.d(TAG, "Logo Base64 length: " + (proyek.getLogoBase64() != null ? proyek.getLogoBase64().length() : 0));
                            Log.d(TAG, "Siteplan Base64 exists: " + (proyek.getSiteplanBase64() != null));
                            Log.d(TAG, "Siteplan Base64 length: " + (proyek.getSiteplanBase64() != null ? proyek.getSiteplanBase64().length() : 0));

                            if (proyek.getLogoBase64() != null && proyek.getLogoBase64().length() > 100) {
                                Log.d(TAG, "Logo Base64 preview: " + proyek.getLogoBase64().substring(0, 100) + "...");
                            }

                            break;
                        }
                    }

                    if (selectedProyek != null) {
                        setupData(selectedProyek);
                        loadFasilitasData(selectedProyek.getNamaProyek());
                    } else {
                        Log.e(TAG, "No project found with ID: " + idProyek);
                        Toast.makeText(DetailProyekActivity.this, "Proyek tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.e(TAG, "Failed to load project data. Response code: " + response.code());
                    Toast.makeText(DetailProyekActivity.this, "Gagal memuat data proyek", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Proyek>> call, Throwable t) {
                Log.e(TAG, "Network error loading project: " + t.getMessage());
                Toast.makeText(DetailProyekActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupData(Proyek proyek) {
        try {
            this.currentProyek = proyek;

            // Set data untuk Card 1
            if (txtNamaProyek != null) txtNamaProyek.setText(proyek.getNamaProyek());
            if (txtLokasiProyek != null) txtLokasiProyek.setText(proyek.getLokasiProyek());
            if (txtDeskripsiProyek != null) txtDeskripsiProyek.setText(proyek.getDeskripsiProyek());

            // Debug informasi proyek
            Log.d(TAG, "Setting up data for project: " + proyek.getNamaProyek());
            Log.d(TAG, "Logo base64 available: " + (proyek.getLogoBase64() != null && !proyek.getLogoBase64().isEmpty()));
            Log.d(TAG, "Siteplan base64 available: " + (proyek.getSiteplanBase64() != null && !proyek.getSiteplanBase64().isEmpty()));

            // Set logo proyek dengan improved error handling
            if (proyek.getLogoBase64() != null && !proyek.getLogoBase64().isEmpty()) {
                boolean logoSuccess = setImageFromBase64(proyek.getLogoBase64(), imgProyek, "logo");
                if (!logoSuccess) {
                    imgProyek.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                Log.w(TAG, "Logo base64 is null or empty");
                imgProyek.setImageResource(R.drawable.ic_placeholder);
            }

            // Set siteplan dengan improved error handling
            if (proyek.getSiteplanBase64() != null && !proyek.getSiteplanBase64().isEmpty()) {
                Log.d(TAG, "Siteplan base64 length: " + proyek.getSiteplanBase64().length());
                boolean siteplanSuccess = setImageFromBase64(proyek.getSiteplanBase64(), imgSitePlan, "siteplan");
                if (siteplanSuccess) {
                    cardSitePlan.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Siteplan successfully set and card shown");
                } else {
                    Log.e(TAG, "Failed to set siteplan, hiding card");
                    cardSitePlan.setVisibility(View.GONE);
                }
            } else {
                Log.w(TAG, "Siteplan base64 is null or empty, hiding card");
                cardSitePlan.setVisibility(View.GONE);
            }

            // Update toolbar title
            if (topAppBar != null) {
                topAppBar.setTitle("Detail " + proyek.getNamaProyek());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up data: " + e.getMessage(), e);
        }
    }

    // Method helper untuk decode Base64 dengan error handling yang lebih baik
    private boolean setImageFromBase64(String base64String, ImageView imageView, String type) {
        if (base64String == null || base64String.isEmpty()) {
            Log.e(TAG, type + " base64 string is null or empty");
            return false;
        }

        try {
            // Clean the base64 string - remove any whitespace or invalid characters
            String cleanBase64 = base64String.trim();

            // Remove data URL prefix if present
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }

            // Remove any whitespace characters
            cleanBase64 = cleanBase64.replaceAll("\\s", "");

            Log.d(TAG, "Cleaned " + type + " base64 length: " + cleanBase64.length());

            // Validate base64 string
            if (!isValidBase64(cleanBase64)) {
                Log.e(TAG, type + " base64 string is not valid");
                return false;
            }

            // Decode base64 to byte array
            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);
            if (decodedBytes == null || decodedBytes.length == 0) {
                Log.e(TAG, type + " decoded bytes are null or empty");
                return false;
            }

            Log.d(TAG, type + " decoded bytes length: " + decodedBytes.length);

            // Try to decode bitmap with different options
            BitmapFactory.Options options = new BitmapFactory.Options();

            // First, just get the dimensions
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

            Log.d(TAG, type + " image dimensions: " + options.outWidth + "x" + options.outHeight);
            Log.d(TAG, type + " image mime type: " + options.outMimeType);

            // Check if the image format is supported
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.e(TAG, type + " invalid image dimensions");
                return false;
            }

            // Calculate sample size to avoid memory issues
            options.inSampleSize = calculateInSampleSize(options, 800, 600);
            options.inJustDecodeBounds = false;

            // Try to decode the bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, type + " successfully decoded and set");
                return true;
            } else {
                Log.e(TAG, type + " bitmap is null after decoding");
                return false;
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, type + " IllegalArgumentException: " + e.getMessage());
            return false;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, type + " OutOfMemoryError: " + e.getMessage());
            // Try with larger sample size
            return setImageFromBase64WithLargerSample(base64String, imageView, type);
        } catch (Exception e) {
            Log.e(TAG, type + " Error decoding base64: " + e.getMessage());
            return false;
        }
    }

    private void tambahFasilitas(String namaFasilitas, Bitmap gambarBitmap) {
        if (currentProyek == null) return;

        // ✅ DAPATKAN USER INFO
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String namaUser = sharedPreferences.getString("nama_user", username);

        if (namaUser.isEmpty()) {
            namaUser = username;
        }

        Log.d(TAG, "Tambah fasilitas - User: " + username + ", Nama: " + namaUser);

        String gambarBase64 = "";
        if (gambarBitmap != null) {
            gambarBase64 = bitmapToBase64(gambarBitmap);
        }

        // ✅ PERBAIKAN: Gunakan method dengan 6 parameter
        Call<BasicResponse> call = apiService.addFasilitas(
                "addFasilitas",
                namaFasilitas,
                currentProyek.getNamaProyek(),
                gambarBase64,
                username,      // ✅ Parameter ke-5: username
                namaUser       // ✅ Parameter ke-6: created_by
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse addResponse = response.body();
                    if (addResponse.isSuccess()) {
                        Toast.makeText(DetailProyekActivity.this,
                                "Fasilitas berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                        // ✅ TAMPILKAN INFO FCM JIKA ADA
                        if (addResponse.getFcmNotification() != null) {
                            Log.d(TAG, "FCM Add Notification: " + addResponse.getFcmNotification());
                        }

                        // Tutup dialog
                        if (currentFasilitasDialog != null) {
                            currentFasilitasDialog.dismiss();
                            currentFasilitasDialog = null;
                            currentFasilitasDialogImageView = null;
                        }
                        // Reload fasilitas
                        loadFasilitasData(currentProyek.getNamaProyek());
                    } else {
                        Toast.makeText(DetailProyekActivity.this,
                                "Gagal menambah fasilitas: " + addResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailProyekActivity.this,
                            "Error response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(DetailProyekActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error adding fasilitas: " + t.getMessage());
            }
        });
    }

    private boolean setImageFromBase64WithLargerSample(String base64String, ImageView imageView, String type) {
        try {
            String cleanBase64 = base64String.trim();
            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
            }
            cleanBase64 = cleanBase64.replaceAll("\\s", "");

            byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // Larger sample size to reduce memory usage

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, type + " successfully decoded with larger sample size");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, type + " Error in fallback decoding: " + e.getMessage());
        }
        return false;
    }

    // Method untuk validasi Base64 string
    private boolean isValidBase64(String base64) {
        try {
            Base64.decode(base64, Base64.DEFAULT);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Method untuk calculate sample size
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        Log.d(TAG, "Calculated inSampleSize: " + inSampleSize);
        return inSampleSize;
    }

    private void loadFasilitasData(String namaProyek) {
        if (namaProyek == null || namaProyek.isEmpty()) {
            Log.e(TAG, "Nama proyek is null or empty");
            hideFasilitasCard();
            return;
        }

        Log.d(TAG, "Loading facilities for project name: " + namaProyek);

        try {
            // PASTIKAN MENGGUNAKAN api_fasilitas.php
            Call<List<FasilitasItem>> call = apiService.getFasilitasByProyekFromFasilitas("getFasilitasByProyek", namaProyek);
            call.enqueue(new Callback<List<FasilitasItem>>() {
                @Override
                public void onResponse(Call<List<FasilitasItem>> call, Response<List<FasilitasItem>> response) {
                    Log.d(TAG, "Facilities API response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            fasilitasList.clear();
                            List<FasilitasItem> responseBody = response.body();
                            Log.d(TAG, "Response body size: " + responseBody.size());

                            // DEBUG DETAILED: Log setiap fasilitas yang diterima
                            for (FasilitasItem item : responseBody) {
                                Log.d(TAG, "=== FASILITAS DETAIL FROM API_FASILITAS ===");
                                Log.d(TAG, "ID: " + item.getIdFasilitas() + " (Type: " + ((Object)item.getIdFasilitas()).getClass().getSimpleName() + ")");
                                Log.d(TAG, "Nama: " + item.getNamaFasilitas());
                                Log.d(TAG, "Proyek: " + item.getNamaProyek());
                                Log.d(TAG, "Gambar length: " + (item.getGambarBase64() != null ? item.getGambarBase64().length() : 0));

                                // Validasi ID
                                if (item.getIdFasilitas() <= 0) {
                                    Log.e(TAG, "ERROR: Received invalid ID for fasilitas: " + item.getNamaFasilitas());
                                }
                                Log.d(TAG, "=== END FASILITAS ===");
                            }

                            fasilitasList.addAll(responseBody);
                            fasilitasAdapter.notifyDataSetChanged();

                            Log.d(TAG, "Successfully loaded " + fasilitasList.size() + " facilities from api_fasilitas.php");
                            showOrHideFasilitasCard();
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing facilities data: " + e.getMessage(), e);
                            hideFasilitasCard();
                        }
                    } else {
                        Log.e(TAG, "Facilities API response error: " + response.code());
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                        }
                        hideFasilitasCard();
                    }
                }

                @Override
                public void onFailure(Call<List<FasilitasItem>> call, Throwable t) {
                    Log.e(TAG, "Network error loading facilities: " + t.getMessage(), t);
                    hideFasilitasCard();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadFasilitasData: " + e.getMessage(), e);
            hideFasilitasCard();
        }
    }
    private void showOrHideFasilitasCard() {
        try {
            CardView cardFasilitas = findViewById(R.id.cardFasilitasSarana);
            if (cardFasilitas != null) {
                if (fasilitasList.isEmpty() && !isEditMode) {
                    cardFasilitas.setVisibility(View.GONE);
                } else {
                    cardFasilitas.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing/hiding facilities card: " + e.getMessage());
        }
    }

    private void hideFasilitasCard() {
        try {
            CardView cardFasilitas = findViewById(R.id.cardFasilitasSarana);
            if (cardFasilitas != null) {
                cardFasilitas.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding facilities card: " + e.getMessage());
        }
    }

    private void openFullScreenFasilitas(FasilitasItem fasilitas) {
        try {
            if (fasilitas.getGambarBase64() != null && !fasilitas.getGambarBase64().isEmpty()) {
                String cacheKey = "fasilitas_" + System.currentTimeMillis();
                saveToCache(cacheKey, fasilitas.getGambarBase64());

                Intent intent = new Intent(DetailProyekActivity.this, FullscreenSiteplanActivity.class);
                intent.putExtra("CACHE_KEY", cacheKey);
                intent.putExtra("PROYEK_NAME", fasilitas.getNamaFasilitas());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                Toast.makeText(this, "Gambar fasilitas tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening full screen fasilitas: " + e.getMessage(), e);
            Toast.makeText(this, "Tidak dapat membuka gambar fasilitas", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            // Button Lihat Unit
            if (btnLihatUnit != null) {
                btnLihatUnit.setOnClickListener(v -> {
                    try {
                        if (currentProyek != null) {
                            Intent intent = new Intent(DetailProyekActivity.this, UnitProyekActivity.class);
                            intent.putExtra("NAMA_PROYEK", currentProyek.getNamaProyek());
                            startActivity(intent);
                        } else {
                            Toast.makeText(DetailProyekActivity.this, "Data proyek tidak tersedia", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening UnitProyekActivity: " + e.getMessage());
                        Toast.makeText(this, "Tidak dapat membuka halaman unit", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Button View Full / Ubah Gambar
            if (btnViewFull != null) {
                btnViewFull.setOnClickListener(v -> {
                    if (isEditMode) {
                        // Tambahkan pengecekan level user
                        if (!"Admin".equals(userLevel)) {
                            Toast.makeText(this, "Hanya Admin yang dapat mengubah gambar", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Mode edit: buka image picker untuk siteplan
                        openImagePicker(PICK_SITEPLAN_REQUEST);
                    } else {
                        // Mode normal: buka fullscreen
                        openFullScreenSiteplan();
                    }
                });
            }

            // Image Site Plan juga bisa diklik untuk fullscreen atau edit
            if (imgSitePlan != null) {
                imgSitePlan.setOnClickListener(v -> {
                    if (isEditMode) {
                        // Tambahkan pengecekan level user
                        if (!"Admin".equals(userLevel)) {
                            Toast.makeText(this, "Hanya Admin yang dapat mengubah gambar", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        openImagePicker(PICK_SITEPLAN_REQUEST);
                    } else {
                        openFullScreenSiteplan();
                    }
                });
            }

            // Set click listeners for logo editing
            imgProyek.setOnClickListener(v -> {
                if (isEditMode) {
                    // Tambahkan pengecekan level user
                    if (!"Admin".equals(userLevel)) {
                        Toast.makeText(this, "Hanya Admin yang dapat mengubah gambar", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    openImagePicker(PICK_LOGO_REQUEST);
                }
            });

            Log.d(TAG, "Click listeners setup successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void openFullScreenSiteplan() {
        try {
            Log.d(TAG, "openFullScreenSiteplan called");

            if (currentProyek == null || currentProyek.getSiteplanBase64() == null ||
                    currentProyek.getSiteplanBase64().isEmpty()) {
                Toast.makeText(this, "Siteplan tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simpan base64 ke cache aplikasi
            String cacheKey = "siteplan_" + System.currentTimeMillis();
            saveToCache(cacheKey, currentProyek.getSiteplanBase64());

            // Buka activity fullscreen dengan key cache
            Intent intent = new Intent(DetailProyekActivity.this, FullscreenSiteplanActivity.class);
            intent.putExtra("CACHE_KEY", cacheKey);
            intent.putExtra("PROYEK_NAME", currentProyek.getNamaProyek());
            startActivity(intent);

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error opening full screen siteplan: " + e.getMessage(), e);
            Toast.makeText(this, "Tidak dapat membuka siteplan", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToCache(String key, String data) {
        try {
            SharedPreferences prefs = getSharedPreferences("siteplan_cache", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Split data besar menjadi chunks jika perlu
            if (data.length() > 100000) {
                int chunks = (int) Math.ceil(data.length() / 100000.0);
                for (int i = 0; i < chunks; i++) {
                    int start = i * 100000;
                    int end = Math.min(start + 100000, data.length());
                    String chunk = data.substring(start, end);
                    editor.putString(key + "_chunk_" + i, chunk);
                }
                editor.putInt(key + "_chunks", chunks);
            } else {
                editor.putString(key, data);
            }

            editor.apply();
            Log.d(TAG, "Data saved to cache with key: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error saving to cache: " + e.getMessage());
        }
    }

    private void saveProyekUpdateToHistori(int proyekId, String namaProyek, String lokasiProyek, String imageData) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String penginput = prefs.getString("nama_lengkap", prefs.getString("username", "User"));

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<BasicResponse> call = apiService.addProyekHistori(
                "add_proyek_histori", // action
                proyekId,
                namaProyek,
                lokasiProyek,
                penginput,
                "Diubah",  // PERUBAHAN: Status "Diubah"
                imageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Proyek update histori saved");

                    // ✅ PERBAIKAN: Kirim broadcast untuk refresh NewsActivity
                    Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
                    broadcastIntent.putExtra("ACTION", "PROYEK_UPDATED");
                    broadcastIntent.putExtra("TYPE", "proyek");
                    broadcastIntent.putExtra("NAMA_PROYEK", namaProyek);
                    broadcastIntent.putExtra("LOKASI_PROYEK", lokasiProyek);
                    broadcastIntent.putExtra("PENGINPUT", penginput);
                    broadcastIntent.putExtra("PROYEK_ID", proyekId);

                    sendBroadcast(broadcastIntent);
                    Log.d(TAG, "📡 Broadcast sent for proyek update: " + namaProyek);
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e(TAG, "❌ Failed to save proyek update histori: " + t.getMessage());

                // Tetap kirim broadcast meskipun histori gagal
                try {
                    Intent broadcastIntent = new Intent("REFRESH_NEWS_DATA");
                    broadcastIntent.putExtra("ACTION", "PROYEK_UPDATED");
                    broadcastIntent.putExtra("TYPE", "proyek");
                    broadcastIntent.putExtra("NAMA_PROYEK", namaProyek);
                    broadcastIntent.putExtra("LOKASI_PROYEK", lokasiProyek);
                    broadcastIntent.putExtra("PENGINPUT", penginput);

                    sendBroadcast(broadcastIntent);
                    Log.d(TAG, "📡 Broadcast sent (fallback) for proyek update: " + namaProyek);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error sending fallback broadcast: " + e.getMessage());
                }
            }
        });
    }

    private void setupNavigation() {
        try {
            // TopAppBar navigation
            if (topAppBar != null) {
                topAppBar.setNavigationOnClickListener(v -> {
                    try {
                        onBackPressed();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating back: " + e.getMessage());
                        finish();
                    }
                });
            }

            // Bottom Navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnItemSelectedListener(item -> {
                    int id = item.getItemId();
                    try {
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
                    } catch (Exception e) {
                        Log.e(TAG, "Error in bottom navigation: " + e.getMessage());
                    }
                    return false;
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}