package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;

public class TrackAPIService extends SpotifyWebAPIService {

    @Override
    public Track fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            String trackStr = getJsonResponse(url, authentication.getAccessToken());

            Track track = Track.createFromItemJSON(new JSONObject(trackStr));
            return track;
        } catch (IOException | JSONException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up the track on Spotify.");
        }
    }
}
