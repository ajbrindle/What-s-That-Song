package com.sk7software.whatsthatsong.util;

import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;

public class SpotifyAuthentication {
    private static String accessToken;

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
}
