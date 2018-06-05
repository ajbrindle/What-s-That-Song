package com.sk7software.whatsthatsong.model;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.whatsthatsong.exception.MissingElementException;
import com.sk7software.whatsthatsong.exception.UnknownDeviceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AvailableDevices {
    private List<Device> devices;

    public AvailableDevices() {
        devices = new ArrayList<>();
    }

    private void setDevices(List<Device> devices) {
        for (Device d : devices) {
            if (!d.isRestricted()) {
                this.addDevice(d);
            }
        }
    }

    public static AvailableDevices createFromJSON(JSONObject response) throws IOException, JSONException {
        AvailableDevices ad = new AvailableDevices();

        JSONArray deviceData = response.getJSONArray("devices");
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Device> devices = mapper.readValue(deviceData.toString(), new TypeReference<List<Device>>(){});
        ad.setDevices(devices);
        ad.setIndexes();

        return ad;
    }

    public boolean isEmpty() {
        return devices.isEmpty();
    }

    public int getNumberOfDevices() {
        return devices.size();
    }

    public Device getDeviceAtIndex(int index) throws UnknownDeviceException, MissingElementException {
        if (devices == null || devices.size() == 0) {
            throw new MissingElementException("Please fetch the device list first.");
        } else if (index > devices.size() || index < 1) {
            throw new UnknownDeviceException("Device " + index + " can't be found.");
        }  else {
            for (Device d : devices) {
                if (d.getIndex() == index) {
                    return d;
                }
            }
        }

        throw new UnknownDeviceException("Unable to find device number " + index);
    }

    public void addDevice(Device d) {
        devices.add(d);
    }

    private void setIndexes() {
        int index = 1;

        for (Device d : devices) {
            if (!d.isRestricted()) {
                d.setIndex(index++);
            }
        }
    }

    public List<Device> getDevices() {
        return devices;
    }

    public String getDeviceList(boolean verbose) throws MissingElementException {
        StringBuilder speechText = new StringBuilder();

        if (devices == null || devices.size() == 0) {
            throw new MissingElementException("There are no available devices.");
        } else if (!verbose) {
            return "Device list fetched.";
        } else {
            for (Device d : devices) {
                if (!d.isRestricted()) {
                    speechText.append(d.getDeviceDescription());
                }
            }
        }
        return speechText.toString();
    }

    public void setActiveDevice(Device active) {
        for (Device d : devices) {
            d.setActive(d.getId().equals(active.getId()));
        }
    }

    public void setActiveDevice(String id) {
        for (Device d : devices) {
            d.setActive(id.equals(d.getId()));
        }
    }

    public Device getActiveDevice() {
        for (Device d : devices) {
            if (d.isActive()) {
                return d;
            }
        }

        return null;
    }

    public String findClosestNameMatchId(String spokenName) throws MissingElementException, UnknownDeviceException {
        int maxScore = 0;
        String maxScoreId = "";

        if (devices.isEmpty()) {
            throw new MissingElementException("Sorry, I couldn't find any available devices. " +
                    "If you are sure you have some, please ask me to fetch your devices first.");
        }

        for (Device d : devices) {
            int matchScore = d.calcNameMatchScore(spokenName);

            if (matchScore > maxScore) {
                maxScore = matchScore;
                maxScoreId = d.getId();
            }
        }

        if ("".equals(maxScoreId)) {
            throw new UnknownDeviceException("Sorry, I wasn't able to find a device that sounded like " +
                    spokenName);
        }

        return maxScoreId;
    }
}
