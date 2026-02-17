package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class HistoriStatusLib {
    @SerializedName("Id_HistoriLib")
    private int idHistoriLib;

    @SerializedName("Perubahan Status")
    private String perubahanStatus;

    @SerializedName("Tanggal_Perubahan")
    private String tanggalPerubahan;

    @SerializedName("Id_Prospek")
    private int idProspek;

    @SerializedName("Nama_prospek")
    private String namaProspek;

    @SerializedName("Nama_Penginput")
    private String namaPenginput;

    // Getter dan Setter
    public int getIdHistoriLib() { return idHistoriLib; }
    public void setIdHistoriLib(int idHistoriLib) { this.idHistoriLib = idHistoriLib; }

    public String getPerubahanStatus() { return perubahanStatus; }
    public void setPerubahanStatus(String perubahanStatus) { this.perubahanStatus = perubahanStatus; }

    public String getTanggalPerubahan() { return tanggalPerubahan; }
    public void setTanggalPerubahan(String tanggalPerubahan) { this.tanggalPerubahan = tanggalPerubahan; }

    public int getIdProspek() { return idProspek; }
    public void setIdProspek(int idProspek) { this.idProspek = idProspek; }

    public String getNamaProspek() { return namaProspek; }
    public void setNamaProspek(String namaProspek) { this.namaProspek = namaProspek; }

    public String getNamaPenginput() { return namaPenginput; }
    public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }
}