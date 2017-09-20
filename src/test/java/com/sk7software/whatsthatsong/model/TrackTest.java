/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sk7software.whatsthatsong.model;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sk7software.whatsthatsong.exception.SpeechException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.network.NowPlayingAPIService;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.network.TrackAPIService;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class TrackTest {

    private SpotifyWebAPIService service;
    private SpotifyAuthentication auth;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    public TrackTest() {
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
        service = new NowPlayingAPIService();
        stubFor(get(urlPathMatching("/track"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("nowplaying.json")));

        Track result = (Track)service.fetchItem("http://localhost:8089/track", auth);
        assertEquals("Mr. Brightside", result.getName());
        assertEquals("The Killers", result.getArtistName());
        assertEquals("Hot Fuss", result.getAlbumName());
        assertEquals("0eGsygTp906u18L0Oimnem", result.getId());
        assertEquals("6TJmQnO44YE5BtTxH8pop1", result.getAlbumId());
        assertEquals("spotify:album:6TJmQnO44YE5BtTxH8pop1", result.getAlbumUri());
        assertEquals("This song is Mr. Brightside, by The Killers", result.getFullDescription());
        assertEquals("Hot Fuss, by The Killers", result.getFullAlbumDescription());
        assertFalse(result.isExplicit());
        assertEquals(44272, result.getProgress());
        assertEquals(10032000, result.getDuration());
        assertNotNull(result.getAlbum());
    }

    @Test
    public void testCreateFromItemJSON() throws Exception {
        service = new TrackAPIService();
        stubFor(get(urlPathMatching("/track"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("track.json")));

        Track result = (Track)service.fetchItem("http://localhost:8089/track", auth);
        assertEquals("The Life of Riley", result.getName());
        assertEquals("The Lightning Seeds", result.getArtistName());
        assertEquals("Sense", result.getAlbumName());
        assertFalse(result.isExplicit());
        assertNotNull(result.getAlbum());
    }

    /**
     * Test of createFromJSON method, of class Track.
     */
    @Test (expected = SpotifyWebAPIException.class)
    public void testCreateFromInvalidJSON() throws Exception {
        service = new TrackAPIService();
        stubFor(get(urlPathMatching("/track"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("garbage.txt")));

        Track result = (Track)service.fetchItem("http://localhost:8089/track", auth);
    }

    @Test
    public void testCreateFromErrorJSON() throws Exception {
        service = new TrackAPIService();
        stubFor(get(urlPathMatching("/track"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error.json")));

        Track result = (Track)service.fetchItem("http://localhost:8089/track", auth);
        assertNull(result.getItem().getId());
    }


    /**
     * Test of getProgressDurationString method, of class Track.
     */
    @Test
    public void testGetProgressDurationString() throws Exception {
        service = new NowPlayingAPIService();
        stubFor(get(urlPathMatching("/track"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("nowplaying.json")));

        Track result = (Track)service.fetchItem("http://localhost:8089/track", auth);
        assertEquals("The track is 2 hours 47 minutes 12 seconds long. It is currently 44 seconds of the way through.",
                result.getProgressDurationString());
    }

    /**
     * Test of getTimeString method, of class Track.
     */
    @Test
    public void testGetTimeString() {
        System.out.println("getTimeString");
        int millis = 5278145;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("1 hour 27 minutes 58 seconds", result);
    }    

    @Test
    public void testGetTimeStringZeroHours() {
        System.out.println("getTimeString");
        int millis = 1080000;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("18 minutes", result);
    }    

    @Test
    public void testGetTimeStringOneMinute() {
        System.out.println("getTimeString");
        int millis = 65987;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("1 minute 5 seconds", result);
    }    
    
    @org.junit.Test
    public void testGetTimeStringZero() {
        System.out.println("getTimeString");
        int millis = 0;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("0 seconds", result);
    }

    @org.junit.Test
    public void testGetTimeStringOneSecond() {
        System.out.println("getTimeString");
        int millis = 121000;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("2 minutes 1 second", result);
    }
}
