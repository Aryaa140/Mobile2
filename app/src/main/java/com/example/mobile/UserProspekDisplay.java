package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class UserProspekDisplay {
    @SerializedName("Id_UserProspek")
    private int Id_UserProspek;

    @SerializedName("Nama_User")
    private String Nama_User;

    @SerializedName("Nama_Penginput")
    private String Nama_Penginput;

    @SerializedName("Email")
    private String Email;

    @SerializedName("No_HP")
    private String No_HP;

    @SerializedName("Alamat")
    private String Alamat;

    @SerializedName("Proyek")
    private String Proyek;

    @SerializedName("Hunian")
    private String Hunian;

    @SerializedName("DP")
    private int DP;

    @SerializedName("Status_BPJS")
    private String Status_BPJS;

    @SerializedName("Status_NPWP")
    private String Status_NPWP;

    @SerializedName("Tanggal_Input")
    private String Tanggal_Input;

    // Getter methods - HARUS SAMA DENGAN FIELD NAMES
    public int getIdUserProspek() { return Id_UserProspek; }
    public String getNamaUser() { return Nama_User; }
    public String getNamaPenginput() { return Nama_Penginput; }
    public String getEmail() { return Email; }
    public String getNoHp() { return No_HP; }
    public String getAlamat() { return Alamat; }
    public String getProyek() { return Proyek; }
    public String getHunian() { return Hunian; }
    public int getDp() { return DP; }
    public String getStatusBpjs() { return Status_BPJS; }
    public String getStatusNpwp() { return Status_NPWP; }
    public String getTanggalInput() { return Tanggal_Input; }
}