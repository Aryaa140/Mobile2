package com.example.mobile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.squareup.picasso.Callback;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private List<NewsItem> newsItems;
    private SimpleDateFormat dateFormat;
    public interface OnItemRemoveListener {
        void onItemRemoved(int position, NewsItem removedItem);
    }

    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsItems.get(position);

        holder.tvNewsTitle.setText(item.getTitle());
        holder.tvPenginput.setText("Oleh: " + item.getPenginput());
        holder.tvStatus.setText("Status: " + item.getStatus());
        holder.tvNewsDate.setText(dateFormat.format(item.getTimestamp()));

        // Handle image - bisa berupa URL atau base64 string
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            String imageData = item.getImageUrl();

            if (imageData.startsWith("http")) {
                // Jika berupa URL, gunakan Picasso
                Picasso.get()
                        .load(imageData)
                        .fit()
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.imgNews);
            } else if (imageData.startsWith("data:image") || imageData.length() > 100) {
                // Jika berupa base64, decode dan set bitmap
                decodeBase64AndSetImage(imageData, holder.imgNews);
            } else {
                // Data tidak valid
                setDefaultImage(holder.imgNews);
            }
        } else {
            setDefaultImage(holder.imgNews);
        }
    }

    private void decodeBase64AndSetImage(String base64String, ImageView imageView) {
        try {
            // Bersihkan base64 string jika mengandung prefix "data:image"
            String base64Image;
            if (base64String.contains(",")) {
                base64Image = base64String.split(",")[1];
            } else {
                base64Image = base64String;
            }

            // Decode base64 to bitmap
            byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                setDefaultImage(imageView);
                Log.e("NewsAdapter", "Failed to decode base64 image");
            }
        } catch (Exception e) {
            setDefaultImage(imageView);
            Log.e("NewsAdapter", "Error decoding base64: " + e.getMessage());
        }
    }

    private void setDefaultImage(ImageView imageView) {
        imageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        imageView.setImageDrawable(null);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public void removeItem(int position) {
        NewsItem removedItem = newsItems.get(position);
        newsItems.remove(position);
        notifyItemRemoved(position);


    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvNewsTitle, tvPenginput, tvStatus, tvNewsDate;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNewsDate = itemView.findViewById(R.id.tvNewsDate);
        }
    }
}