package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {

    private Context context;
    private List<Promo> promoList;
    private OnPromoActionListener actionListener;
    private String userLevel = "Operator"; // Default ke Operator

    public static final int EDIT_PROMO_REQUEST = 1001;

    public interface OnPromoActionListener {
        void onPromoUpdated(int promoId, String updatedImage);
        void onPromoDeleted(String promoTitle, String penginput);
    }

    public PromoAdapter(Context context, List<Promo> promoList) {
        this.context = context;
        this.promoList = promoList;
    }

    // METHOD BARU: Set user level
    public void setUserLevel(String level) {
        this.userLevel = level != null ? level : "Operator";
        Log.d("PromoAdapter", "User level set to: " + this.userLevel);
    }

    public void setOnPromoActionListener(OnPromoActionListener listener) {
        this.actionListener = listener;
        Log.d("PromoAdapter", "Listener set: " + (listener != null));
    }

    @NonNull
    @Override
    public PromoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_promo_card, parent, false);
        return new PromoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoViewHolder holder, int position) {
        Promo promo = promoList.get(position);
        holder.bind(promo);

        // ATUR VISIBILITAS TOMBOL MENU BERDASARKAN LEVEL USER
        if ("Admin".equals(userLevel)) {
            holder.btnMenu.setVisibility(View.VISIBLE);
            holder.btnMenu.setOnClickListener(v -> {
                showPopupMenu(v, promo, holder.getAdapterPosition());
            });
        } else {
            holder.btnMenu.setVisibility(View.GONE);
        }
    }

    private void showPopupMenu(View view, Promo promo, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_promo_item, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_edit) {
                openEditActivity(promo);
                return true;
            } else if (id == R.id.menu_delete) {
                showDeleteConfirmationDialog(promo, position);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void openEditActivity(Promo promo) {
        Log.d("PromoAdapter", "Opening EditActivity for promo ID: " + promo.getIdPromo());

        // Validasi data sebelum dikirim
        if (promo == null) {
            Log.e("PromoAdapter", "Promo data is null");
            return;
        }

        try {
            Intent intent = new Intent(context, EditDataPromooActivity.class);

            intent.putExtra("PROMO_ID", promo.getIdPromo());
            intent.putExtra("PROMO_TITLE", promo.getNamaPromo() != null ? promo.getNamaPromo() : "");
            intent.putExtra("PROMO_INPUTTER", promo.getNamaPenginput() != null ? promo.getNamaPenginput() : "");
            intent.putExtra("PROMO_REFERENCE", promo.getReferensiProyek() != null ? promo.getReferensiProyek() : "");
            intent.putExtra("PROMO_IMAGE", promo.getGambarBase64() != null ? promo.getGambarBase64() : "");

            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(intent, EDIT_PROMO_REQUEST);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            Log.d("PromoAdapter", "✅ Edit activity opened successfully");

        } catch (Exception e) {
            Log.e("PromoAdapter", "Error opening edit activity: " + e.getMessage());
            showErrorNotification("Gagal membuka halaman edit");
        }
    }

    public void updatePromoItem(int promoId, String updatedImage) {
        Log.d("PromoAdapter", "=== UPDATE PROMO ITEM ===");
        Log.d("PromoAdapter", "Target ID: " + promoId);

        boolean found = false;

        for (int i = 0; i < promoList.size(); i++) {
            Promo promo = promoList.get(i);
            if (promo.getIdPromo() == promoId) {
                Log.d("PromoAdapter", "Found promo at position: " + i);
                found = true;

                // Update data
                if (updatedImage != null && !updatedImage.isEmpty()) {
                    promo.setGambarBase64(updatedImage);
                    Log.d("PromoAdapter", "Image updated for promo: " + promo.getNamaPromo());
                }

                notifyItemChanged(i);

                // ✅ TAMBAHKAN NOTIFIKASI UPDATE BERHASIL
                showUpdateSuccessNotification(promo.getNamaPromo(), promo.getNamaPenginput());

                break;
            }
        }

        if (!found) {
            Log.w("PromoAdapter", "Promo not found for update: " + promoId);
        }
    }

    private void showDeleteConfirmationDialog(Promo promo, int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Konfirmasi Hapus");
            builder.setMessage("Apakah Anda yakin ingin menghapus promo '" + getSafePromoName(promo) + "'?");

            builder.setPositiveButton("Ya, Hapus", (dialog, which) -> {
                deletePromo(promo, position);
            });

            builder.setNegativeButton("Batal", (dialog, which) -> {
                dialog.dismiss();
                Log.d("PromoAdapter", "Delete operation cancelled");
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            Log.d("PromoAdapter", "Delete confirmation dialog shown");

        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing delete dialog: " + e.getMessage());
            showErrorNotification("Error menampilkan dialog");
        }
    }

    private void deletePromo(Promo promo, int position) {
        Log.d("PromoAdapter", "Deleting promo with ID: " + promo.getIdPromo());

        // Validasi position
        if (position < 0 || position >= promoList.size()) {
            Log.e("PromoAdapter", "Invalid position for delete: " + position);
            showErrorNotification("Posisi promo tidak valid");
            return;
        }

        // Tampilkan loading state
        showLoadingState(true);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.deletePromo(promo.getIdPromo());

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                showLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        handleDeleteSuccess(promo, position);
                    } else {
                        handleDeleteError("Gagal menghapus: " + basicResponse.getMessage());
                    }
                } else {
                    handleDeleteError("Error response dari server: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                showLoadingState(false);
                handleDeleteError("Error koneksi: " + t.getMessage());
                Log.e("PromoAdapter", "Network error: " + t.getMessage());
            }
        });
    }

    private void handleDeleteSuccess(Promo promo, int position) {
        // Validasi ulang position sebelum remove
        if (position >= 0 && position < promoList.size()) {
            String promoName = getSafePromoName(promo);
            String penginput = promo.getNamaPenginput() != null ? promo.getNamaPenginput() : "Unknown";

            // Hapus dari list
            promoList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, promoList.size());

            // ✅ TAMBAHKAN NOTIFIKASI DELETE BERHASIL
            showDeleteSuccessNotification(promoName, penginput);

            // PANGGIL LISTENER UNTUK NEWS DAN NOTIFIKASI
            if (actionListener != null) {
                actionListener.onPromoDeleted(promoName, penginput);
                Log.d("PromoAdapter", "Listener called for delete: " + promoName + " by " + penginput);
            } else {
                Log.w("PromoAdapter", "Action listener is null for delete!");
            }

            Log.d("PromoAdapter", "✅ Promo deleted successfully: " + promoName);
        } else {
            Log.e("PromoAdapter", "Invalid position after delete: " + position);
        }
    }

    private void handleDeleteError(String errorMessage) {
        Log.e("PromoAdapter", "Delete failed: " + errorMessage);
        showErrorNotification(errorMessage);
    }

    // ✅ METHOD BARU: NOTIFIKASI UPDATE BERHASIL
    private void showUpdateSuccessNotification(String promoName, String updatedBy) {
        try {
            String title = "✅ Promo Diupdate";
            String message = "Promo '" + promoName + "' berhasil diupdate";

            if (updatedBy != null && !updatedBy.isEmpty()) {
                message += " oleh " + updatedBy;
            }

            NotificationHelper.showSimpleNotification(context, title, message);
            Log.d("PromoAdapter", "Update notification shown: " + promoName);

        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing update notification: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: NOTIFIKASI DELETE BERHASIL
    private void showDeleteSuccessNotification(String promoName, String deletedBy) {
        try {
            String title = "🗑️ Promo Dihapus";
            String message = "Promo '" + promoName + "' berhasil dihapus";

            if (deletedBy != null && !deletedBy.isEmpty()) {
                message += " oleh " + deletedBy;
            }

            NotificationHelper.showSimpleNotification(context, title, message);
            Log.d("PromoAdapter", "Delete notification shown: " + promoName);

        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing delete notification: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: NOTIFIKASI ERROR
    private void showErrorNotification(String message) {
        try {
            NotificationHelper.showSimpleNotification(context, "❌ Error", message);
            Log.e("PromoAdapter", "Error notification shown: " + message);
        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing error notification: " + e.getMessage());
        }
    }

    // ✅ METHOD BARU: LOADING STATE
    private void showLoadingState(boolean isLoading) {
        // Anda bisa implement loading indicator di sini jika diperlukan
        if (isLoading) {
            Log.d("PromoAdapter", "Loading state: ON");
        } else {
            Log.d("PromoAdapter", "Loading state: OFF");
        }
    }

    @Override
    public int getItemCount() {
        return promoList != null ? promoList.size() : 0;
    }

    // Helper method untuk mendapatkan nama promo yang aman
    private String getSafePromoName(Promo promo) {
        return promo.getNamaPromo() != null ? promo.getNamaPromo() : "Unknown Promo";
    }

    public static class PromoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPromo;
        ImageButton btnMenu;

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPromo = itemView.findViewById(R.id.imgPromo);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(Promo promo) {
            loadImage(promo.getGambarBase64());
        }

        private void loadImage(String base64Image) {
            // Reset ke placeholder dulu
            imgPromo.setImageResource(R.drawable.ic_placeholder);

            if (base64Image == null || base64Image.trim().isEmpty()) {
                Log.d("PromoViewHolder", "Base64 image is null or empty");
                return;
            }

            try {
                String cleanBase64 = base64Image.trim();

                if (cleanBase64.length() < 100) {
                    Log.w("PromoViewHolder", "Base64 too short: " + cleanBase64.length());
                    return;
                }

                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                if (decodedBytes == null || decodedBytes.length == 0) {
                    Log.w("PromoViewHolder", "Decoded bytes are empty");
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (bitmap != null) {
                    imgPromo.setImageBitmap(bitmap);
                    Log.d("PromoViewHolder", "✅ Image loaded successfully");
                } else {
                    Log.w("PromoViewHolder", "Failed to decode bitmap");
                }

            } catch (Exception e) {
                Log.e("PromoViewHolder", "Error loading image: " + e.getMessage());
            }
        }
    }
}