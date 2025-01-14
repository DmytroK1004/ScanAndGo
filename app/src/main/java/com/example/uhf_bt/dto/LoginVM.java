package com.example.uhf_bt.dto;

import com.google.gson.annotations.SerializedName;

public class LoginVM {

    @SerializedName("message")
    public String message;


    @SerializedName("status")

    public int status;


    public LoginVM()
    {

    }

    public LoginVM( String _message, int _status)
    {
        message = _message;

        status = _status;
    }
}
