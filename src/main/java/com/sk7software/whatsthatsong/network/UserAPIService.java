package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;

public class UserAPIService extends SpotifyWebAPIService {
    @Override
    public String fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            log.info("Auth token: " + authentication.getAccessToken());
            String userStr = getJsonResponse(url, authentication.getAccessToken());
            JSONObject userData = new JSONObject(userStr);
            return (String)userData.get("country");
        } catch (JSONException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up the user information on Spotify.");
        }
    }
}
