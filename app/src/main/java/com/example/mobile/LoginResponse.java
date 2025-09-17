package com.example.mobile;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("NIP")
    private String NIP;

    @SerializedName("Divisi")
    private String Divisi;

    @SerializedName("data")
    private UserData data;

    // Getter dan Setter
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getNIP() {
        return NIP;
    }

    public String getDivisi() {
        return Divisi;
    }

    public UserData getData() {
        return data;
    }

    // Inner class untuk data user jika response menggunakan struktur data
    public static class UserData {
        @SerializedName("user_id")
        private String userId;

        @SerializedName("NIP")
        private String NIP;

        @SerializedName("Divisi")
        private String Divisi;

        @SerializedName("username")
        private String username;

        // Getter dan Setter
        public String getUserId() {
            return userId;
        }

        public String getNIP() {
            return NIP;
        }

        public String getDivisi() {
            return Divisi;
        }

        public String getUsername() {
            return username;
        }
    }
}