package com.sk7software.whatsthatsong;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.network.*;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TrackSpeechlet {
    private static final Logger log = LoggerFactory.getLogger(TrackSpeechlet.class);

    private Track track;
    private SpotifyAuthentication authentication;

    public TrackSpeechlet(SpotifyAuthentication authentication) {
        this.authentication = authentication;
    }

    public Track getTrack() {
        return track;
    }

    public SpeechletResponse getNowPlayingResponse() {
        StringBuilder speechText = new StringBuilder();

        try {
            SpotifyWebAPIService trackService = new NowPlayingAPIService();
            // Get currently playing track and append market for track relinking
            String url = SpotifyWebAPIService.NOW_PLAYING_URL;
            track = (Track)trackService.fetchItem(url, authentication);
            speechText.append(track.getFullDescription());
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }

    public SpeechletResponse getTrackOriginalAlbumResponse() {
        StringBuilder speechText = new StringBuilder();

        try {
            log.info("Lookup " + track.getId());
            SpotifyWebAPIService trackService = new TrackAPIService();
            Track originalTrack = (Track)trackService.fetchItem(
                    SpotifyWebAPIService.TRACK_URL + track.getId(),
                    authentication);
            if (originalTrack.getAlbumId().equals(track.getAlbumId())) {
                speechText.append("This is the original album track.");
            } else {
                Album album = fetchAlbum(originalTrack.getAlbumId());
                speechText.append("The track is from the original album ");
                speechText.append(originalTrack.getFullAlbumDescription());
                speechText.append(". ");
                speechText.append(album.getAlbumInfo());
                track.setOriginalAlbumUri(originalTrack.getAlbumUri());
                track.setOriginalAlbumName(originalTrack.getAlbumName());
            }
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
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
                Album album = fetchAlbum(track.getAlbumId());
                speechText.append("This track is from the album ");
                speechText.append(album.getFullAlbumDescription());
                speechText.append(". ");
                speechText.append(album.getAlbumInfo());
            } else {
                speechText.append("Sorry, I can't find any information about the track");
            }
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }

    private Album fetchAlbum(String id) throws SpeechException {
        SpotifyWebAPIService albumService = new AlbumAPIService();
        return (Album)albumService.fetchItem(SpotifyWebAPIService.ALBUM_URL + id, authentication);
    }

    public SpeechletResponse getTrackTimeResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            // Re-fetch track to get latest progress
            SpotifyWebAPIService trackService = new NowPlayingAPIService();
            track = (Track)trackService.fetchItem(
                    SpotifyWebAPIService.TRACK_PROGRESS_URL,
                    authentication);
            speechText.append(track.getProgressDurationString());
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
    }
}
