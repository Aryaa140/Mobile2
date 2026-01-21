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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private List<NewsItem> newsItems;

    public NewsAdapter(Context context, List<NewsItem> newsItems) {
        this.context = context;
        this.newsItems = newsItems;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    // DI NewsAdapter.java - perbaiki onBindViewHolder()
    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsItems.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvStatus.setText(item.getStatus());
        holder.tvPenginput.setText("Oleh: " + item.getPenginput());
        holder.tvTime.setText(item.getFormattedTimestamp());

        // Tampilkan badge untuk jenis item
        String itemType = item.getItemType();
        if (itemType != null) {
            holder.tvItemType.setVisibility(View.VISIBLE);
            switch (itemType) {
                case "promo":
                    holder.tvItemType.setText("PROMO");
                    holder.tvItemType.setBackgroundResource(R.drawable.badge_promo);
                    break;
                case "hunian":
                    holder.tvItemType.setText("HUNIAN");
                    holder.tvItemType.setBackgroundResource(R.drawable.badge_hunian);
                    break;
                case "proyek":
                    holder.tvItemType.setText("PROYEK");
                    holder.tvItemType.setBackgroundResource(R.drawable.badge_proyek);
                    break;
                default:
                    holder.tvItemType.setVisibility(View.GONE);
            }
        } else {
            holder.tvItemType.setVisibility(View.GONE);
        }

        // ✅ PERBAIKAN: Validasi gambar dengan cara yang lebih baik
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            String imageData = item.getImageUrl().trim();
            itemType = item.getItemType();
            boolean shouldLoadImage = false;

            switch (itemType) {
                case "hunian":
                    // Untuk hunian: minimal 50 karakter (bukan 500)
                    shouldLoadImage = imageData.length() >= 50 &&
                            !imageData.equalsIgnoreCase("null") &&
                            !imageData.startsWith("data:") &&
                            !imageData.endsWith("...") &&
                            !imageData.endsWith("..") &&
                            !imageData.contains("undefined");
                    break;

                // ✅ PERBAIKAN: Di method onBindViewHolder untuk proyek
                case "proyek":
                    // Untuk proyek: kriteria lebih longgar
                    shouldLoadImage = imageData.length() >= 50 &&  // Minimal 50 karakter
                            !imageData.equalsIgnoreCase("null") &&
                            !imageData.equalsIgnoreCase("NULL") &&
                            !imageData.startsWith("data:") &&
                            !imageData.endsWith("...") &&
                            !imageData.endsWith("..");
                    break;

                case "promo":
                    // Untuk promo: minimal 100 karakter
                    shouldLoadImage = imageData.length() >= 100 &&
                            !imageData.equalsIgnoreCase("null") &&
                            !imageData.startsWith("data:") &&
                            !imageData.endsWith("...") &&
                            !imageData.endsWith("..") &&
                            !imageData.contains("undefined") &&
                            imageData.matches("^[A-Za-z0-9+/]*={0,2}$");
                    break;

                default:
                    shouldLoadImage = imageData.length() >= 50 &&
                            !imageData.equalsIgnoreCase("null");
            }

            // ✅ DEBUG LOG untuk troubleshooting
            Log.d("NewsAdapter", "🖼️ Image check - Type: " + itemType +
                    ", Length: " + imageData.length() +
                    ", Should load: " + shouldLoadImage +
                    ", Title: " + item.getTitle());

            if (shouldLoadImage) {
                // ✅ PERBAIKAN: Gunakan background thread untuk decode
                String finalItemType = itemType;
                new Thread(() -> {
                    try {
                        // Validasi base64 sebelum decode
                        if (!isValidBase64(imageData)) {
                            Log.w("NewsAdapter", "❌ Invalid base64 for " + finalItemType + ": " + item.getTitle());
                            holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                            return;
                        }

                        byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);

                        if (decodedString == null || decodedString.length == 0) {
                            Log.w("NewsAdapter", "❌ Decoded bytes empty for " + finalItemType + ": " + item.getTitle());
                            holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                            return;
                        }

                        // Cek dimensi gambar tanpa decode penuh
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

                        if (options.outWidth <= 0 || options.outHeight <= 0) {
                            Log.w("NewsAdapter", "❌ Invalid image dimensions for " + finalItemType + ": " + item.getTitle());
                            holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                            return;
                        }

                        // Decode dengan sample size untuk menghemat memory
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = calculateOptimalSampleSize(options, 400, 300);

                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(
                                decodedString, 0, decodedString.length, options);

                        if (decodedBitmap != null) {
                            holder.imgNews.post(() -> {
                                holder.imgNews.setImageBitmap(decodedBitmap);
                                holder.imgNews.setVisibility(View.VISIBLE);
                                Log.d("NewsAdapter", "✅ Image loaded for " + finalItemType + ": " + item.getTitle() +
                                        " | Size: " + decodedBitmap.getWidth() + "x" + decodedBitmap.getHeight());
                            });
                        } else {
                            holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                            Log.w("NewsAdapter", "❌ Bitmap is null after decode for " + finalItemType + ": " + item.getTitle());
                        }

                    } catch (IllegalArgumentException e) {
                        Log.e("NewsAdapter", "❌ Base64 decode error for " + finalItemType + ": " +
                                item.getTitle() + " | Error: " + e.getMessage());
                        holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                    } catch (OutOfMemoryError e) {
                        Log.e("NewsAdapter", "❌ Out of memory loading image for " + finalItemType + ": " + item.getTitle());
                        holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                    } catch (Exception e) {
                        Log.e("NewsAdapter", "❌ Error loading image for " + finalItemType + ": " +
                                item.getTitle() + " | Error: " + e.getMessage());
                        holder.imgNews.post(() -> setDefaultImage(holder.imgNews, finalItemType));
                    }
                }).start();
            } else {
                // Gambar tidak valid
                setDefaultImage(holder.imgNews, itemType);
                Log.d("NewsAdapter", "📭 No valid image for " + itemType + ": " + item.getTitle() +
                        " | Length: " + imageData.length() + " chars");
            }
        } else {
            // Tidak ada gambar URL
            setDefaultImage(holder.imgNews, item.getItemType());
            Log.d("NewsAdapter", "📭 No image URL for " + item.getItemType() + ": " + item.getTitle());
        }

        // Tampilkan kadaluwarsa hanya untuk promo
        if ("promo".equals(itemType) && item.getKadaluwarsa() != null && !item.getKadaluwarsa().isEmpty()) {
            holder.tvKadaluwarsa.setVisibility(View.VISIBLE);
            holder.tvKadaluwarsa.setText("Kadaluwarsa: " + item.getFormattedKadaluwarsa());
        } else {
            holder.tvKadaluwarsa.setVisibility(View.GONE);
        }
    }

    // ✅ TAMBAHKAN METHOD INI DI NewsAdapter
    private boolean isValidBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return false;

        try {
            // Cek format base64 dasar
            String cleanBase64 = base64.trim();

            // Jika string terlalu pendek
            if (cleanBase64.length() < 50) return false;

            // Jika mengandung karakter yang tidak valid
            if (cleanBase64.contains("undefined") ||
                    cleanBase64.contains("null") ||
                    cleanBase64.contains("NULL")) {
                return false;
            }

            // Coba decode untuk validasi
            byte[] decoded = Base64.decode(cleanBase64, Base64.DEFAULT);
            return decoded != null && decoded.length > 0;

        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ TAMBAHKAN METHOD setDefaultImage yang sederhana
    private void setDefaultImage(ImageView imageView, String itemType) {
        try {
            // Gunakan placeholder yang sama untuk semua
            imageView.setImageResource(R.drawable.ic_placeholder);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setVisibility(View.VISIBLE);

            // Tambahkan warna latar berbeda berdasarkan tipe (opsional)
            if (itemType != null) {
                switch (itemType) {
                    case "hunian":
                        imageView.setBackgroundColor(0xFFF3E5F5); // Light purple
                        break;
                    case "proyek":
                        imageView.setBackgroundColor(0xFFE0F2F1); // Light teal
                        break;
                    case "promo":
                        imageView.setBackgroundColor(0xFFE8F5E8); // Light green
                        break;
                }
            }

        } catch (Exception e) {
            // Fallback sederhana
            imageView.setImageResource(R.drawable.ic_placeholder);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    // ✅ Method calculateOptimalSampleSize (harus sudah ada)
    private int calculateOptimalSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    // PERBAIKAN: Method setCardBackgroundBasedOnStatus dengan type
    private void setCardBackgroundBasedOnStatus(CardView cardView, String status, String itemType) {
        try {
            int backgroundColor;

            // Warna dasar berdasarkan type
            switch (itemType) {
                case "promo":
                    backgroundColor = getPromoBackgroundColor(status);
                    break;
                case "hunian":
                    backgroundColor = getHunianBackgroundColor(status);
                    break;
                case "proyek":
                    backgroundColor = getProyekBackgroundColor(status);
                    break;
                default:
                    backgroundColor = 0xFFF5F5F5; // Light gray
            }

            cardView.setCardBackgroundColor(backgroundColor);
        } catch (Exception e) {
            Log.e("NewsAdapter", "Error setting card background: " + e.getMessage());
            cardView.setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    private int getPromoBackgroundColor(String status) {
        switch (status) {
            case "Ditambahkan":
                return 0xFFE8F5E8; // Light green
            case "Diubah":
                return 0xFFE3F2FD; // Light blue
            case "Dihapus":
                return 0xFFFFEBEE; // Light red
            case "Kadaluwarsa":
                return 0xFFFFF3CD; // Light yellow
            default:
                return 0xFFF5F5F5;
        }
    }

    private int getHunianBackgroundColor(String status) {
        switch (status) {
            case "Ditambahkan":
                return 0xFFF3E5F5; // Light purple
            case "Diubah":
                return 0xFFE8EAF6; // Light indigo
            case "Dihapus":
                return 0xFFFFEBEE; // Light red
            default:
                return 0xFFF5F5F5;
        }
    }

    private int getProyekBackgroundColor(String status) {
        switch (status) {
            case "Ditambahkan":
                return 0xFFE0F2F1; // Light teal
            case "Diubah":
                return 0xFFE0F7FA; // Light cyan
            case "Dihapus":
                return 0xFFFFEBEE; // Light red
            default:
                return 0xFFF5F5F5;
        }
    }


    @Override
    public int getItemCount() {
        return newsItems != null ? newsItems.size() : 0;
    }

    public void updateData(List<NewsItem> newItems) {
        this.newsItems = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < newsItems.size()) {
            newsItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvPenginput, tvTime, tvKadaluwarsa, tvItemType;
        ImageView imgNews;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPenginput = itemView.findViewById(R.id.tvPenginput);
            tvTime = itemView.findViewById(R.id.tvTimestamp);
            tvKadaluwarsa = itemView.findViewById(R.id.tvKadaluwarsa);
            tvItemType = itemView.findViewById(R.id.tvItemType); // Tambahkan ini
            imgNews = itemView.findViewById(R.id.imgNews);
        }
    }
}