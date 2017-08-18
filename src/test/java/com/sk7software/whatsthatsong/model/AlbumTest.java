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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class AlbumTest {
    
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
    }
    
    @After
    public void tearDown() {
    }

    private JSONObject fetchJSON(String filename) throws IOException, JSONException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
        return TestUtilities.fetchJSON(in);
    }


    /**
     * Test of createFromJSON method, of class Album.
     */
    @Test
    public void testCreateFromJSON() throws Exception {
        System.out.println("createFromJSON");
        JSONObject response = fetchJSON("album.json");
        Album result = Album.createFromJSON(response);
        assertEquals("She's So Unusual", result.getName());
        assertEquals(13, result.getTracks().getTotal());
    }

    @Test(expected = JSONException.class)
    public void testCreateFromInvalidJSON() throws Exception {
        System.out.println("createFromJSON");
        JSONObject response = fetchJSON("garbage.txt");
        Album result = Album.createFromJSON(response);
    }

    @Test
    public void testCreateFromErrorJSON() throws Exception {
        System.out.println("createFromJSON");
        JSONObject response = fetchJSON("error.json");
        Album result = Album.createFromJSON(response);
        assertNull(result.getName());
    }

    /**
     * Test of getAlbumInfo method, of class Album.
     */
    @Test
    public void testGetAlbumInfo() throws Exception {
        System.out.println("getAlbumInfo");
        JSONObject response = fetchJSON("album.json");
        Album result = Album.createFromJSON(response);
        assertEquals("It was released in 1983 and contains 13 tracks", result.getAlbumInfo());
    }

    @Test
    public void testGetAlbumInfo2() throws Exception {
        System.out.println("getAlbumInfo");
        JSONObject response = fetchJSON("album2.json");
        Album result = Album.createFromJSON(response);
        assertEquals("It was released in 2010 and contains 1 track", result.getAlbumInfo());
    }
}
