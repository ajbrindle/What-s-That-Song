package com.sk7software.whatsthatsong;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SpotifyAPITest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void rateLimitReachedTest() {
        stubFor(get(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "150")));
        try {
            String result = SpeechletUtils.getJsonResponse("http://localhost:8089/rateLimit", "123");
            assertFalse(true);
        } catch (UsageLimitException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                    "Please do not make any more requests for 2 minutes, 31 seconds",
                    ule.getSpeechText());
        }
    }

    @Test
    public void rateLimitReachedOtherTest() {
        stubFor(get(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "60")));
        try {
            String result = SpeechletUtils.getJsonResponse("http://localhost:8089/rateLimit", "123");
            assertFalse(true);
        } catch (UsageLimitException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 1 minute, 1 second",
                    ule.getSpeechText());
        }
    }

    @Test
    public void rateLimitReachedLastTest() {
        stubFor(get(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "10")));
        try {
            String result = SpeechletUtils.getJsonResponse("http://localhost:8089/rateLimit", "123");
            assertFalse(true);
        } catch (UsageLimitException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 11 seconds",
                    ule.getSpeechText());
        }
    }

}
