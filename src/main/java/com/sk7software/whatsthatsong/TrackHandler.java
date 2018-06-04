package com.sk7software.whatsthatsong;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.network.*;
import com.sk7software.whatsthatsong.util.DeviceCapability;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class TrackHandler {
    private static final Logger log = LoggerFactory.getLogger(TrackHandler.class);

    public Optional<Response> getNowPlayingResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = null;

        try {
            SpotifyWebAPIService trackService = new NowPlayingAPIService();
            // Get currently playing track and append market for track relinking
            String url = SpotifyWebAPIService.NOW_PLAYING_URL;
            track = (Track) trackService.fetchItem(url, new SpotifyAuthentication(handlerInput));

            if (track != null) {
                ObjectMapper mapper = new ObjectMapper();
                //handlerInput.getAttributesManager().setSessionAttributes(mapper.convertValue(track, Map.class));
                handlerInput.getAttributesManager().getSessionAttributes().put("item", mapper.convertValue(track.getItem(), Map.class));
            }
            speechText.append(track.getFullDescription());
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        DeviceCapability deviceCapability = new DeviceCapability(handlerInput);

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);

        if (track != null && deviceCapability.hasDisplay()) {
            log.debug("Device has display");
            responseBuilder.addRenderTemplateDirective(SpeechletUtils.createDisplayTemplate(track));
        }

        return responseBuilder.build();
    }

    public Optional<Response> getTrackOriginalAlbumResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);
        Track originalTrack = null;

        try {
            log.info("Lookup " + track.getId());
            SpotifyWebAPIService trackService = new TrackAPIService();
            originalTrack = (Track)trackService.fetchItem(
                    SpotifyWebAPIService.TRACK_URL + track.getId(),
                    new SpotifyAuthentication(handlerInput));
            if (originalTrack.getAlbumId().equals(track.getAlbumId())) {
                speechText.append("This is the original album track.");
            } else {
                Album album = fetchAlbum(originalTrack.getAlbumId(), handlerInput);
                speechText.append("The track is from the original album ");
                speechText.append(originalTrack.getFullAlbumDescription());
                speechText.append(". ");
                speechText.append(album.buildAlbumInfo());
                track.setOriginalAlbumUri(originalTrack.getAlbumUri());
                track.setOriginalAlbumName(originalTrack.getAlbumName());
            }
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I couldn't find any original track information");
            log.error(e.getMessage());
        }

        DeviceCapability deviceCapability = new DeviceCapability(handlerInput);

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);

        if (originalTrack != null && deviceCapability.hasDisplay()) {
            log.debug("Device has display");
            responseBuilder.addRenderTemplateDirective(SpeechletUtils.createDisplayTemplate(originalTrack));
        }

        return responseBuilder.build();
    }

    public Optional<Response> getTrackExplicitResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);

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

        DeviceCapability deviceCapability = new DeviceCapability(handlerInput);

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);

        if (track != null && deviceCapability.hasDisplay()) {
            log.debug("Device has display");
            responseBuilder.addRenderTemplateDirective(SpeechletUtils.createDisplayTemplate(track));
        }

        return responseBuilder.build();
    }

    public Optional<Response> getAlbumNameResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);

        // Get currently playing track
        try {
            if (track != null) {
                log.debug("Restored track: " + track.getItem().toString());

                // Fetch the album
                Album album = fetchAlbum(track.getAlbumId(), handlerInput);
                speechText.append("This track is from the album ");
                speechText.append(album.getFullAlbumDescription());
                speechText.append(". ");
                speechText.append(album.buildAlbumInfo());
            } else {
                speechText.append("Sorry, I can't find any information about the track");
            }
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        DeviceCapability deviceCapability = new DeviceCapability(handlerInput);

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);

        if (track != null && deviceCapability.hasDisplay()) {
            log.debug("Device has display");
            responseBuilder.addRenderTemplateDirective(SpeechletUtils.createDisplayTemplate(track));
        }

        return responseBuilder.build();
    }

    private Album fetchAlbum(String id, HandlerInput handlerInput) throws SpeechException {
        SpotifyWebAPIService albumService = new AlbumAPIService();
        return (Album)albumService.fetchItem(SpotifyWebAPIService.ALBUM_URL + id,
                new SpotifyAuthentication(handlerInput));
    }

    public Optional<Response> getTrackTimeResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = null;

        // Get currently playing track
        try {
            // Re-fetch track to get latest progress
            SpotifyWebAPIService trackService = new NowPlayingAPIService();
            track = (Track)trackService.fetchItem(
                    SpotifyWebAPIService.TRACK_PROGRESS_URL,
                    new SpotifyAuthentication(handlerInput));
            speechText.append(track.getProgressDurationString());
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        DeviceCapability deviceCapability = new DeviceCapability(handlerInput);

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);

        if (track != null && deviceCapability.hasDisplay()) {
            log.debug("Device has display");
            responseBuilder.addRenderTemplateDirective(SpeechletUtils.createDisplayTemplate(track));
        }

        return responseBuilder.build();
    }

    private Track getTrackFromSession(HandlerInput handlerInput) {
        try {
            if (handlerInput.getAttributesManager().getSessionAttributes().containsKey("item")) {
                JSONObject j = new JSONObject(handlerInput.getAttributesManager().getSessionAttributes());
                log.debug("Session attribute: " + j);
                Track t = Track.createFromJSON(j);
                log.debug(t.getName());
                return t;
            }
        } catch (IOException ie) {
            log.error("Unable to deserialise track: " + ie.getMessage());
        }
        return null;
    }
}
