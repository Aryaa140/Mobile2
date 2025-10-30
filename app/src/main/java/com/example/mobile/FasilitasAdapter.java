package com.example.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FasilitasAdapter extends RecyclerView.Adapter<FasilitasAdapter.ViewHolder> {
    private List<FasilitasItem> fasilitasList;

    public FasilitasAdapter(List<FasilitasItem> fasilitasList) {
        this.fasilitasList = fasilitasList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gunakan layout custom yang baru dibuat
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fasilitas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FasilitasItem item = fasilitasList.get(position);
        holder.textNamaFasilitas.setText(item.getNamaFasilitas());

        // Set gambar dari base64
        if (item.getGambarBase64() != null && !item.getGambarBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(item.getGambarBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageFasilitas.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imageFasilitas.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            holder.imageFasilitas.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return fasilitasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageFasilitas;
        public TextView textNamaFasilitas;

        public ViewHolder(View view) {
            super(view);
            imageFasilitas = view.findViewById(R.id.imageFasilitas);
            textNamaFasilitas = view.findViewById(R.id.textNamaFasilitas);
        }
    }
}