package com.example.mobile;

import java.util.List;

public class PromoResponse {
    private boolean success;
    private String message;
    private List<Promo> data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Promo> getData() { return data; }
}