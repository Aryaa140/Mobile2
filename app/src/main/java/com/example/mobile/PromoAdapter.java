package com.example.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.PromoViewHolder> {

    private Context context;
    private List<Promo> promoList;

    public PromoAdapter(Context context, List<Promo> promoList) {
        this.context = context;
        this.promoList = promoList;
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
    }

    @Override
    public int getItemCount() {
        return promoList.size();
    }

    public class PromoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPromo;

        public PromoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPromo = itemView.findViewById(R.id.imgPromo);
        }

        public void bind(Promo promo) {
            try {
                byte[] decodedBytes = Base64.decode(promo.getGambarBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgPromo.setImageBitmap(bitmap);

                // Optional: Click listener untuk setiap item promo
                itemView.setOnClickListener(v -> {
                    // Bisa ditambahkan aksi ketika promo di klik
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}