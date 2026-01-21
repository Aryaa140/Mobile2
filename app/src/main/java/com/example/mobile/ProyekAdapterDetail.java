package com.example.mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProyekAdapterDetail extends RecyclerView.Adapter<ProyekAdapterDetail.ProyekViewHolder> {

    private static final String TAG = "ProyekAdapterDetail";
    private List<Proyek> proyekList;
    private OnItemClickListener listener;
    private Context context;
    private ApiService apiService;
    private String userLevel;
    private SharedPreferences sharedPreferences;

    public interface OnItemClickListener {
        void onItemClick(Proyek proyek);
        void onProyekDeleted(); // Callback untuk refresh data setelah delete
    }


    public ProyekAdapterDetail(List<Proyek> proyekList, OnItemClickListener listener, Context context) {
        this.proyekList = proyekList;
        this.listener = listener;
        this.context = context;
        this.apiService = RetrofitClient.getClient().create(ApiService.class);

        // Ambil level user dari SharedPreferences
        this.sharedPreferences = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        this.userLevel = sharedPreferences.getString("level", "Operator");

        Log.d(TAG, "User Level: " + userLevel);
    }

    @NonNull
    @Override
    public ProyekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proyek_detail, parent, false);
        return new ProyekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProyekViewHolder holder, int position) {
        Proyek proyek = proyekList.get(position);
        holder.bind(proyek, listener);
    }

    @Override
    public int getItemCount() {
        return proyekList.size();
    }

    class ProyekViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProyek;
        private TextView textNamaProyek;
        private TextView textLokasiProyek;
        private FloatingActionButton btnDeleteProyek;

        public ProyekViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProyek = itemView.findViewById(R.id.imageProyek);
            textNamaProyek = itemView.findViewById(R.id.textNamaProyek);
            textLokasiProyek = itemView.findViewById(R.id.textLokasiProyek);
            btnDeleteProyek = itemView.findViewById(R.id.btnDeleteProyek);
        }

        public void bind(Proyek proyek, OnItemClickListener listener) {
            textNamaProyek.setText(proyek.getNamaProyek());
            textLokasiProyek.setText(proyek.getLokasiProyek());

            // Tampilkan atau sembunyikan button delete berdasarkan level user
            if ("Admin".equals(userLevel)) {
                btnDeleteProyek.setVisibility(View.VISIBLE);
                btnDeleteProyek.setOnClickListener(v -> showDeleteConfirmation(proyek));
            } else {
                btnDeleteProyek.setVisibility(View.GONE);
            }

            // PERBAIKAN: Load logo dengan method yang lebih robust
            loadProyekLogo(proyek);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(proyek);
                }
            });
        }

        // PERBAIKAN: Method baru untuk load logo dengan error handling yang lebih baik
        private void loadProyekLogo(Proyek proyek) {
            if (proyek.getLogoBase64() != null && !proyek.getLogoBase64().isEmpty()) {
                Log.d(TAG, "Loading logo untuk: " + proyek.getNamaProyek());
                Log.d(TAG, "Logo base64 length: " + proyek.getLogoBase64().length());

                boolean success = setImageFromBase64(proyek.getLogoBase64(), imageProyek, "logo");
                if (!success) {
                    Log.e(TAG, "Gagal load logo dari base64, menggunakan placeholder");
                    imageProyek.setImageResource(R.drawable.quality_riverside);
                }
            } else {
                Log.w(TAG, "Tidak ada logo di database untuk: " + proyek.getNamaProyek());
                imageProyek.setImageResource(R.drawable.quality_riverside);
            }
        }

        // PERBAIKAN: Method helper untuk decode Base64 dengan error handling yang lebih baik
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

                // Try multiple approaches to decode the bitmap

                // Approach 1: Standard decode
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                Log.d(TAG, type + " image dimensions: " + options.outWidth + "x" + options.outHeight);
                Log.d(TAG, type + " image mime type: " + options.outMimeType);

                // Check if the image format is supported
                if (options.outWidth <= 0 || options.outHeight <= 0) {
                    Log.e(TAG, type + " invalid image dimensions, trying alternative approach");

                    // Approach 2: Try without bounds checking
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        Log.d(TAG, type + " successfully decoded with alternative approach");
                        return true;
                    } else {
                        // Approach 3: Try with different config
                        Log.e(TAG, type + " standard decoding failed, trying RGB_565");
                        BitmapFactory.Options options2 = new BitmapFactory.Options();
                        options2.inPreferredConfig = Bitmap.Config.RGB_565;
                        bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options2);

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            Log.d(TAG, type + " successfully decoded with RGB_565");
                            return true;
                        }
                    }

                    Log.e(TAG, type + " all decoding approaches failed");
                    return false;
                }

                // Calculate sample size to avoid memory issues
                options.inSampleSize = calculateInSampleSize(options, 800, 600);
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory

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
                return setImageFromBase64WithLargerSample(base64String, imageView, type);
            } catch (Exception e) {
                Log.e(TAG, type + " Error decoding base64: " + e.getMessage());
                return false;
            }
        }

        // Method untuk handle OutOfMemoryError dengan sample size yang lebih besar
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
                options.inPreferredConfig = Bitmap.Config.RGB_565;

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
                Log.e(TAG, "Invalid base64: " + e.getMessage());
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

        private void showDeleteConfirmation(Proyek proyek) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Hapus Proyek");
            builder.setMessage("Apakah Anda yakin ingin menghapus proyek \"" + proyek.getNamaProyek() + "\"?\n\n" +
                    "Tindakan ini akan menghapus:\n" +
                    "• Data proyek\n" +
                    "• Data kavling terkait\n" +
                    "• Data hunian terkait\n" +
                    "• Data promo terkait\n\n" +
                    "Tindakan ini tidak dapat dibatalkan!");

            builder.setPositiveButton("Hapus", (dialog, which) -> deleteProyek(proyek));
            builder.setNegativeButton("Batal", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void deleteProyek(Proyek proyek) {
            Log.d(TAG, "Menghapus proyek: " + proyek.getNamaProyek() + " (ID: " + proyek.getIdProyek() + ")");

            // ✅ DAPATKAN USER INFO UNTUK NOTIFIKASI
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String username = sharedPreferences.getString("username", "");
            String namaUser = sharedPreferences.getString("nama_user", username);

            Log.d(TAG, "User info - Username: " + username + ", Nama: " + namaUser);

            // ✅ GUNAKAN API DENGAN PARAMETER USER INFO
            Call<BasicResponse> call = apiService.deleteProyek(
                    proyek.getIdProyek(),
                    proyek.getNamaProyek(),
                    username,
                    namaUser
            );

            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BasicResponse deleteResponse = response.body();
                        if (deleteResponse.isSuccess()) {
                            Toast.makeText(context, "Proyek berhasil dihapus", Toast.LENGTH_SHORT).show();

                            // ✅ TAMPILKAN INFO FCM JIKA ADA
                            if (deleteResponse.getFcmNotification() != null) {
                                Log.d(TAG, "FCM Delete Notification Result: " + deleteResponse.getFcmNotification());
                            }

                            // Panggil callback untuk refresh data
                            if (listener != null) {
                                listener.onProyekDeleted();
                            }
                        } else {
                            Toast.makeText(context, "Gagal menghapus: " + deleteResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "Error response dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Delete response error: " + response.code());

                        // ✅ COBA BACA ERROR BODY UNTUK DEBUG
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Delete error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading delete error body: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Toast.makeText(context, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Delete network error: " + t.getMessage());
                }
            });
        }
    }
}