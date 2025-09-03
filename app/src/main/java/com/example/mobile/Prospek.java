package com.example.mobile;

public class Prospek {
    private int prospekId;
    private String nama;
    private String email;
    private String noHp;
    private String alamat;
    private String referensi;

    public Prospek(int prospekId, String nama, String email, String noHp, String alamat, String referensi) {
        this.prospekId = prospekId;
        this.nama = nama;
        this.email = email;
        this.noHp = noHp;
        this.alamat = alamat;
        this.referensi = referensi;
    }

    public int getProspekId() { return prospekId; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getNoHp() { return noHp; }
    public String getAlamat() { return alamat; }
    public String getReferensi() { return referensi; }
}
