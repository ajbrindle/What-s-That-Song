package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.model.Lyrics;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.network.*;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

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

            // Update session attributes
            if (track != null) {
                ObjectMapper mapper = new ObjectMapper();
                handlerInput.getAttributesManager().getSessionAttributes().put("track", mapper.convertValue(track, Map.class));
            }
            speechText.append(track.getFullDescription());
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
        SpeechletUtils.addTrackDisplay(handlerInput, track, responseBuilder);
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
                ObjectMapper mapper = new ObjectMapper();
                handlerInput.getAttributesManager().getSessionAttributes().put("track", mapper.convertValue(track, Map.class));
            }
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I couldn't find any original track information");
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
        SpeechletUtils.addAlbumDisplay(handlerInput, originalTrack, responseBuilder);
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

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
        SpeechletUtils.addTrackDisplay(handlerInput, track, responseBuilder);
        return responseBuilder.build();
    }

    public Optional<Response> getAlbumNameResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);

        // Get currently playing track
        try {
            if (track != null) {
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

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
        SpeechletUtils.addAlbumDisplay(handlerInput, track, responseBuilder);
        return responseBuilder.build();
    }

    public Optional<Response> getAlbumInLibraryResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);
        Boolean inLibrary = false;

        try {
            log.info("Lookup " + track.getAlbumId());
            SpotifyWebAPIService libraryService = new AlbumLibraryQueryAPIService();
            inLibrary = (Boolean)libraryService.fetchItem(
                    SpotifyWebAPIService.LIBRARY_QUERY_URL + track.getAlbumId(),
                    new SpotifyAuthentication(handlerInput));
            speechText.append("This album is ");
            speechText.append(inLibrary ? "already" : "not");
            speechText.append(" in your library.");
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, I couldn't find your library information");
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), false);
        SpeechletUtils.addAlbumDisplay(handlerInput, track, responseBuilder);
        return responseBuilder.build();
    }

    public Optional<Response> getAlbumAddToLibraryResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);

        try {
            log.info("Add " + track.getAlbumId());
            List<String> ids = new ArrayList<>();
            ids.add(track.getAlbumId());

            SpotifyWebUpdateAPIService libraryService = new SpotifyWebUpdateAPIService();
            libraryService.sendCommand(
                    SpotifyWebUpdateAPIService.LIBRARY_ADD_URL, "PUT", ids,
                        new SpotifyAuthentication(handlerInput));
            speechText.append("This album has been added to your library");
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, there was an error adding the album to your library");
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), false);
        SpeechletUtils.addAlbumDisplay(handlerInput, track, responseBuilder);
        return responseBuilder.build();
    }

    public Optional<Response> getLyricsResponse(HandlerInput handlerInput) {
        StringBuilder speechText = new StringBuilder();
        Track track = getTrackFromSession(handlerInput);
        Lyrics lyrics = null;

        try {
            log.info("Fetch lyrics for " + track.getName());
            lyrics = fetchLyrics(track, handlerInput);
            speechText.append("Lyrics fetched");
        } catch (SpeechException se) {
            speechText.append(se.getSpeechText());
        } catch (Exception e) {
            speechText.append("Sorry, there was an error fetching the lyrics");
            log.error(e.getMessage());
        }

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), false);
        SpeechletUtils.addLyricsDisplay(handlerInput, track, lyrics, responseBuilder);
        return responseBuilder.build();
    }

    private Album fetchAlbum(String id, HandlerInput handlerInput) throws SpeechException {
        SpotifyWebAPIService albumService = new AlbumAPIService();
        return (Album)albumService.fetchItem(SpotifyWebAPIService.ALBUM_URL + id,
                new SpotifyAuthentication(handlerInput));
    }

    private Lyrics fetchLyrics(Track track, HandlerInput handlerInput) throws SpeechException {
        try {
            SpotifyWebAPIService lyricsService = new LyricsAPIService();
            String urlArgs = "&q_track=" + URLEncoder.encode(track.getName(), "UTF-8") +
                    "&q_artist=" + URLEncoder.encode(track.getArtistName(), "UTF-8");
            return (Lyrics) lyricsService.fetchItem(SpotifyWebAPIService.MUSIXMATCH_URL + urlArgs,
                    new SpotifyAuthentication(handlerInput));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding: " + e.getMessage());
            return null;
        }
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

        ResponseBuilder responseBuilder = SpeechletUtils.buildStandardAskResponse(speechText.toString(), true);
        SpeechletUtils.addTrackDisplay(handlerInput, track, responseBuilder);
        return responseBuilder.build();
    }

    public static Track getTrackFromSession(HandlerInput handlerInput) {
        try {
            if (handlerInput.getAttributesManager().getSessionAttributes().containsKey("track")) {
                JSONObject j = new JSONObject(handlerInput.getAttributesManager().getSessionAttributes());
                Track t = Track.createFromJSON(j.getJSONObject("track"));
                log.debug("Restored track: " + t.getName());
                return t;
            }
        } catch (JSONException je) {
            log.error("Unable to deserialise track: " + je.getMessage());
        } catch (IOException ie) {
            log.error("Unable to deserialise track: " + ie.getMessage());
        }
        return null;
    }
}
