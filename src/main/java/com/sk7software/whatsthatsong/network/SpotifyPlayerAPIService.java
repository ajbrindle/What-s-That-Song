package com.sk7software.whatsthatsong.network;

import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import javafx.scene.effect.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static com.sk7software.whatsthatsong.network.SpotifyWebAPIService.RESPONSE_DONE;
import static com.sk7software.whatsthatsong.network.SpotifyWebAPIService.RESPONSE_OK;
import static com.sk7software.whatsthatsong.network.SpotifyWebAPIService.RESPONSE_RETRY;

public class SpotifyPlayerAPIService {
    protected static final Logger log = LoggerFactory.getLogger(SpotifyPlayerAPIService.class);

    public static final String PLAYER_URL = "https://api.spotify.com/v1/me/player";
    public static final String VOLUME_URL = "https://api.spotify.com/v1/me/player/volume?volume_percent=";
    public static final String SKIP_URL = "https://api.spotify.com/v1/me/player/next";
    public static final String PREV_URL = "https://api.spotify.com/v1/me/player/previous";
    public static final String RESTART_URL = "https://api.spotify.com/v1/me/player/seek?position_ms=0";
    public static final String PAUSE_URL = "https://api.spotify.com/v1/me/player/pause";
    public static final String RESUME_URL = "https://api.spotify.com/v1/me/player/play";
    public static final String PLAY_ALBUM_URL = "https://api.spotify.com/v1/me/player/play";
    public static final String PLAY_ORIGINAL_ALBUM_URL = "https://api.spotify.com/v1/me/player/play";

    private int retryInterval = 5000;

    public void sendPlayerCommand(String requestUrl, String method,
                                 Map<String, Object> postParams, SpotifyAuthentication authentication)
            throws SpeechException {

        HttpURLConnection con = null;

        try {
            log.debug("Player URL: " + requestUrl);
            URL url = new URL(requestUrl);
            con = makeRequest(url, method, authentication.getAccessToken(), postParams);
            int responseCode = con.getResponseCode();

            if (responseCode == SpotifyWebAPIService.RESPONSE_LIMIT) {
                throw new UsageLimitException(con);
            } else if (responseCode == RESPONSE_RETRY) {
                // Can retry twice, 5 seconds apart
                for (int i=0; i<2; i++) {
                    try {
                        Thread.sleep(retryInterval);
                        con = makeRequest(url, method, authentication.getAccessToken(), postParams);
                        responseCode = con.getResponseCode();
                        if (responseCode != RESPONSE_RETRY) {
                            break;
                        }
                    } catch (InterruptedException e) {}
                }
            }

            if (responseCode != RESPONSE_DONE &&
                responseCode != RESPONSE_OK) {
                log.debug("Response code: " + responseCode);
                throw new SpotifyWebAPIException("Failed to send action to player");
            }
        } catch (IOException e) {
            try {
                if (con != null) {
                    log.error("Response code: " + con.getResponseCode());
                }
            } catch (IOException e1) {}
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Failed to send action to player");
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private HttpURLConnection makeRequest(URL url, String method,
                                          String accessToken, Map<String, Object> postParams) throws IOException {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // set up url connection to get retrieve information back
            con.setRequestMethod(method);

            // stuff the Authorization request header
            con.setRequestProperty("Authorization",
                    "Bearer " + accessToken);
            con.setRequestProperty("Accept", "application/json");

            if (postParams != null) {
                JSONObject postData = new JSONObject(postParams);
                con.setRequestProperty("Content-Type", "application/json");
                log.info("postData: " + postData.toString());
                con.setDoOutput(true);
                con.getOutputStream().write(postData.toString().getBytes());
                con.getOutputStream().flush();
                con.getOutputStream().close();
            } else {
                con.setDoOutput(true);
                con.setRequestProperty("Content-Length", "0");
                con.connect();
            }

            return con;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IOException(e);
        }
    }

    public void setRetryInterval(int millis) {
        retryInterval = millis;
    }
}
