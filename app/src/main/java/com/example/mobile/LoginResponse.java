package com.example.mobile;

public class LoginResponse {
    private boolean success;
    private String message;
    private String NIP;
    private String Divisi;
    private String Level;
    private String date_out; // TAMBAH INI
    private String warning;  // Opsional: untuk warning masa tenggang
    private int days_until_expiry; // Opsional: untuk info hari sampai expired

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNIP() {
        return NIP;
    }

    public void setNIP(String NIP) {
        this.NIP = NIP;
    }

    public String getDivisi() {
        return Divisi;
    }

    public void setDivisi(String divisi) {
        Divisi = divisi;
    }

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        Level = level;
    }

    // TAMBAH GETTER SETTER UNTUK DATE_OUT
    public String getDate_out() {
        return date_out;
    }

    public void setDate_out(String date_out) {
        this.date_out = date_out;
    }

    // Opsional: getter setter untuk warning
    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    // Opsional: getter setter untuk days_until_expiry
    public int getDays_until_expiry() {
        return days_until_expiry;
    }

    public void setDays_until_expiry(int days_until_expiry) {
        this.days_until_expiry = days_until_expiry;
    }
}