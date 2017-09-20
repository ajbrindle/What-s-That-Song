package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.AvailableDevices;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;

public class DevicesAPIService extends SpotifyWebAPIService {
    @Override
    public AvailableDevices fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            String devicesStr = getJsonResponse(url, authentication.getAccessToken());
            log.info(devicesStr);
            return AvailableDevices.createFromJSON(new JSONObject(devicesStr));
        } catch (IOException | JSONException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up your devices on Spotify.");
        }
    }
}
