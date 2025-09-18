package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class Prospek2 {
    @SerializedName("id_prospek")
    private int idProspek;

    @SerializedName("nama_prospek")
    private String namaProspek;

    @SerializedName("nama_penginput")
    private String namaPenginput;

    @SerializedName("alamat")
    private String alamat;

    @SerializedName("email")
    private String email;

    @SerializedName("no_hp")
    private String noHp;

    @SerializedName("referensi_proyek")
    private String referensiProyek;

    @SerializedName("status_npwp")
    private String statusNpwp;

    @SerializedName("status_bpjs")
    private String statusBpjs;

    @SerializedName("tanggal_input")
    private String tanggalInput;

    // Getter methods
    public int getIdProspek() { return idProspek; }
    public String getNamaProspek() { return namaProspek; }
    public String getNamaPenginput() { return namaPenginput; }
    public String getEmail() { return email; }
    public String getNoHp() { return noHp; }
    public String getAlamat() { return alamat; }
    public String getReferensiProyek() { return referensiProyek; }
    public String getStatusNpwp() { return statusNpwp; }
    public String getStatusBpjs() { return statusBpjs; }
    public String getTanggalInput() { return tanggalInput; }

    // Setter methods (jika diperlukan)
    public void setIdProspek(int idProspek) { this.idProspek = idProspek; }
    public void setNamaProspek(String namaProspek) { this.namaProspek = namaProspek; }
    public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }
    public void setEmail(String email) { this.email = email; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public void setReferensiProyek(String referensiProyek) { this.referensiProyek = referensiProyek; }
    public void setStatusNpwp(String statusNpwp) { this.statusNpwp = statusNpwp; }
    public void setStatusBpjs(String statusBpjs) { this.statusBpjs = statusBpjs; }
    public void setTanggalInput(String tanggalInput) { this.tanggalInput = tanggalInput; }
}