package com.example.uhf_bt.dto;

import com.google.gson.annotations.SerializedName;

public class DetailLocation  implements  Comparable<DetailLocation> {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("img_data")
    public String imgData;

    @Override
    public int compareTo(DetailLocation otherItem) {
        // Compare by id
        return Integer.compare(this.id, otherItem.id);
    }

    public DetailLocation()
    {

    }

    public String getName()
    {
        return this.name;
    }

    public int getId()
    {
        return this.id;
    }
    public DetailLocation(int id, String name, String imgData) {
        this.id = id;
        this.name = name;
        this.imgData = imgData;
    }
}
