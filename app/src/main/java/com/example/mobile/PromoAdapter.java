package com.example.mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

    // Constants untuk request code
    public static final int EDIT_PROMO_REQUEST = 1001;

    public interface OnPromoActionListener {
        void onEditPromo(Promo promo);
        void onDeletePromo(Promo promo);
        void onPromoUpdated(int promoId, String updatedImage); // New method untuk handle update
    }

    public PromoAdapter(Context context, List<Promo> promoList) {
        this.context = context;
        this.promoList = promoList;

        if (context instanceof OnPromoActionListener) {
            this.actionListener = (OnPromoActionListener) context;
        }
    }

    public void setOnPromoActionListener(OnPromoActionListener listener) {
        this.actionListener = listener;
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
            showPopupMenu(v, promo, position);
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

        Intent intent = new Intent(context, EditDataPromoActivity.class);

        // Kirim semua data promo yang diperlukan
        intent.putExtra("PROMO_ID", promo.getIdPromo());
        intent.putExtra("PROMO_TITLE", promo.getNamaPromo());
        intent.putExtra("PROMO_INPUTTER", promo.getNamaPenginput());
        intent.putExtra("PROMO_REFERENCE", promo.getReferensiProyek());
        intent.putExtra("PROMO_IMAGE", promo.getGambarBase64());

        // Jika context adalah Activity, start dengan request code
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).startActivityForResult(intent, EDIT_PROMO_REQUEST);
        } else {
            // Fallback untuk non-Activity context
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    // Method untuk handle update dari activity yang memanggil
    public void updatePromoItem(int promoId, String updatedImage) {
        for (int i = 0; i < promoList.size(); i++) {
            Promo promo = promoList.get(i);
            if (promo.getIdPromo() == promoId) {
                // Update gambar promo
                promo.setGambarBase64(updatedImage);
                notifyItemChanged(i);
                Log.d("PromoAdapter", "Promo ID " + promoId + " image updated");
                break;
            }
        }
    }

    private void showDeleteConfirmationDialog(Promo promo, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Apakah Anda yakin ingin menghapus promo '" + promo.getNamaPromo() + "'?");

        builder.setPositiveButton("Ya, Hapus", (dialog, which) -> {
            if (actionListener != null) {
                actionListener.onDeletePromo(promo);
            } else {
                deletePromo(promo, position);
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePromo(Promo promo, int position) {
        Log.d("PromoAdapter", "Deleting promo with ID: " + promo.getIdPromo());

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<BasicResponse> call = apiService.deletePromo(promo.getIdPromo());

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasicResponse basicResponse = response.body();
                    if (basicResponse.isSuccess()) {
                        promoList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, promoList.size());
                        Toast.makeText(context, "Promo berhasil dihapus", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Gagal: " + basicResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error response dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return promoList.size();
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
            try {
                String base64Image = promo.getGambarBase64();

                if (base64Image != null && !base64Image.trim().isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    if (bitmap != null) {
                        imgPromo.setImageBitmap(bitmap);
                    } else {
                        // Jika decoding gagal, gunakan placeholder default
                        imgPromo.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    // Jika string kosong/null → gunakan placeholder
                    imgPromo.setImageResource(R.drawable.ic_placeholder);
                }
            } catch (Exception e) {
                e.printStackTrace();
                imgPromo.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }
}