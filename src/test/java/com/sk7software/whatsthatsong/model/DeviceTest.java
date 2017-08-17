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
import java.util.List;
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
public class DeviceTest {
    
    public DeviceTest() {
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
     * Test of createFromJSON method, of class Device.
     */
    @Test
    public void testCreateFromJSON() throws Exception {
        System.out.println("createFromJSON");
        JSONObject response = fetchJSON("devices.json");
        List<Device> result = Device.createFromJSON(response);
        assertEquals(4, result.size());
        
        for (int i=0; i<result.size(); i++) {
            assertEquals("device" + (i+1), result.get(i).getId());
        }
    }

    @Test (expected = JSONException.class)
    public void testCreateFromInvalidJSON() throws Exception {
        System.out.println("createFromInvalidJSON");
        JSONObject response = fetchJSON("garbage.txt");
        List<Device> result = Device.createFromJSON(response);
    }
    
    @Test (expected = JSONException.class)
    public void testCreateFromErrorJSON() throws Exception {
        System.out.println("createFromErrorJSON");
        JSONObject response = fetchJSON("error.json");
        List<Device> result = Device.createFromJSON(response);
    }

    /**
     * Test of calcNameMatchScore method, of class Device.
     */
    @Test
    public void testCalcNameMatchScore() {
        System.out.println("calcNameMatchScore");
        String spokenName = "dot SPEAKER";
        Device d = new Device();
        d.setName("Echo Dot");
        d.setType("Speaker");
        int expResult = 4;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcNameMatchScorePartial() {
        System.out.println("calcNameMatchScore");
        String spokenName = "Samsung Phone";
        Device d = new Device();
        d.setName("Motorola");
        d.setType("Smartphone");
        int expResult = 1;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcNameMatchScoreStripChars() {
        System.out.println("calcNameMatchScore");
        String spokenName = "Samsung Galaxy-S7 Phone";
        Device d = new Device();
        d.setName("Galaxy+S7");
        d.setType("Phone");
        int expResult = 4;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

    public void testCalcNameMatchScoreNamePartial() {
        System.out.println("calcNameMatchScore");
        String spokenName = "Lounge ABCbigDEF Speaker";
        Device d = new Device();
        d.setName("Big");
        d.setType("SpeaKer");
        int expResult = 3;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

    public void testCalcNameMatchScoreNone() {
        System.out.println("calcNameMatchScore");
        String spokenName = "Echo Dot Speaker";
        Device d = new Device();
        d.setName("LG G6");
        d.setType("Smartphone");
        int expResult = 0;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

}
