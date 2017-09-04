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

import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class TrackTest {
    
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
    }
    
    @After
    public void tearDown() {
    }

    private JSONObject fetchJSON(String filename) throws IOException, JSONException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
        return TestUtilities.fetchJSON(in);
    }
    
    /**
     * Test of createFromJSON method, of class Track.
     */
    @Test
    public void testCreateFromJSON() throws Exception {
        JSONObject response = fetchJSON("nowplaying.json");
        Track result = Track.createFromJSON(response);
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
        JSONObject response = fetchJSON("track.json");
        Track result = Track.createFromItemJSON(response);
        assertEquals("The Life of Riley", result.getName());
        assertEquals("The Lightning Seeds", result.getArtistName());
        assertEquals("Sense", result.getAlbumName());
        assertFalse(result.isExplicit());
        assertNotNull(result.getAlbum());
    }

    /**
     * Test of createFromJSON method, of class Track.
     */
    @Test (expected = JSONException.class)
    public void testCreateFromInvalidJSON() throws Exception {
        System.out.println("createFromJSON");
        JSONObject response = fetchJSON("garbage.txt");
        Track result = Track.createFromJSON(response);
    }

    @org.junit.Test
    public void testCreateFromErrorJSON() throws Exception {
        System.out.println("createFromJSON");
        JSONObject response = fetchJSON("error.json");
        Track result = Track.createFromJSON(response);
        assertNull(result.getItem());
    }


    /**
     * Test of getProgressDurationString method, of class Track.
     */
    @org.junit.Test
    public void testGetProgressDurationString() throws Exception {
        System.out.println("getProgressDurationString");
        JSONObject response = fetchJSON("nowplaying.json");
        Track result = Track.createFromJSON(response);
        assertEquals("The track is 2 hours 47 minutes 12 seconds long. It is currently 44 seconds of the way through.",
                result.getProgressDurationString());
    }

    /**
     * Test of getTimeString method, of class Track.
     */
    @org.junit.Test
    public void testGetTimeString() {
        System.out.println("getTimeString");
        int millis = 5278145;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("1 hour 27 minutes 58 seconds", result);
    }    

    @org.junit.Test
    public void testGetTimeStringZeroHours() {
        System.out.println("getTimeString");
        int millis = 1080000;
        Track instance = new Track();
        String result = instance.getTimeString(millis);
        assertEquals("18 minutes", result);
    }    

    @org.junit.Test
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
