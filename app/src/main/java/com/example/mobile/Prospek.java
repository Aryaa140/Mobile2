package com.example.mobile;

public class Prospek {
    private int prospekId;
    private String penginput;
    private String nama;
    private String email;
    private String noHp;
    private String alamat;
    private String referensi;
    private String statusNpwp; // TAMBAHAN
    private String statusBpjs; // TAMBAHAN
    private String tanggalBuat;

    // Constructor
    public Prospek() {
        // Constructor kosong
    }

    // Constructor lama untuk backward compatibility
    public Prospek(int prospekId, String penginput, String nama, String email,
                   String noHp, String alamat, String referensi, String tanggalBuat) {
        this(prospekId, penginput, nama, email, noHp, alamat, referensi, "Tidak Ada", "Tidak Ada", tanggalBuat);
    }

    // Constructor baru dengan status NPWP dan BPJS
    public Prospek(int prospekId, String penginput, String nama, String email,
                   String noHp, String alamat, String referensi,
                   String statusNpwp, String statusBpjs, String tanggalBuat) {
        this.prospekId = prospekId;
        this.penginput = penginput;
        this.nama = nama;
        this.email = email;
        this.noHp = noHp;
        this.alamat = alamat;
        this.referensi = referensi;
        this.statusNpwp = statusNpwp;
        this.statusBpjs = statusBpjs;
        this.tanggalBuat = tanggalBuat;
    }

    // Getter dan Setter
    public int getProspekId() { return prospekId; }
    public void setProspekId(int prospekId) { this.prospekId = prospekId; }

    public String getPenginput() { return penginput; }
    public void setPenginput(String penginput) { this.penginput = penginput; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getReferensi() { return referensi; }
    public void setReferensi(String referensi) { this.referensi = referensi; }

    public String getStatusNpwp() { return statusNpwp; } // TAMBAHAN
    public void setStatusNpwp(String statusNpwp) { this.statusNpwp = statusNpwp; } // TAMBAHAN

    public String getStatusBpjs() { return statusBpjs; } // TAMBAHAN
    public void setStatusBpjs(String statusBpjs) { this.statusBpjs = statusBpjs; } // TAMBAHAN

    public String getTanggalBuat() { return tanggalBuat; }
    public void setTanggalBuat(String tanggalBuat) { this.tanggalBuat = tanggalBuat; }
}