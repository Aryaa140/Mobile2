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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {

    private Context context;
    private List<Promo> promoList;
    private OnPromoActionListener actionListener;
    private String userLevel = "Operator";

    public static final int EDIT_PROMO_REQUEST = 1001;

    public interface OnPromoActionListener {
        void onPromoUpdated(int promoId, String updatedImage);
        void onPromoDeleted(String promoTitle, String penginput);
    }

    public PromoAdapter(Context context, List<Promo> promoList) {
        this.context = context;
        this.promoList = promoList;

        // ❌ HAPUS PEMANGGILAN AUTO DELETE DARI CONSTRUCTOR
        // SEKARANG DITANGANI OLEH NewBeranda BACKGROUND SERVICE
        Log.d("PromoAdapter", "Adapter created - Auto Delete handled by NewBeranda service");
    }

    // ✅ METHOD SETTER - TETAP SAMA
    public void setUserLevel(String level) {
        this.userLevel = level != null ? level : "Operator";
        Log.d("PromoAdapter", "User level set to: " + this.userLevel);
    }

    public void setOnPromoActionListener(OnPromoActionListener listener) {
        this.actionListener = listener;
        Log.d("PromoAdapter", "Listener set: " + (listener != null));
    }

    // ✅ METHOD UNTUK REFRESH DATA DARI ACTIVITY
    public void refreshData(List<Promo> newPromoList) {
        this.promoList.clear();
        this.promoList.addAll(newPromoList);
        notifyDataSetChanged();
        Log.d("PromoAdapter", "Data refreshed: " + promoList.size() + " items");
    }

    // 🎯 METHOD-METHOD LAIN TETAP SAMA PERSIS
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

            String kadaluwarsa = promo.getKadaluwarsa();
            if (kadaluwarsa != null && !kadaluwarsa.isEmpty() && !kadaluwarsa.equals("null")) {
                intent.putExtra("PROMO_KADALUWARSA", kadaluwarsa);
            } else {
                intent.putExtra("PROMO_KADALUWARSA", "");
            }

            Log.d("PromoAdapter", "Data ke EditActivity - ID: " + promo.getIdPromo() + ", Kadaluwarsa: " + kadaluwarsa);

            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).startActivityForResult(intent, EDIT_PROMO_REQUEST);
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

        } catch (Exception e) {
            Log.e("PromoAdapter", "Error opening edit activity: " + e.getMessage());
            showErrorNotification("Gagal membuka halaman edit");
        }
    }

    public void updatePromoItem(int promoId, String updatedImage) {
        Log.d("PromoAdapter", "=== UPDATE PROMO ITEM ===");
        Log.d("PromoAdapter", "Target ID: " + promoId);

        for (int i = 0; i < promoList.size(); i++) {
            Promo promo = promoList.get(i);
            if (promo.getIdPromo() == promoId) {
                Log.d("PromoAdapter", "Found promo at position: " + i);

                if (updatedImage != null && !updatedImage.isEmpty()) {
                    promo.setGambarBase64(updatedImage);
                    Log.d("PromoAdapter", "Image updated for promo: " + promo.getNamaPromo());
                }

                notifyItemChanged(i);
                showUpdateSuccessNotification(promo.getNamaPromo(), promo.getNamaPenginput());
                break;
            }
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
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing delete dialog: " + e.getMessage());
            showErrorNotification("Error menampilkan dialog");
        }
    }

    private void deletePromo(Promo promo, int position) {
        Log.d("PromoAdapter", "Deleting promo with ID: " + promo.getIdPromo());

        if (position < 0 || position >= promoList.size()) {
            Log.e("PromoAdapter", "Invalid position for delete: " + position);
            showErrorNotification("Posisi promo tidak valid");
            return;
        }

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
            }
        });
    }

    private void handleDeleteSuccess(Promo promo, int position) {
        if (position >= 0 && position < promoList.size()) {
            String promoName = getSafePromoName(promo);
            String penginput = promo.getNamaPenginput() != null ? promo.getNamaPenginput() : "Unknown";

            promoList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, promoList.size());

            showDeleteSuccessNotification(promoName, penginput);

            if (actionListener != null) {
                actionListener.onPromoDeleted(promoName, penginput);
            }

            Log.d("PromoAdapter", "✅ Promo deleted successfully: " + promoName);
            savePromoDeleteToHistori(promo.getIdPromo(), promoName, penginput, promo.getGambarBase64());
        }
    }

    private void savePromoDeleteToHistori(int promoId, String title, String penginput, String imageData) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.deletePromoHistori(
                "delete_promo_histori",
                promoId,
                title,
                penginput,
                imageData
        );

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        Log.d("PromoAdapter", "✅ Histori delete berhasil disimpan");
                    } else {
                        Log.e("PromoAdapter", "❌ Gagal menyimpan histori delete: " + basicResponse.getMessage());
                    }
                } else {
                    Log.e("PromoAdapter", "❌ Error response histori delete: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.e("PromoAdapter", "❌ Error menyimpan histori delete: " + t.getMessage());
            }
        });
    }

    private void handleDeleteError(String errorMessage) {
        Log.e("PromoAdapter", "Delete failed: " + errorMessage);
        showErrorNotification(errorMessage);
    }

    private void showUpdateSuccessNotification(String promoName, String updatedBy) {
        try {
            String title = "✅ Promo Diupdate";
            String message = "Promo '" + promoName + "' berhasil diupdate";

            if (updatedBy != null && !updatedBy.isEmpty()) {
                message += " oleh " + updatedBy;
            }

            NotificationHelper.showSimpleNotification(context, title, message);
        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing update notification: " + e.getMessage());
        }
    }

    private void showDeleteSuccessNotification(String promoName, String deletedBy) {
        try {
            String title = "🗑️ Promo Dihapus";
            String message = "Promo '" + promoName + "' berhasil dihapus";

            if (deletedBy != null && !deletedBy.isEmpty()) {
                message += " oleh " + deletedBy;
            }

            NotificationHelper.showSimpleNotification(context, title, message);
        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing delete notification: " + e.getMessage());
        }
    }

    private void showErrorNotification(String message) {
        try {
            NotificationHelper.showSimpleNotification(context, "❌ Error", message);
        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing error notification: " + e.getMessage());
        }
    }

    private void showLoadingState(boolean isLoading) {
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
            imgPromo.setImageResource(R.drawable.ic_placeholder);

            if (base64Image == null || base64Image.trim().isEmpty()) {
                return;
            }

            try {
                String cleanBase64 = base64Image.trim();

                if (cleanBase64.length() < 100) {
                    return;
                }

                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                if (decodedBytes == null || decodedBytes.length == 0) {
                    return;
                }

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (bitmap != null) {
                    imgPromo.setImageBitmap(bitmap);
                }

            } catch (Exception e) {
                Log.e("PromoViewHolder", "Error loading image: " + e.getMessage());
            }
        }
    }
}