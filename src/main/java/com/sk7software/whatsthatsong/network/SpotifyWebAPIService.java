package com.sk7software.whatsthatsong.network;

import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public abstract class SpotifyWebAPIService {
    protected static final Logger log = LoggerFactory.getLogger(SpotifyWebAPIService.class);

    public static final int RESPONSE_DONE = 204;
    public static final int RESPONSE_RETRY = 202;
    public static final int RESPONSE_LIMIT = 429;
    public static final int RESPONSE_OK = 200;

    public static final String NOW_PLAYING_URL = "https://api.spotify.com/v1/me/player/currently-playing";
    public static final String TRACK_URL = "https://api.spotify.com/v1/tracks/";
    public static final String ALBUM_URL = "https://api.spotify.com/v1/albums/";
    public static final String TRACK_PROGRESS_URL = "https://api.spotify.com/v1/me/player";
    public static final String USER_URL = "https://api.spotify.com/v1/me";
    public static final String DEVICES_URL = "https://api.spotify.com/v1/me/player/devices";
    public static final String LIBRARY_QUERY_URL ="https://api.spotify.com/v1/me/albums/contains?ids=";
    public static final String MUSIXMATCH_URL = "https://api.musixmatch.com/ws/1.1/matcher.lyrics.get?format=json";

    private int retryInterval = 5000;

    public abstract Object fetchItem(String url, SpotifyAuthentication authentication)
            throws SpotifyAuthenticationException, UsageLimitException, SpotifyWebAPIException;

    protected String getJsonResponse(String requestURL, String accessToken)
            throws UsageLimitException, SpotifyWebAPIException {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text;

        log.info("Calling: " + requestURL);

        try {
            String line;
            URL url = new URL(requestURL);
            HttpURLConnection con = makeRequest(url, accessToken);
            log.debug("Response code: " + con.getResponseCode());

            if (con.getResponseCode() == RESPONSE_RETRY) {
                // Can retry twice, 5 seconds apart
                for (int i = 0; i < 2; i++) {
                    try {
                        Thread.sleep(retryInterval);
                        con = makeRequest(url, accessToken);
                        if (con.getResponseCode() != RESPONSE_RETRY) {
                            break;
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (con.getResponseCode() == RESPONSE_LIMIT) {
                throw new UsageLimitException(con);
            } else if (con.getResponseCode() != RESPONSE_DONE &&
                       con.getResponseCode() != RESPONSE_OK) {
                throw new SpotifyWebAPIException("Sorry, I was unable to complete that request (code: " + con.getResponseCode() + ")");
            } else {
                inputStream = new InputStreamReader(con.getInputStream(), Charset.forName("US-ASCII"));
                bufferedReader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                text = builder.toString();
                log.info(text);
            }
        } catch (IOException e) {
            // reset text variable to a blank string
            log.error(e.getMessage());
            throw new SpotifyWebAPIException("Sorry, I was unable to complete that request");
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }

        return text;
    }

    private HttpURLConnection makeRequest(URL url, String accessToken) throws IOException {
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // set up url connection to get retrieve information back
            con.setRequestMethod("GET");

            // stuff the Authorization request header
            con.setRequestProperty("Authorization",
                    "Bearer " + accessToken);
            con.connect();
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
