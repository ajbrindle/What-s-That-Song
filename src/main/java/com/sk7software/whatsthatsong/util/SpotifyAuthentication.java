package com.sk7software.whatsthatsong.util;

import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.network.UserAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotifyAuthentication {
    private String accessToken;
    private String market;

    private static final Logger log = LoggerFactory.getLogger(SpotifyAuthentication.class);

    public SpotifyAuthentication() {
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() throws SpotifyAuthenticationException{
        if (accessToken != null && !"".equals(accessToken)) {
            return accessToken;
        } else {
            throw new SpotifyAuthenticationException("You are not authorised by Spotify");
        }
    }

    public String getMarket() {
        if (market == null || "".equals(market)) {
            try {
                SpotifyWebAPIService userService = new UserAPIService();
                market = (String)userService.fetchItem(SpotifyWebAPIService.USER_URL,
                        this);
            } catch (SpeechException e) {
                log.error("Error finding market: " + e.getMessage());
                market = "";
            }
        }

        return market;
    }
}
