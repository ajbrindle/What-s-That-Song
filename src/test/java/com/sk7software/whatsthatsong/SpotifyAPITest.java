package com.sk7software.whatsthatsong;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.network.NowPlayingAPIService;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SpotifyAPITest {

    private SpotifyWebAPIService service;
    private SpotifyAuthentication auth;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setup() {
        service = new NowPlayingAPIService();
        auth = new SpotifyAuthentication();
        auth.setAccessToken("123");
    }

    @Test
    public void rateLimitReachedTest() throws Exception {
        stubFor(get(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "150")));
        try {
            Object track = service.fetchItem("http://localhost:8089/rateLimit", auth);
            assertFalse(true);
        } catch (SpeechException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                    "Please do not make any more requests for 2 minutes, 31 seconds",
                    ule.getSpeechText());
        }
    }

    @Test
    public void rateLimitReachedOtherTest() throws Exception {
        stubFor(get(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "60")));
        try {
            Object track = service.fetchItem("http://localhost:8089/rateLimit", auth);
            assertFalse(true);
        } catch (SpeechException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 1 minute, 1 second",
                    ule.getSpeechText());
        }
    }

    @Test
    public void rateLimitReachedLastTest() throws Exception {
        stubFor(get(urlPathMatching("/rateLimit"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Retry-After", "10")));
        try {
            Object track = service.fetchItem("http://localhost:8089/rateLimit", auth);
            assertFalse(true);
        } catch (SpeechException ule) {
            assertEquals("The usage limit for Spotify has been exceeded. " +
                            "Please do not make any more requests for 11 seconds",
                    ule.getSpeechText());
        }
    }

    @Test(expected = SpotifyWebAPIException.class)
    public void retryFailTest() throws Exception {
        stubFor(get(urlPathMatching("/retry"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Retry-After", "10")));

        service.setRetryInterval(5);
        Track track = (Track)service.fetchItem("http://localhost:8089/retry", auth);
        assertFalse(true);
    }

    @Test
    public void retrySuccessTest() throws Exception {
        stubFor(get(urlPathMatching("/retry")).inScenario("Retry test")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(202))
                .willSetStateTo("retried"));

        stubFor(get(urlPathMatching("/retry")).inScenario("Retry test")
                .whenScenarioStateIs("retried")
                .willReturn(aResponse()
                        .withStatus(200)
                .withBodyFile("nowplaying.json")));

        service.setRetryInterval(5);
        Track result = (Track)service.fetchItem("http://localhost:8089/retry", auth);
        assertEquals("Mr. Brightside", result.getName());
    }

}
