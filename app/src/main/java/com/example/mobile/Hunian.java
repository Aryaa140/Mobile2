package com.example.mobile;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Hunian implements Serializable {
    @SerializedName("id_hunian")
    private int idHunian;

    @SerializedName("nama_hunian")
    private String namaHunian;

    @SerializedName("nama_proyek")
    private String namaProyek;

    // FIELD BARU - TAMBAHAN
    @SerializedName("gambar_unit")
    private String gambarUnit;

    @SerializedName("gambar_denah")
    private String gambarDenah;

    @SerializedName("luas_tanah")
    private int luasTanah;

    @SerializedName("luas_bangunan")
    private int luasBangunan;

    @SerializedName("deskripsi_hunian")
    private String deskripsiHunian;

    @SerializedName("fasilitas")
    private List<FasilitasHunianItem> fasilitas;

    // GETTERS DAN SETTERS
    public int getIdHunian() { return idHunian; }
    public void setIdHunian(int idHunian) { this.idHunian = idHunian; }

    public String getNamaHunian() { return namaHunian; }
    public void setNamaHunian(String namaHunian) { this.namaHunian = namaHunian; }

    public String getNamaProyek() { return namaProyek; }
    public void setNamaProyek(String namaProyek) { this.namaProyek = namaProyek; }

    public String getGambarUnit() { return gambarUnit; }
    public void setGambarUnit(String gambarUnit) { this.gambarUnit = gambarUnit; }

    public String getGambarDenah() { return gambarDenah; }
    public void setGambarDenah(String gambarDenah) { this.gambarDenah = gambarDenah; }

    public int getLuasTanah() { return luasTanah; }
    public void setLuasTanah(int luasTanah) { this.luasTanah = luasTanah; }

    public int getLuasBangunan() { return luasBangunan; }
    public void setLuasBangunan(int luasBangunan) { this.luasBangunan = luasBangunan; }

    public String getDeskripsiHunian() { return deskripsiHunian; }
    public void setDeskripsiHunian(String deskripsiHunian) { this.deskripsiHunian = deskripsiHunian; }

    public List<FasilitasHunianItem> getFasilitas() { return fasilitas; }
    public void setFasilitas(List<FasilitasHunianItem> fasilitas) { this.fasilitas = fasilitas; }
}