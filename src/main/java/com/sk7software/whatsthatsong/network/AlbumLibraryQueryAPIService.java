package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;

public class AlbumLibraryQueryAPIService extends SpotifyWebAPIService {
    @Override
    public Boolean fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            String libraryStr = getJsonResponse(url, authentication.getAccessToken());

            Boolean[] isInLibrary;
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            isInLibrary = mapper.readValue(libraryStr, Boolean[].class);
            return isInLibrary[0];

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up the album on Spotify.");
        }
    }
}
