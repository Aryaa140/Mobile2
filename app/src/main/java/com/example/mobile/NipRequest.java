package com.example.mobile;

public class NipRequest {
    private String no_nip;
    private String date_out;
    private String username;
    private String password;
    private String email;
    private String level;   // prefix code
    private String divisi;  // full division name

    public NipRequest(String no_nip, String date_out) {
        this.no_nip = no_nip;
        this.date_out = date_out;
    }

    public NipRequest(String no_nip, String date_out, String divisi) {
        this.no_nip = no_nip;
        this.date_out = date_out;
        this.divisi = divisi;
    }

    public NipRequest(String no_nip, String date_out, String username, String password, String email, String level, String divisi) {
        this.no_nip = no_nip;
        this.date_out = date_out;
        this.username = username;
        this.password = password;
        this.email = email;
        this.level = level;
        this.divisi = divisi;
    }

    // Getters and setters
    public String getNo_nip() { return no_nip; }
    public void setNo_nip(String no_nip) { this.no_nip = no_nip; }

    public String getDate_out() { return date_out; }
    public void setDate_out(String date_out) { this.date_out = date_out; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDivisi() { return divisi; }
    public void setDivisi(String divisi) { this.divisi = divisi; }
}