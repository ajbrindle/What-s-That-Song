package com.sk7software.whatsthatsong.model;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sk7software.whatsthatsong.network.LyricsAPIService;
import com.sk7software.whatsthatsong.network.NowPlayingAPIService;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class LyricsTest {
    private SpotifyWebAPIService service;
    private SpotifyAuthentication auth;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    public LyricsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        auth = new SpotifyAuthentication();
        auth.setAccessToken("123");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createFromJSON method, of class Track.
     */
    @Test
    public void testCreateFromJSON() throws Exception {
        service = new LyricsAPIService();
        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("lyrics.json")));

        Lyrics result = (Lyrics)service.fetchItem("http://localhost:8089/lyrics", auth);
        assertEquals("A long, long time ago<br/>", result.getFormattedLyrics().substring(0, 26));
        assertEquals("Lyrics powered by www.musixmatch.com", result.getCopyright().substring(0, 36));
    }
}
