package com.example.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FasilitasAdapter extends RecyclerView.Adapter<FasilitasAdapter.ViewHolder> {
    private List<FasilitasItem> fasilitasList;
    private OnFasilitasActionListener listener;
    private boolean isEditMode = false;

    public interface OnFasilitasActionListener {
        void onEditFasilitas(FasilitasItem fasilitas);
        void onDeleteFasilitas(FasilitasItem fasilitas);
        void onViewFasilitas(FasilitasItem fasilitas);
    }

    // Constructor untuk DetailProyekActivity (dengan listener)
    public FasilitasAdapter(List<FasilitasItem> fasilitasList, OnFasilitasActionListener listener) {
        this.fasilitasList = fasilitasList;
        this.listener = listener;
    }

    // Constructor untuk InputDataProyekActivity (tanpa listener)
    public FasilitasAdapter(List<FasilitasItem> fasilitasList) {
        this.fasilitasList = fasilitasList;
        this.listener = null;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fasilitas, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FasilitasItem item = fasilitasList.get(position);

        // DEBUG: Log data fasilitas
        Log.d("FasilitasAdapter", "Position: " + position +
                ", ID: " + item.getIdFasilitas() +
                ", Nama: " + item.getNamaFasilitas());

        // Set nama fasilitas
        holder.textNamaFasilitas.setText(item.getNamaFasilitas());

        // Set gambar
        if (item.getGambarBase64() != null && !item.getGambarBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(item.getGambarBase64(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageFasilitas.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Log.e("FasilitasAdapter", "Error loading image: " + e.getMessage());
                holder.imageFasilitas.setImageResource(R.drawable.ic_placeholder);
            }
        } else {
            holder.imageFasilitas.setImageResource(R.drawable.ic_placeholder);
        }

        // Atur tombol edit/delete
        if (isEditMode && listener != null) {
            holder.btnEditFasilitas.setVisibility(View.VISIBLE);
            holder.btnDeleteFasilitas.setVisibility(View.VISIBLE);

            holder.btnEditFasilitas.setOnClickListener(v -> {
                Log.d("FasilitasAdapter", "Edit clicked - ID: " + item.getIdFasilitas() + ", Name: " + item.getNamaFasilitas());
                if (listener != null) {
                    listener.onEditFasilitas(item);
                }
            });

            holder.btnDeleteFasilitas.setOnClickListener(v -> {
                Log.d("FasilitasAdapter", "Delete clicked - ID: " + item.getIdFasilitas() + ", Name: " + item.getNamaFasilitas());
                if (listener != null) {
                    listener.onDeleteFasilitas(item);
                }
            });

            holder.imageFasilitas.setOnClickListener(null);
        } else {
            holder.btnEditFasilitas.setVisibility(View.GONE);
            holder.btnDeleteFasilitas.setVisibility(View.GONE);

            if (listener != null) {
                holder.imageFasilitas.setOnClickListener(v -> {
                    listener.onViewFasilitas(item);
                });
            } else {
                holder.imageFasilitas.setOnClickListener(null);
            }
        }
    }
    @Override
    public int getItemCount() {
        return fasilitasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageFasilitas;
        public TextView textNamaFasilitas;
        public ImageButton btnEditFasilitas;
        public ImageButton btnDeleteFasilitas;

        public ViewHolder(View view) {
            super(view);
            imageFasilitas = view.findViewById(R.id.imageFasilitas);
            textNamaFasilitas = view.findViewById(R.id.textNamaFasilitas);
            btnEditFasilitas = view.findViewById(R.id.btnEditFasilitas);
            btnDeleteFasilitas = view.findViewById(R.id.btnDeleteFasilitas);
        }
    }
}