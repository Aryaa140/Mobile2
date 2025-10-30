package com.example.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FasilitasItem item = fasilitasList.get(position);
        holder.textView.setText(item.getNamaFasilitas());
    }

    @Override
    public int getItemCount() {
        return fasilitasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
        }
    }
}