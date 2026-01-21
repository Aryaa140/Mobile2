package com.example.mobile;

public class DeletedNewsItem {
    private String itemType; // "promo", "hunian", "proyek"
    private String title;
    private String penginput;
    private String status;
    private String timestamp;
    private String imageData;
    private String referenceId;
    private String kadaluwarsa;
    private int id;

    // Constructor
    public DeletedNewsItem(String itemType, String title, String penginput, String status,
                           String timestamp, String imageData, String referenceId, String kadaluwarsa) {
        this.itemType = itemType;
        this.title = title;
        this.penginput = penginput;
        this.status = status;
        this.timestamp = timestamp;
        this.imageData = imageData;
        this.referenceId = referenceId;
        this.kadaluwarsa = kadaluwarsa;
        this.id = generateId();
    }

    // Getters and Setters
    public String getItemType() { return itemType; }
    public String getTitle() { return title; }
    public String getPenginput() { return penginput; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public String getImageData() { return imageData; }
    public String getReferenceId() { return referenceId; }
    public String getKadaluwarsa() { return kadaluwarsa; }
    public int getId() { return id; }

    private int generateId() {
        return (itemType + title + timestamp).hashCode();
    }
}