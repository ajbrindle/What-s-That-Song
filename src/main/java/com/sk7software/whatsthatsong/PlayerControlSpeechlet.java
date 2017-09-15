package com.sk7software.whatsthatsong;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.util.PlayerAction;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import com.sun.prism.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.sk7software.whatsthatsong.util.SpeechletUtils.*;

public class PlayerControlSpeechlet {
    private static PlayerControlSpeechlet instance = null;

    private static final Logger log = LoggerFactory.getLogger(PlayerControlSpeechlet.class);

    private PlayerControlSpeechlet() {
    }

    public static synchronized PlayerControlSpeechlet getInstance() {
        if (instance == null) {
            instance = new PlayerControlSpeechlet();
        }

        return instance;
    }

    public SpeechletResponse playerControl(PlayerAction action) {
        StringBuilder speechText = new StringBuilder();

        try {
            String spotifyURL;
            String method;
            Map<String, Object> postParams = new HashMap<>();
            Map<String, Object> position = new HashMap<>();
            Track track = TrackSpeechlet.getInstance().getTrack();

            switch (action) {
                case SKIP:
                    spotifyURL = "https://api.spotify.com/v1/me/player/next";
                    method = "POST";
                    speechText.append("Skipped");
                    break;
                case RESTART:
                    spotifyURL = "https://api.spotify.com/v1/me/player/seek?position_ms=0";
                    speechText.append("Restarted");
                    method = "PUT";
                    break;
                case PAUSE:
                    spotifyURL = "https://api.spotify.com/v1/me/player/pause";
                    speechText.append("Paused");
                    method = "PUT";
                    break;
                case RESUME:
                    spotifyURL = "https://api.spotify.com/v1/me/player/play";
                    speechText.append("Resumed");
                    method = "PUT";
                    break;
                case PLAY_ALBUM:
                    spotifyURL = "https://api.spotify.com/v1/me/player/play";
                    speechText.append("Playing ");
                    speechText.append(track.getAlbumName());
                    method = "PUT";
                    postParams.put("context_uri", track.getAlbumUri());
                    position.put("position", 0);
                    postParams.put("offset", position);
                    break;
                case PLAY_ORIGINAL_ALBUM:
                    spotifyURL = "https://api.spotify.com/v1/me/player/play";
                    if (track.hasOriginalAlbum()) {
                        speechText.append("Playing ");
                        speechText.append(track.getOriginalAlbumName());
                        method = "PUT";
                        postParams.put("context_uri", track.getOriginalAlbumUri());
                        position.put("position", 0);
                        postParams.put("offset", position);
                    } else {
                        speechText.append("Please ask if this track has an original album first");
                        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), false);
                    }
                    break;
                default:
                    // Never happens as actions are controlled by speechlet
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
        } catch (UsageLimitException ule) {
            speechText.delete(0, speechText.length());
            speechText.append(ule.getSpeechText());
        } catch (Exception e) {
            speechText.delete(0, speechText.length());
            speechText.append("An error has occurred");
            log.error(e.getMessage());
        }

        return SpeechletUtils.buildStandardAskResponse(speechText.toString(), false);
    }

    public int sendPlayerCommand(String requestURL, String method, Map<String, Object> postParams)
        throws UsageLimitException {
        URL url;
        HttpURLConnection con = null;

        try {
            url = new URL(requestURL);
            con = (HttpURLConnection) url.openConnection();

            // set up url connection to get retrieve information back
            con.setRequestMethod(method);

            // stuff the Authorization request header
            con.setRequestProperty("Authorization",
                    "Bearer " + SpotifyAuthentication.getAccessToken());

            if (postParams != null && postParams.size() > 0) {
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

            if (con.getResponseCode() == RESPONSE_LIMIT) {
                throw new UsageLimitException(con);
            }

            return con.getResponseCode();
        } catch (SpotifyAuthenticationException se) {
            log.error(se.getMessage() + " : " + se.getSpeechText());
            return RESPONSE_ERROR;
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
