package com.example.uhf_bt.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UpdateLocation {

    @SerializedName("barcode_list")
    public List<String> barcode_list = new ArrayList<>();

    @SerializedName("building_id")
    public  int building_id;

    @SerializedName("area_id")
    public  int area_id;

    @SerializedName("floor_id")
    public  int floor_id;

    @SerializedName("block_id")
    public  int block_id;

    public UpdateLocation()
    {

    }

    public UpdateLocation(List<String> _barcode_list, int _building_id, int _area_id, int _floor_id, int _block_id)
    {
        this.barcode_list = _barcode_list;
        this.building_id = _building_id;
        this.area_id = _area_id;
        this.floor_id = _floor_id;
        this.block_id = _block_id;
    }
}
