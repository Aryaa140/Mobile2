package com.example.mobile;

public class LoginResponse {
    private boolean success;
    private String message;
    private String NIP;
    private String Divisi;
    private String username;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getNIP() { return NIP; }
    public String getDivisi() { return Divisi; }
    public String getUsername() { return username; }
}
