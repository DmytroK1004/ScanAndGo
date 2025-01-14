package com.example.uhf_bt.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResponseCheckItems {

    @SerializedName("data")

    public ArrayList<ResponseCheckItem> data = new ArrayList<ResponseCheckItem>();

}
