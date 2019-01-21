package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.model.TrackItem;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsAPIService extends SpotifyWebAPIService {
    @Override
    public List<Track> fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException {
        try {
            String recommendationUrl = url +
                    ("".equals(authentication.getMarket()) ? "" :
                            "&market=" + authentication.getMarket());
            String tracksStr = getJsonResponse(recommendationUrl, authentication.getAccessToken());
            JSONObject response = new JSONObject(tracksStr);
            JSONArray deviceData = response.getJSONArray("tracks");
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<TrackItem> trackItems = mapper.readValue(deviceData.toString(),
                    new TypeReference<List<TrackItem>>(){});

            List<Track> tracks = new ArrayList<>();

            for (TrackItem i : trackItems) {
                Track t = new Track();
                t.setItem(i);
                tracks.add(t);
            }

            return tracks;
        } catch (IOException | JSONException e) {
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, there was an error looking up the recommendations on Spotify.");
        }
    }

}
