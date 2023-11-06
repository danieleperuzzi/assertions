package com.danieleperuzzi.assertion.util;

public class ApiResponseMock {

    private int status;
    private String responseText;

    public ApiResponseMock(int status, String responseText) {
        this.status = status;
        this.responseText = responseText;
    }

    public int getStatus() {
        return status;
    }

    public String getResponseText() {
        return responseText;
    }
}
