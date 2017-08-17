/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong.model;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Device {
    private String id;
    @JsonProperty("is_active")
    private boolean active;
    @JsonProperty("is_restricted")
    private boolean restricted;
    private String name;
    private String type;
    @JsonProperty("volume_percent")
    private int volumePercent;
    private int index;

    public static List<Device> createFromJSON(JSONObject response) throws IOException, JSONException {
        
        JSONArray deviceData = response.getJSONArray("devices");
        
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Device> devices = mapper.readValue(deviceData.toString(), new TypeReference<List<Device>>(){});

        return devices;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(int volumePercent) {
        this.volumePercent = volumePercent;
    }
    
    public int calcNameMatchScore(String spokenName) {
        int score = 0;
        String deviceName = name;
        String deviceType = type;
        deviceName = deviceName.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();
        deviceType = deviceType.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();
        spokenName = spokenName.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();
        
        // Remove all non-alphanumerics (leave spaces)
        String[] deviceWords = deviceName.split(" ");
        String[] spokenWords = spokenName.split(" ");
        String[] typeWords = deviceType.split(" ");
        
        // Loop through words in spoken name and device name to find matches
        for (String dw : deviceWords) {
            for (String sw : spokenWords) {
                if (dw.equals(sw)) {
                    // 3 points for a match in the name
                    score += 3;
                } else if (dw.contains(sw)) {
                    // 2 points for name containing word
                    score += 2;
                }
            }
        }
        
        for (String tw : typeWords) {
            for (String sw : spokenWords) {
                if (tw.contains(sw)) {
                    // 1 point for a match in the type
                    score++;
                }
            }            
        }
        
        return score;
    }
}
