package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class HistoriUserProspek {
    @SerializedName("Id_Histori")
    private int idHistori;

    @SerializedName("Id_UserProspek")
    private int idUserProspek;

    @SerializedName("Nama_User")
    private String namaUser;

    @SerializedName("Nama_Penginput")
    private String namaPenginput;

    @SerializedName("Email")
    private String email;

    @SerializedName("No_HP")
    private String noHp;

    @SerializedName("Alamat")
    private String alamat;

    @SerializedName("Proyek")
    private String proyek;

    @SerializedName("Hunian")
    private String hunian;

    @SerializedName("Tipe_Hunian")
    private String tipeHunian;

    @SerializedName("DP")
    private int dp;

    @SerializedName("Status_BPJS")
    private String statusBpjs;

    @SerializedName("Status_NPWP")
    private String statusNpwp;

    @SerializedName("Tanggal_Perubahan")
    private String tanggalPerubahan;

    @SerializedName("Tipe_Perubahan")
    private String tipePerubahan;

    // Getters and setters
    public int getIdHistori() { return idHistori; }
    public void setIdHistori(int idHistori) { this.idHistori = idHistori; }

    public int getIdUserProspek() { return idUserProspek; }
    public void setIdUserProspek(int idUserProspek) { this.idUserProspek = idUserProspek; }

    public String getNamaUser() { return namaUser; }
    public void setNamaUser(String namaUser) { this.namaUser = namaUser; }

    public String getNamaPenginput() { return namaPenginput; }
    public void setNamaPenginput(String namaPenginput) { this.namaPenginput = namaPenginput; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getProyek() { return proyek; }
    public void setProyek(String proyek) { this.proyek = proyek; }

    public String getHunian() { return hunian; }
    public void setHunian(String hunian) { this.hunian = hunian; }

    public String getTipeHunian() { return tipeHunian; }
    public void setTipeHunian(String tipeHunian) { this.tipeHunian = tipeHunian; }

    public int getDp() { return dp; }
    public void setDp(int dp) { this.dp = dp; }

    public String getStatusBpjs() { return statusBpjs; }
    public void setStatusBpjs(String statusBpjs) { this.statusBpjs = statusBpjs; }

    public String getStatusNpwp() { return statusNpwp; }
    public void setStatusNpwp(String statusNpwp) { this.statusNpwp = statusNpwp; }

    public String getTanggalPerubahan() { return tanggalPerubahan; }
    public void setTanggalPerubahan(String tanggalPerubahan) { this.tanggalPerubahan = tanggalPerubahan; }

    public String getTipePerubahan() { return tipePerubahan; }
    public void setTipePerubahan(String tipePerubahan) { this.tipePerubahan = tipePerubahan; }
}