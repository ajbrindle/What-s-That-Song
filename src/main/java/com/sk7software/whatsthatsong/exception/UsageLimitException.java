package com.sk7software.whatsthatsong.exception;

import com.amazon.speech.speechlet.SpeechletResponse;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class UsageLimitException extends SpeechException {

    private int retryAfter;

    public UsageLimitException() {
        super("Usage limit exceeded");
        retryAfter = 0;
    }

    public UsageLimitException(HttpURLConnection conn) {
        super();
        String retryHeader = conn.getHeaderField("Retry-After");
        if (retryHeader != null) {
            retryAfter = Integer.parseInt(retryHeader) + 1;
            this.speechText = "The usage limit for Spotify has been exceeded. " +
                    "Please do not make any more requests for " + formatRetryMessage();
        } else {
            this.speechText = "The usage limit for Spotify has been exceeded. " +
                    "Please do not make any more requests for a few minutes.";
        }
    }

    private String formatRetryMessage() {
        if (retryAfter < 60) {
            return retryAfter + " second" + (retryAfter == 1 ? "" : "s");
        } else {
            int minutes = retryAfter / 60;
            int seconds = retryAfter % 60;
            return minutes + " minute" + (minutes == 1 ? "" : "s") +
                    (seconds > 0 ? ", " + seconds + " second" + (seconds == 1 ? "" : "s") : "");
        }
    }
}
