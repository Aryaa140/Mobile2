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

    public static final int EDIT_PROMO_REQUEST = 1001;

    public interface OnPromoActionListener {
        void onPromoUpdated(int promoId, String updatedImage);
        void onPromoDeleted(String promoTitle, String penginput);
    }

    public PromoAdapter(Context context, List<Promo> promoList) {
        this.context = context;
        this.promoList = promoList;
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

        holder.btnMenu.setOnClickListener(v -> {
            showPopupMenu(v, promo, holder.getAdapterPosition());
        });
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

        // PERBAIKAN: Validasi data sebelum dikirim
        if (promo == null) {
            Toast.makeText(context, "Data promo tidak valid", Toast.LENGTH_SHORT).show();
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
        } catch (Exception e) {
            Log.e("PromoAdapter", "Error opening edit activity: " + e.getMessage());
            Toast.makeText(context, "Gagal membuka halaman edit", Toast.LENGTH_SHORT).show();
        }
    }

    public void updatePromoItem(int promoId, String updatedImage) {
        Log.d("PromoAdapter", "=== UPDATE PROMO ITEM ===");
        Log.d("PromoAdapter", "Target ID: " + promoId + ", Image: " +
                (updatedImage != null ? updatedImage.length() + " chars" : "null"));

        for (int i = 0; i < promoList.size(); i++) {
            Promo promo = promoList.get(i);
            if (promo.getIdPromo() == promoId) {
                Log.d("PromoAdapter", "Found promo at position: " + i);

                // PERBAIKAN: Only update image if not null
                if (updatedImage != null) {
                    promo.setGambarBase64(updatedImage);
                }

                notifyItemChanged(i);
                break;
            }
        }
    }

    private void showDeleteConfirmationDialog(Promo promo, int position) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Konfirmasi Hapus");
            builder.setMessage("Apakah Anda yakin ingin menghapus promo '" + promo.getNamaPromo() + "'?");

            builder.setPositiveButton("Ya, Hapus", (dialog, which) -> {
                deletePromoDirectly(promo, position);
            });

            builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e("PromoAdapter", "Error showing delete dialog: " + e.getMessage());
        }
    }

    private void deletePromoDirectly(Promo promo, int position) {
        Log.d("PromoAdapter", "Deleting promo with ID: " + promo.getIdPromo());

        // PERBAIKAN: Validasi position
        if (position < 0 || position >= promoList.size()) {
            Toast.makeText(context, "Posisi promo tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.deletePromo(promo.getIdPromo());

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        // PERBAIKAN: Validasi ulang position sebelum remove
                        if (position >= 0 && position < promoList.size()) {
                            promoList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, promoList.size());

                            Toast.makeText(context, "Promo berhasil dihapus", Toast.LENGTH_SHORT).show();

                            // PANGGIL LISTENER UNTUK NEWS
                            if (actionListener != null) {
                                actionListener.onPromoDeleted(
                                        promo.getNamaPromo() != null ? promo.getNamaPromo() : "Unknown",
                                        promo.getNamaPenginput() != null ? promo.getNamaPenginput() : "Unknown"
                                );
                            } else {
                                Log.w("PromoAdapter", "Action listener is null!");
                            }
                        } else {
                            Toast.makeText(context, "Posisi tidak valid setelah penghapusan", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Gagal menghapus: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error response dari server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(context, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("PromoAdapter", "Network error: " + t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return promoList != null ? promoList.size() : 0;
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