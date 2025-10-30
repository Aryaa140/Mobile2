package com.example.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProyekAdapterDetail extends RecyclerView.Adapter<ProyekAdapterDetail.ProyekViewHolder> {

    private List<Proyek> proyekList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Proyek proyek);
    }

    public ProyekAdapterDetail(List<Proyek> proyekList, OnItemClickListener listener) {
        this.proyekList = proyekList;
        this.listener = listener;
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

    static class ProyekViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageProyek;
        private TextView textNamaProyek;
        private TextView textLokasiProyek;

        public ProyekViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProyek = itemView.findViewById(R.id.imageProyek);
            textNamaProyek = itemView.findViewById(R.id.textNamaProyek);
            textLokasiProyek = itemView.findViewById(R.id.textLokasiProyek);
        }

        public void bind(Proyek proyek, OnItemClickListener listener) {
            textNamaProyek.setText(proyek.getNamaProyek());
            textLokasiProyek.setText(proyek.getLokasiProyek());

            // LOAD LOGO DARI DATABASE - INI YANG PENTING
            if (proyek.getLogoBase64() != null && !proyek.getLogoBase64().isEmpty()) {
                try {
                    Log.d("ProyekAdapter", "Loading logo dari database untuk: " + proyek.getNamaProyek());
                    byte[] decodedString = Base64.decode(proyek.getLogoBase64(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageProyek.setImageBitmap(decodedByte);
                    Log.d("ProyekAdapter", "Logo berhasil di-load dari database");
                } catch (Exception e) {
                    Log.e("ProyekAdapter", "Gagal load logo dari database: " + e.getMessage());
                    imageProyek.setImageResource(R.drawable.quality_riverside);
                }
            } else {
                Log.d("ProyekAdapter", "Tidak ada logo di database untuk: " + proyek.getNamaProyek());
                imageProyek.setImageResource(R.drawable.quality_riverside);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(proyek);
                }
            });
        }
    }
}