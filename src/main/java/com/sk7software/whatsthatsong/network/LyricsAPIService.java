package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Lyrics;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;

public class LyricsAPIService extends SpotifyWebAPIService {

    private static final String MUSIXMATCH_API_KEY = System.getenv("MUSIXMATCH_API_KEY");

    @Override
    public Lyrics fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            String lyricsStr = getJsonResponse(url + "&apikey=" + MUSIXMATCH_API_KEY,
                    authentication.getAccessToken());

            JSONObject fullResponse = new JSONObject(lyricsStr);
            JSONObject lyricsResponse = fullResponse
                    .getJSONObject("message")
                    .getJSONObject("body")
                    .getJSONObject("lyrics");

            Lyrics lyrics = Lyrics.createFromJSON(lyricsResponse);
            return lyrics;
        } catch (IOException | JSONException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up the track lyrics.");
        }
    }
}
