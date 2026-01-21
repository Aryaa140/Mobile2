package com.example.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsHistoriAdapter extends RecyclerView.Adapter<NewsHistoriAdapter.NewsHistoriViewHolder> {
    private Context context;
    private List<NewsHistoriItem> newsHistoriItems;

    public NewsHistoriAdapter(Context context, List<NewsHistoriItem> newsHistoriItems) {
        this.context = context;
        this.newsHistoriItems = newsHistoriItems;
    }

    @NonNull
    @Override
    public NewsHistoriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsHistoriViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHistoriViewHolder holder, int position) {
        NewsHistoriItem item = newsHistoriItems.get(position);

        // ✅ PERBAIKAN: Pastikan menggunakan TextView yang benar untuk setiap data
        Log.d("NewsHistoriAdapter", "Binding item: " + item.getTitle() + " | Status: " + item.getStatus());

        // Set data ke view dengan ID yang benar
        holder.tvNewsTitle.setText(item.getTitle() != null ? item.getTitle() : "No Title");
        holder.tvPenginput.setText("Oleh: " + (item.getPenginput() != null ? item.getPenginput() : "Unknown"));
        holder.tvStatus.setText("Status: " + (item.getStatus() != null ? item.getStatus() : "Unknown"));

        // ✅ PERBAIKAN: Gunakan method getFormattedTime() untuk timestamp
        holder.tvNewsDate.setText(item.getFormattedTime() != null ? item.getFormattedTime() : "Baru saja");

        // Set background color berdasarkan status
        setCardBackgroundBasedOnType(holder.cardView, item.getType());

        // Tampilkan gambar jika ada
        if (item.getImage_base64() != null && !item.getImage_base64().isEmpty()) {
            loadNewsImage(item.getImage_base64(), holder.imgNews);
            holder.imgNews.setVisibility(View.VISIBLE);
        } else {
            setDefaultImage(holder.imgNews);
            holder.imgNews.setVisibility(View.VISIBLE);
        }
    }

    private void setCardBackgroundBasedOnType(CardView cardView, String type) {
        try {
            int backgroundColor;

            switch (type) {
                case "promo_added":
                    backgroundColor = 0xFFE8F5E8; // Light green for added
                    break;
                case "promo_updated":
                    backgroundColor = 0xFFE3F2FD; // Light blue for updated
                    break;
                case "promo_deleted":
                    backgroundColor = 0xFFFFEBEE; // Light red for deleted
                    break;
                case "promo_expired": // ✅ TAMBAHKAN UNTUK KADALUWARSA
                    backgroundColor = 0xFFFFF3CD; // Light yellow for expired
                    break;
                default:
                    backgroundColor = 0xFFFFFFFF; // White for unknown
            }

            cardView.setCardBackgroundColor(backgroundColor);
        } catch (Exception e) {
            Log.e("NewsHistoriAdapter", "Error setting card background: " + e.getMessage());
            cardView.setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    private void loadNewsImage(String imageData, ImageView imageView) {
        if (imageData == null || imageData.trim().isEmpty()) {
            setDefaultImage(imageView);
            return;
        }

        new Thread(() -> {
            try {
                String cleanBase64 = imageData.trim();

                if (cleanBase64.length() < 100) {
                    Log.w("NewsHistoriAdapter", "Base64 too short: " + cleanBase64.length());
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                    return;
                }

                byte[] decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT);

                if (decodedBytes == null || decodedBytes.length == 0) {
                    Log.w("NewsHistoriAdapter", "Decoded bytes are empty");
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                    return;
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, 200, 200);
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                if (bitmap != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setVisibility(View.VISIBLE);
                    });
                } else {
                    Log.w("NewsHistoriAdapter", "Failed to decode bitmap");
                    new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
                }

            } catch (Exception e) {
                Log.e("NewsHistoriAdapter", "Error loading image: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> setDefaultImage(imageView));
            }
        }).start();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void setDefaultImage(ImageView imageView) {
        new Handler(Looper.getMainLooper()).post(() -> {
            imageView.setImageResource(R.drawable.ic_placeholder);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return newsHistoriItems != null ? newsHistoriItems.size() : 0;
    }

    public void updateData(List<NewsHistoriItem> newItems) {
        this.newsHistoriItems = newItems;
        notifyDataSetChanged();
        Log.d("NewsHistoriAdapter", "Data updated: " + newItems.size() + " items");
    }

    public static class NewsHistoriViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNews;
        TextView tvNewsTitle, tvPenginput, tvStatus, tvNewsDate;
        CardView cardView;

        public NewsHistoriViewHolder(@NonNull View itemView) {
            super(itemView);

            // ✅ PERBAIKAN: Pastikan ID TextView sesuai dengan layout item_news_card.xml
            imgNews = itemView.findViewById(R.id.imgNews);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNewsDate = itemView.findViewById(R.id.tvTimestamp); // ✅ PERBAIKAN: Pastikan ID ini benar
            cardView = itemView.findViewById(R.id.cardView);

            Log.d("NewsHistoriAdapter", "ViewHolder initialized with correct IDs");
        }
    }
}