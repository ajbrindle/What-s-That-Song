/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.model.Album;
import com.sk7software.whatsthatsong.model.Track;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andrew
 */
public class WhatsThatSongSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(WhatsThatSongSpeechlet.class);
    private static final String repromptText = "Next Action?";
    
    private static final int ACTION_SKIP = 1;
    private static final int ACTION_RESTART = 2;
    private static final int ACTION_PAUSE = 3;
    private static final int ACTION_RESUME = 4;
    private static final int ACTION_ALBUM_PLAY = 5;
    
    private static final int RESPONSE_DONE = 204;
    private static final int RESPONSE_RETRY = 202;
    private static final int RESPONSE_ERROR = 0;
    
    private String accessToken;
    private Track track;

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        accessToken = session.getUser().getAccessToken();
        log.info("Access token: " + accessToken);

        return getNowPlayingResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}, intentName={}", request.getRequestId(),
                session.getSessionId(), (request.getIntent() != null ? request.getIntent().getName() : "null"));

        accessToken = session.getUser().getAccessToken();

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        
        switch (intentName) {
            case "WhatsThatSongIntent":
                return getNowPlayingResponse();
            case "TrackExplicitIntent":
                return getTrackExplicitResponse();
            case "PlayerControlSkipIntent":
                return playerControl(ACTION_SKIP);
            case "PlayerControlRestartIntent":
                return playerControl(ACTION_RESTART);
            case "PlayerControlPauseIntent":
                return playerControl(ACTION_PAUSE);
            case "PlayerControlResumeIntent":
                return playerControl(ACTION_RESUME);
            case "AlbumNameIntent":
                return getAlbumNameResponse();
            case "AlbumPlayIntent":
                return playerControl(ACTION_ALBUM_PLAY);
            case "TrackTimeIntent":
                return getTrackTimeResponse();
            case "AMAZON.HelpIntent":
                return getHelpResponse();
            case "AMAZON.StopIntent":
                return getStopResponse();
            default:
                throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getNowPlayingResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            String trackStr = getJsonResponse("https://api.spotify.com/v1/me/player");
            track = Track.createFromJSON(new JSONObject(trackStr));
            speechText.append("This song is ");
            speechText.append(track.getName());
            speechText.append(" by ");
            speechText.append(track.getArtistName());
            
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }
        
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("What's That Song");
        card.setContent(speechText.toString());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = getStandardReprompt();

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getTrackExplicitResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            if (track != null) {
                boolean isExplicit = track.isExplicit();
                speechText.append("This track does ");
                speechText.append(isExplicit ? "" : "not ");
                speechText.append("contain explicit lyrics.");
            } else {
                speechText.append("Sorry, I can't find any information about the track");
            }
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("What's That Song");
        card.setContent(speechText.toString());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = getStandardReprompt();

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse getAlbumNameResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            if (track != null) {
                // Fetch the album
                String albumStr = getJsonResponse("https://api.spotify.com/v1/albums/" + track.getAlbumId());
                Album album = Album.createFromJSON(new JSONObject(albumStr));

                speechText.append("This track is from the album ");
                speechText.append(track.getAlbumName());
                speechText.append(" by ");
                speechText.append(track.getArtistName());
                speechText.append(". ");
                speechText.append(album.getAlbumInfo());
            } else {
                speechText.append("Sorry, I can't find any information about the track");
            }
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("What's That Song");
        card.setContent(speechText.toString());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = getStandardReprompt();

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }


    private SpeechletResponse getTrackTimeResponse() {
        StringBuilder speechText = new StringBuilder();

        // Get currently playing track
        try {
            // Re-fetch track to get latest progress
            String trackStr = getJsonResponse("https://api.spotify.com/v1/me/player");
            track = Track.createFromJSON(new JSONObject(trackStr));
            speechText.append(track.getProgressDurationString());
        } catch (Exception e) {
            speechText.append("Sorry, I can't find the currently playing track");
            log.error(e.getMessage());
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("What's That Song");
        card.setContent(speechText.toString());

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = getStandardReprompt();

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse playerControl(int action) {
        StringBuilder speechText = new StringBuilder();

        try {
            String spotifyURL;
            String method;
            Map<String, String> postParams = new HashMap<>();
            
            switch (action) {
                case ACTION_SKIP:
                    spotifyURL = "https://api.spotify.com/v1/me/player/next";
                    method = "POST";
                    speechText.append("Skipped");
                    break;
                case ACTION_RESTART:
                    spotifyURL = "https://api.spotify.com/v1/me/player/seek?position_ms=0";
                    speechText.append("Restarted");
                    method = "PUT";
                    break;
                case ACTION_PAUSE:
                    spotifyURL = "https://api.spotify.com/v1/me/player/pause";
                    speechText.append("Paused");
                    method = "PUT";
                    break;
                case ACTION_RESUME:
                    spotifyURL = "https://api.spotify.com/v1/me/player/play";
                    speechText.append("Resumed");
                    method = "PUT";
                    break;
                case ACTION_ALBUM_PLAY:
                    spotifyURL = "https://api.spotify.com/v1/me/player/play";
                    speechText.append("Playing ");
                    speechText.append(track.getAlbumName());
                    speechText.append(" by ");
                    speechText.append(track.getArtistName());
                    method = "PUT";
                    postParams.put("context_uri", track.getAlbumUri());
                    break;                    
                default:
                    throw new Exception("Invalid action");
            }
            int responseCode = sendPlayerCommand(spotifyURL, method, postParams);
            
            if (responseCode == RESPONSE_RETRY) {
                // Can retry twice, 5 seconds apart
                for (int i=0; i<2; i++) {
                    try {
                        Thread.sleep(5000);
                        responseCode = sendPlayerCommand(spotifyURL, method, postParams);
                        if (responseCode != RESPONSE_RETRY) {
                            break;
                        } 
                    } catch (InterruptedException e) {}
                }
            }
            
            if (responseCode != RESPONSE_DONE) {
                speechText.delete(0, speechText.length());
                speechText.append("Failed to send action");
            }            
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText.toString());

        // Create reprompt
        Reprompt reprompt = getStandardReprompt();

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }
    
    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("You can ask to skip or restart the song by saying, ");
        helpText.append("Skip, pause, resume, or restart it. ");
        helpText.append("You can ask if it contains explicit lyrics by saying, is it explicit. ");
        helpText.append("You can ask what album is it on. ");
        helpText.append("You can ask it to play the whole album. ");
        helpText.append("Please give your next command.");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(helpText.toString());

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse getStopResponse() {
        String stopText = "Goodbye";

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(stopText);

        SpeechletResponse stopResponse = new SpeechletResponse();
        stopResponse.setShouldEndSession(true);
        stopResponse.setOutputSpeech(speech);
        
        return stopResponse;
    }

    Reprompt getStandardReprompt() {
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        reprompt.setOutputSpeech(repromptSpeech);
        return reprompt;
    }
    
    String getJsonResponse(String requestURL) {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text = "";
        try {
            String line;
            URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // set up url connection to get retrieve information back
            con.setRequestMethod("GET");
            
            // stuff the Authorization request header
            con.setRequestProperty("Authorization",
                    "Bearer " + accessToken);
            
            inputStream = new InputStreamReader(con.getInputStream(), Charset.forName("US-ASCII"));
            bufferedReader = new BufferedReader(inputStream);
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            text = builder.toString();
        } catch (IOException e) {
            // reset text variable to a blank string
            log.error(e.getMessage());
            text = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }
        
        return text;
    }
    
    
    int sendPlayerCommand(String requestURL, String method, Map<String, String>postParams) {
        URL url;
        HttpURLConnection con = null;
        
        try {
            url = new URL(requestURL);
            con = (HttpURLConnection) url.openConnection();

            // set up url connection to get retrieve information back
            con.setRequestMethod(method);

            // stuff the Authorization request header
            con.setRequestProperty("Authorization",
                    "Bearer " + accessToken);
            
            if (postParams.size() > 0) {
                JSONObject postData = new JSONObject(postParams);        
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                log.info("postData: " + postData.toString());
                con.setDoOutput(true);
                con.getOutputStream().write(postData.toString().getBytes());
                con.getOutputStream().flush();
                con.getOutputStream().close();
            } else {
                con.connect();
            }

            log.info("Response: " + con.getResponseCode());
            return con.getResponseCode();
        } catch (IOException e) {
            try {
                if (con != null) {
                    log.error("Response code: " + con.getResponseCode());
                }
            } catch (IOException e1) {}
            log.error(e.getMessage());
            return RESPONSE_ERROR;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
