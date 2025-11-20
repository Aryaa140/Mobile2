package com.example.mobile;

public class CheckNIPResponse {
    private boolean exists;
    private String message;
    private String username;
    private boolean success;
    public boolean isExists() {
        return exists;
    }

    public String getMessage() {
        return message;
    }
    public String getUsername() {
        return username;
    }

    public boolean isNipMatchWithUsername(String inputUsername) {
        return username != null && username.equals(inputUsername);
    }
    public boolean isSuccess() {
        return success;
    }

}