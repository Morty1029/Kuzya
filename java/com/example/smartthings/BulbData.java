package com.example.smartthings;
import com.google.gson.annotations.SerializedName;

public class BulbData {
    @SerializedName("ip")
    private String ip;

    @SerializedName("port")
    private String port;

    @SerializedName("name")
    private String name;

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getName() {
        return name;
    }
}
