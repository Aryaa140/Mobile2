package com.example.mobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InputDataProyekActivity extends AppCompatActivity {

    private static final String TAG = "InputDataProyekActivity";
    private static final int PICK_LOGO_REQUEST = 1;
    private static final int PICK_SITEPLAN_REQUEST = 2;
    private static final int PICK_FASILITAS_IMAGE_REQUEST = 3;

    private Button btnSimpan, btnBatal, btnPilihLogo, btnPilihSiteplan, btnPilihGambarFasilitas, btnTambahFasilitas;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;
    private EditText editTextNamaProyek, editTextLokasiProyek, editTextDeskripsiProyek, editTextNamaFasilitas;
    private ImageView imagePreviewLogo, imagePreviewSiteplan, imagePreviewFasilitas;
    private RecyclerView recyclerViewFasilitas;
    private View textListFasilitas;

    private ApiService apiService;
    private FasilitasAdapter fasilitasAdapter;
    private List<FasilitasItem> fasilitasList;

    private Uri logoUri, siteplanUri, currentFasilitasImageUri;
    private String logoBase64, siteplanBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_data_proyek);

        // Inisialisasi API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Inisialisasi view
        initViews();
        setupRecyclerView();
        setupNavigation();
        setupButtonListeners();
    }

    private void initViews() {
        try {
            btnSimpan = findViewById(R.id.btnSimpan);
            btnBatal = findViewById(R.id.btnBatal);
            btnPilihLogo = findViewById(R.id.btnPilihLogo);
            btnPilihSiteplan = findViewById(R.id.btnPilihSiteplan);
            btnPilihGambarFasilitas = findViewById(R.id.btnPilihGambarFasilitas);
            btnTambahFasilitas = findViewById(R.id.btnTambahFasilitas);

            editTextNamaProyek = findViewById(R.id.editTextNamaProyek);
            editTextLokasiProyek = findViewById(R.id.editTextLokasiProyek);
            editTextDeskripsiProyek = findViewById(R.id.editTextDeskripsiProyek);
            editTextNamaFasilitas = findViewById(R.id.editTextNamaFasilitas);

            imagePreviewLogo = findViewById(R.id.imagePreviewLogo);
            imagePreviewSiteplan = findViewById(R.id.imagePreviewSiteplan);
            imagePreviewFasilitas = findViewById(R.id.imagePreviewFasilitas);

            recyclerViewFasilitas = findViewById(R.id.recyclerViewFasilitas);
            textListFasilitas = findViewById(R.id.textListFasilitas);

            topAppBar = findViewById(R.id.topAppBar);
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            fasilitasList = new ArrayList<>();

            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error initializing UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerView() {
        try {
            fasilitasAdapter = new FasilitasAdapter(fasilitasList);
            recyclerViewFasilitas.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewFasilitas.setAdapter(fasilitasAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recycler view: " + e.getMessage());
        }
    }

    private void setupNavigation() {
        try {
            // Navigasi toolbar
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
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation: " + e.getMessage());
        }
    }

    private void setupButtonListeners() {
        try {
            // Simpan data proyek
            btnSimpan.setOnClickListener(v -> simpanDataProyek());

            // Batal input
            btnBatal.setOnClickListener(v -> navigateToHome());

            // Pilih gambar
            btnPilihLogo.setOnClickListener(v -> pilihGambar(PICK_LOGO_REQUEST));
            btnPilihSiteplan.setOnClickListener(v -> pilihGambar(PICK_SITEPLAN_REQUEST));
            btnPilihGambarFasilitas.setOnClickListener(v -> pilihGambar(PICK_FASILITAS_IMAGE_REQUEST));

            // Tambah fasilitas ke list
            btnTambahFasilitas.setOnClickListener(v -> tambahFasilitasKeList());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up button listeners: " + e.getMessage());
        }
    }

    private void pilihGambar(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Error picking image: " + e.getMessage());
            Toast.makeText(this, "Error memilih gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void tambahFasilitasKeList() {
        try {
            String namaFasilitas = editTextNamaFasilitas.getText().toString().trim();

            if (namaFasilitas.isEmpty()) {
                editTextNamaFasilitas.setError("Nama fasilitas harus diisi");
                return;
            }

            if (currentFasilitasImageUri == null) {
                Toast.makeText(this, "Pilih gambar fasilitas terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String gambarBase64 = convertImageToBase64(currentFasilitasImageUri);
            FasilitasItem fasilitas = new FasilitasItem(namaFasilitas, gambarBase64);
            fasilitasList.add(fasilitas);

            // Update UI
            fasilitasAdapter.notifyDataSetChanged();
            recyclerViewFasilitas.setVisibility(View.VISIBLE);
            textListFasilitas.setVisibility(View.VISIBLE);

            // Reset form fasilitas
            editTextNamaFasilitas.setText("");
            imagePreviewFasilitas.setVisibility(View.INVISIBLE);
            currentFasilitasImageUri = null;

            Toast.makeText(this, "Fasilitas ditambahkan ke list", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error adding facility to list: " + e.getMessage());
            Toast.makeText(this, "Error menambahkan fasilitas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                switch (requestCode) {
                    case PICK_LOGO_REQUEST:
                        logoUri = imageUri;
                        logoBase64 = convertImageToBase64(imageUri);
                        imagePreviewLogo.setImageURI(imageUri);
                        imagePreviewLogo.setVisibility(View.VISIBLE);
                        break;

                    case PICK_SITEPLAN_REQUEST:
                        siteplanUri = imageUri;
                        siteplanBase64 = convertImageToBase64(imageUri);
                        imagePreviewSiteplan.setImageURI(imageUri);
                        imagePreviewSiteplan.setVisibility(View.VISIBLE);
                        break;

                    case PICK_FASILITAS_IMAGE_REQUEST:
                        currentFasilitasImageUri = imageUri;
                        imagePreviewFasilitas.setImageURI(imageUri);
                        imagePreviewFasilitas.setVisibility(View.VISIBLE);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing image result: " + e.getMessage());
                Toast.makeText(this, "Error memproses gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertImageToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);

        // Decode image dengan options untuk mengurangi ukuran
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 800, 800);
        options.inJustDecodeBounds = false;

        inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        if (bitmap == null) {
            throw new IOException("Gagal decode bitmap");
        }

        // Kompresi gambar untuk menghindari ukuran terlalu besar
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Tutup stream
        inputStream.close();
        byteArrayOutputStream.close();

        String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d(TAG, "Image converted to base64, length: " + base64.length());

        return base64;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    private void simpanDataProyek() {
        try {
            // Validasi input
            String namaProyek = editTextNamaProyek.getText().toString().trim();
            String lokasiProyek = editTextLokasiProyek.getText().toString().trim();
            String deskripsiProyek = editTextDeskripsiProyek.getText().toString().trim();

            if (namaProyek.isEmpty()) {
                editTextNamaProyek.setError("Nama proyek harus diisi");
                editTextNamaProyek.requestFocus();
                return;
            }

            if (lokasiProyek.isEmpty()) {
                editTextLokasiProyek.setError("Lokasi proyek harus diisi");
                editTextLokasiProyek.requestFocus();
                return;
            }

            if (deskripsiProyek.isEmpty()) {
                editTextDeskripsiProyek.setError("Deskripsi proyek harus diisi");
                editTextDeskripsiProyek.requestFocus();
                return;
            }

            // Tampilkan loading
            btnSimpan.setEnabled(false);
            btnSimpan.setText("Menyimpan...");

            // Konversi list fasilitas ke JSON
            String fasilitasJson = new Gson().toJson(fasilitasList);

            // DEBUG: Cek apakah gambar benar-benar ada
            Log.d(TAG, "=== DATA YANG AKAN DIKIRIM ===");
            Log.d(TAG, "Nama Proyek: " + namaProyek);
            Log.d(TAG, "Lokasi: " + lokasiProyek);
            Log.d(TAG, "Deskripsi: " + deskripsiProyek);
            Log.d(TAG, "Logo Base64 available: " + (logoBase64 != null));
            Log.d(TAG, "Siteplan Base64 available: " + (siteplanBase64 != null));
            Log.d(TAG, "Jumlah fasilitas: " + fasilitasList.size());

            if (logoBase64 != null) {
                Log.d(TAG, "Logo length: " + logoBase64.length());
                // Hanya log sebagian kecil untuk debug
                if (logoBase64.length() > 100) {
                    Log.d(TAG, "Logo preview: " + logoBase64.substring(0, 100) + "...");
                }
            }

            if (siteplanBase64 != null) {
                Log.d(TAG, "Siteplan length: " + siteplanBase64.length());
                if (siteplanBase64.length() > 100) {
                    Log.d(TAG, "Siteplan preview: " + siteplanBase64.substring(0, 100) + "...");
                }
            }

            // Kirim data ke server
            Call<BasicResponse> call = apiService.addProyek(
                    "addProyek",
                    namaProyek,
                    lokasiProyek,
                    deskripsiProyek,
                    logoBase64 != null ? logoBase64 : "",
                    siteplanBase64 != null ? siteplanBase64 : "",
                    fasilitasJson
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("Simpan");

                    Log.d(TAG, "Response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse basicResponse = response.body();
                        Log.d(TAG, "Response success: " + basicResponse.isSuccess());
                        Log.d(TAG, "Response message: " + basicResponse.getMessage());

                        if (basicResponse.isSuccess()) {
                            Toast.makeText(InputDataProyekActivity.this,
                                    basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            resetForm();
                            new android.os.Handler().postDelayed(
                                    () -> navigateToHome(),
                                    1000
                            );
                        } else {
                            String errorMsg = basicResponse.getMessage();
                            Toast.makeText(InputDataProyekActivity.this,
                                    errorMsg.isEmpty() ? "Gagal menyimpan data" : errorMsg,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    btnSimpan.setEnabled(true);
                    btnSimpan.setText("Simpan");
                    Log.e(TAG, "Network error: " + t.getMessage());
                    Toast.makeText(InputDataProyekActivity.this,
                            "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error saving project data: " + e.getMessage());
            btnSimpan.setEnabled(true);
            btnSimpan.setText("Simpan");
            Toast.makeText(this, "Error menyimpan data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void resetForm() {
        editTextNamaProyek.setText("");
        editTextLokasiProyek.setText("");
        editTextDeskripsiProyek.setText("");
        editTextNamaFasilitas.setText("");

        imagePreviewLogo.setVisibility(View.INVISIBLE);
        imagePreviewSiteplan.setVisibility(View.INVISIBLE);
        imagePreviewFasilitas.setVisibility(View.INVISIBLE);

        recyclerViewFasilitas.setVisibility(View.GONE);
        textListFasilitas.setVisibility(View.GONE);

        fasilitasList.clear();
        fasilitasAdapter.notifyDataSetChanged();

        logoUri = null;
        siteplanUri = null;
        currentFasilitasImageUri = null;
        logoBase64 = null;
        siteplanBase64 = null;
    }

    private void handleErrorResponse(Response<BasicResponse> response) {
        String errorMessage = "Gagal menyimpan data. ";
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Raw error body: " + errorBody);
                try {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    BasicResponse errorResponse = gson.fromJson(errorBody, BasicResponse.class);
                    if (errorResponse != null && !errorResponse.getMessage().isEmpty()) {
                        errorMessage = errorResponse.getMessage();
                    } else {
                        errorMessage += "HTTP " + response.code();
                    }
                } catch (Exception e) {
                    errorMessage += "HTTP " + response.code();
                }
            } else {
                errorMessage += "HTTP " + response.code();
            }
        } catch (Exception e) {
            errorMessage += "HTTP " + response.code();
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(InputDataProyekActivity.this, NewBeranda.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToHome();
    }
}