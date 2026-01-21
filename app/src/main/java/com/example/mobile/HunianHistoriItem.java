package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class HunianHistoriItem {
    @SerializedName("id_news_hunian")
    private int idNewsHunian;

    @SerializedName("hunian_id")
    private int hunianId;

    @SerializedName("nama_hunian")
    private String namaHunian;

    @SerializedName("nama_proyek")
    private String namaProyek;

    @SerializedName("luas_tanah")
    private int luasTanah;

    @SerializedName("luas_bangunan")
    private int luasBangunan;

    @SerializedName("penginput")
    private String penginput;

    @SerializedName("status")
    private String status;

    @SerializedName("image_data")
    private String imageData;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("deleted_by")
    private String deletedBy;

    // Getters and Setters
    public int getIdNewsHunian() {
        return idNewsHunian;
    }

    public void setIdNewsHunian(int idNewsHunian) {
        this.idNewsHunian = idNewsHunian;
    }

    public int getHunianId() {
        return hunianId;
    }

    public void setHunianId(int hunianId) {
        this.hunianId = hunianId;
    }

    public String getNamaHunian() {
        return namaHunian != null ? namaHunian : "";
    }

    public void setNamaHunian(String namaHunian) {
        this.namaHunian = namaHunian;
    }

    public String getNamaProyek() {
        return namaProyek != null ? namaProyek : "";
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }

    public int getLuasTanah() {
        return luasTanah;
    }

    public void setLuasTanah(int luasTanah) {
        this.luasTanah = luasTanah;
    }

    public int getLuasBangunan() {
        return luasBangunan;
    }

    public void setLuasBangunan(int luasBangunan) {
        this.luasBangunan = luasBangunan;
    }

    public String getPenginput() {
        return penginput != null ? penginput : "";
    }

    public void setPenginput(String penginput) {
        this.penginput = penginput;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getTimestamp() {
        return timestamp != null ? timestamp : "";
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeletedBy() {
        return deletedBy != null ? deletedBy : "";
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    // Helper method untuk NewsActivity
    public String getTitle() {
        return "Hunian: " + namaHunian + " (Proyek: " + namaProyek + ")";
    }

    public String getType() {
        return "hunian";
    }
}