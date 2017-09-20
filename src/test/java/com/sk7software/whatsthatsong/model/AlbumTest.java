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
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.network.AlbumAPIService;
import com.sk7software.whatsthatsong.network.NowPlayingAPIService;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class AlbumTest {

    private SpotifyWebAPIService service;
    private SpotifyAuthentication auth;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    public AlbumTest() {
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
     * Test of createFromJSON method, of class Album.
     */
    @Test
    public void testCreateFromJSON() throws Exception {
        service = new AlbumAPIService();
        stubFor(get(urlPathMatching("/album"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("album.json")));

        Album result = (Album)service.fetchItem("http://localhost:8089/album", auth);
        assertEquals("She's So Unusual", result.getName());
        assertEquals(13, result.getTracks().getTotal());
        assertEquals("She's So Unusual, by Cyndi Lauper", result.getFullAlbumDescription());
    }

    @Test(expected = SpotifyWebAPIException.class)
    public void testCreateFromInvalidJSON() throws Exception {
        service = new AlbumAPIService();
        stubFor(get(urlPathMatching("/album"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("garbage.txt")));

        Album result = (Album)service.fetchItem("http://localhost:8089/album", auth);
    }

    @Test
    public void testCreateFromErrorJSON() throws Exception {
        service = new AlbumAPIService();
        stubFor(get(urlPathMatching("/album"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error.json")));

        Album result = (Album)service.fetchItem("http://localhost:8089/album", auth);
        assertNull(result.getName());
    }

    /**
     * Test of getAlbumInfo method, of class Album.
     */
    @Test
    public void testGetAlbumInfo() throws Exception {
        service = new AlbumAPIService();
        stubFor(get(urlPathMatching("/album"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("album.json")));

        Album result = (Album)service.fetchItem("http://localhost:8089/album", auth);
        assertEquals("It was released in 1983 and contains 13 tracks", result.getAlbumInfo());
    }

    @Test
    public void testGetAlbumInfo2() throws Exception {
        service = new AlbumAPIService();
        stubFor(get(urlPathMatching("/album"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("album2.json")));

        Album result = (Album)service.fetchItem("http://localhost:8089/album", auth);
        assertEquals("It was released in 2010 and contains 1 track", result.getAlbumInfo());
    }
}
