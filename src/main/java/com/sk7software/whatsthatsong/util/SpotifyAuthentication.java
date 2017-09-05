package com.sk7software.whatsthatsong.util;

import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotifyAuthentication {
    private static String accessToken;
    private static String market;

    private static final Logger log = LoggerFactory.getLogger(SpotifyAuthentication.class);

    private SpotifyAuthentication() {
    }

    public static synchronized void setAccessToken(String accessToken) {
        SpotifyAuthentication.accessToken = accessToken;
    }

    public static synchronized String getAccessToken() throws SpotifyAuthenticationException{
        if (accessToken != null && !"".equals(accessToken)) {
            return accessToken;
        } else {
            throw new SpotifyAuthenticationException("Not authorised by Spotify");
        }
    }

    public static synchronized String getMarket() {
        if (market == null || "".equals(market)) {
            try {
                String userResponse = SpeechletUtils.getJsonResponse("https://api.spotify.com/v1/me",
                        SpotifyAuthentication.getAccessToken());
                JSONObject userData = new JSONObject(userResponse);
                market = (String)userData.get("country");
            } catch (Exception e) {
                log.error("Error finding market: " + e.getMessage());
                market = "";
            }
        }

        return market;
    }
}
