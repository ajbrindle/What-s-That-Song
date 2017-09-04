package com.sk7software.whatsthatsong;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackSpeechlet {

    private Track track;

    private static TrackSpeechlet instance = null;

    private static final Logger log = LoggerFactory.getLogger(TrackSpeechlet.class);

    private TrackSpeechlet() {
    }

    public static synchronized TrackSpeechlet getInstance() {
        if (instance == null) {
            instance = new TrackSpeechlet();
        }
        return instance;
    }

    public Track getTrack() {
        return track;
    }

    public SpeechletResponse getNowPlayingResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            String trackStr = SpeechletUtils.getJsonResponse("https://api.spotify.com/v1/me/player",
                    SpotifyAuthentication.getAccessToken());
            track = Track.createFromJSON(new JSONObject(trackStr));
            speechText.append(track.getFullDescription());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }

    public SpeechletResponse getTrackOriginalAlbumResponse() {
        StringBuilder speechText = new StringBuilder();

        // Lookup the track id
        try {
            String trackStr = SpeechletUtils.getJsonResponse("https://api.spotify.com/v1/tracks/" + track.getId(),
                    SpotifyAuthentication.getAccessToken());
            Track originalTrack = Track.createFromItemJSON(new JSONObject(trackStr));
            speechText.append(originalTrack.getFullAlbumDescription());
        } catch (Exception e) {
            speechText.append("Sorry, I couldn't find any original track information");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }

    public SpeechletResponse getTrackExplicitResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            if (track != null) {
                boolean isExplicit = track.isExplicit();
                speechText.append(isExplicit ? "Yes" : "No");
                speechText.append(", this track does ");
                speechText.append(isExplicit ? "" : "not ");
                speechText.append("contain explicit lyrics.");
            } else {
                speechText.append("Sorry, I can't find any information about the track");
            }
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }

    public SpeechletResponse getAlbumNameResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            if (track != null) {
                // Fetch the album
                String albumStr = SpeechletUtils.getJsonResponse("https://api.spotify.com/v1/albums/" + track.getAlbumId(),
                        SpotifyAuthentication.getAccessToken());
                Album album = Album.createFromJSON(new JSONObject(albumStr));

                speechText.append("This track is from the album ");
                speechText.append(album.getFullAlbumDescription());
                speechText.append(". ");
                speechText.append(album.getAlbumInfo());
            } else {
                speechText.append("Sorry, I can't find any information about the track");
            }
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }

    public SpeechletResponse getTrackTimeResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            // Re-fetch track to get latest progress
            String trackStr = SpeechletUtils.getJsonResponse("https://api.spotify.com/v1/me/player",
                    SpotifyAuthentication.getAccessToken());
            track = Track.createFromJSON(new JSONObject(trackStr));
            speechText.append(track.getProgressDurationString());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }


}
