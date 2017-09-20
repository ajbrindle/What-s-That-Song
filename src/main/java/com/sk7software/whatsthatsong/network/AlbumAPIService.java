package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;

public class AlbumAPIService extends SpotifyWebAPIService {
    @Override
    public Album fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            String albumStr = getJsonResponse(url, authentication.getAccessToken());

            Album album = Album.createFromJSON(new JSONObject(albumStr));
            return album;
        } catch (IOException | JSONException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up the album on Spotify.");
        }
    }
}
