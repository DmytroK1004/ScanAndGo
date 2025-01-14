package com.example.uhf_bt.dto;

import com.google.gson.annotations.SerializedName;

public class Login {

    @SerializedName("username")

    public String username;

    @SerializedName("password")

    public String password;

    public Login()
    {

    }
    public Login(String _username, String _password)
    {
        username = _username;
        password = _password;
    }

}
