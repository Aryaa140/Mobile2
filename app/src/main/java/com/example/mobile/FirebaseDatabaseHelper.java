package com.example.mobile;

import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.UUID;

public class FirebaseDatabaseHelper {
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    public FirebaseDatabaseHelper() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference("promo");
            storageReference = FirebaseStorage.getInstance().getReference("promo_images");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Promo {
        private String id;
        private String namaGambar;
        private String referensiProyek;
        private String namaPenginput;
        private String imageUrl;
        private long timestamp;

        public Promo() {}

        public Promo(String namaGambar, String referensiProyek, String namaPenginput, String imageUrl) {
            this.id = UUID.randomUUID().toString();
            this.namaGambar = namaGambar;
            this.referensiProyek = referensiProyek;
            this.namaPenginput = namaPenginput;
            this.imageUrl = imageUrl;
            this.timestamp = System.currentTimeMillis();
        }

        // Getter dan Setter
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNamaGambar() { return namaGambar; }
        public void setNamaGambar(String namaGambar) { this.namaGambar = namaGambar; }
        public String getReferensiProyek() { return referensiProyek; }
        public void setReferensiProyek(String referensiProyek) { this.referensiProyek = referensiProyek; }
        public String getNamaPenginput() { return namaPenginput; }
        public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    public void savePromoWithImage(Uri imageUri, String namaGambar, String referensiProyek,
                                   String namaPenginput, OnSuccessListener<String> onSuccess,
                                   OnFailureListener onFailure) {

        if (imageUri == null) {
            onFailure.onFailure(new Exception("Image URI is null"));
            return;
        }

        String imageFileName = "promo_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileReference = storageReference.child(imageFileName);

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            Promo promo = new Promo(namaGambar, referensiProyek, namaPenginput, uri.toString());
                            savePromoData(promo, onSuccess, onFailure);
                        }).addOnFailureListener(onFailure)
                )
                .addOnFailureListener(onFailure);
    }

    private void savePromoData(Promo promo, OnSuccessListener<String> onSuccess,
                               OnFailureListener onFailure) {
        String promoId = databaseReference.push().getKey();
        if (promoId != null) {
            promo.setId(promoId);
            databaseReference.child(promoId)
                    .setValue(promo)
                    .addOnSuccessListener(aVoid -> onSuccess.onSuccess(promoId))
                    .addOnFailureListener(onFailure);
        }
    }

    public DatabaseReference getPromoReference() {
        return databaseReference;
    }
}