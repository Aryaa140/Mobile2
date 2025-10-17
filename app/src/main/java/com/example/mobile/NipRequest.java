package com.example.mobile;

public class NipRequest {
    private String no_nip;
    private String date_out;

    public NipRequest(String no_nip, String date_out) {
        this.no_nip = no_nip;
        this.date_out = date_out;
    }

    public String getNo_nip() {
        return no_nip;
    }

    public void setNo_nip(String no_nip) {
        this.no_nip = no_nip;
    }

    public String getDate_out() {
        return date_out;
    }

    public void setDate_out(String date_out) {
        this.date_out = date_out;
    }
}