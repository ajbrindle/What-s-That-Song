package com.sk7software.whatsthatsong;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.network.SpotifyWebUpdateAPIService;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpotifyPlayerAPITest {
    private SpotifyWebUpdateAPIService service;
    private SpotifyAuthentication auth;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setup() {
        service = new SpotifyWebUpdateAPIService();
        auth = new SpotifyAuthentication();
        auth.setAccessToken("123");
    }

    @Test
    public void rateLimitReachedTest() throws Exception {
        stubFor(put(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "0")));
        try {
            service.sendCommand("http://localhost:8089/rateLimit", "PUT", null, auth);
            assertFalse(true);
        } catch (SpeechException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 1 second",
                    ule.getSpeechText());
        }
    }

    @Test
    public void rateLimitReachedOtherTest() throws Exception {
        stubFor(put(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "359")));
        try {
            service.sendCommand("http://localhost:8089/rateLimit", "PUT", null, auth);
            assertFalse(true);
        } catch (SpeechException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 6 minutes",
                    ule.getSpeechText());
        }
    }

    @Test
    public void rateLimitReachedLastTest() throws Exception {
        stubFor(put(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "10")));
        try {
            service.sendCommand("http://localhost:8089/rateLimit", "PUT", null, auth);
            assertFalse(true);
        } catch (SpeechException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 11 seconds",
                    ule.getSpeechText());
        }
    }

    @Test(expected = SpotifyWebAPIException.class)
    public void retryFailTest() throws Exception {
        stubFor(put(urlPathMatching("/retry"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Retry-After", "10")));

        service.setRetryInterval(5);
        service.sendCommand("http://localhost:8089/retry", "PUT", null, auth);
        assertFalse(true);
    }

    @Test
    public void retrySuccessTest() throws Exception {
        stubFor(put(urlPathMatching("/retry")).inScenario("Retry test")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(202))
                .willSetStateTo("retried"));

        stubFor(put(urlPathMatching("/retry")).inScenario("Retry test")
                .whenScenarioStateIs("retried")
                .willReturn(aResponse()
                        .withStatus(204)));

        service.setRetryInterval(5);
        service.sendCommand("http://localhost:8089/retry", "PUT", null, auth);
        assertTrue(true);
    }

}
