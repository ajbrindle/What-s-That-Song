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
import com.sk7software.whatsthatsong.exception.MissingElementException;
import com.sk7software.whatsthatsong.exception.SpotifyWebAPIException;
import com.sk7software.whatsthatsong.exception.UnknownDeviceException;
import com.sk7software.whatsthatsong.network.DevicesAPIService;
import com.sk7software.whatsthatsong.network.SpotifyWebAPIService;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 *
 * @author Andrew
 */
public class DeviceTest {

    private SpotifyWebAPIService service;
    private SpotifyAuthentication auth;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

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
        auth = new SpotifyAuthentication();
        auth.setAccessToken("123");
        service = new DevicesAPIService();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createFromJSON method, of class Device.
     */
    @Test
    public void testCreateFromJSON() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
//        System.out.println("createFromJSON");
//        JSONObject response = fetchJSON("devices.json");
//        AvailableDevices result = AvailableDevices.createFromJSON(response);
        assertEquals(4, result.getNumberOfDevices());

        for (int i=1; i<=result.getNumberOfDevices(); i++) {
            assertEquals("device" + i, result.getDeviceAtIndex(i).getId());
        }
    }

    @Test (expected = SpotifyWebAPIException.class)
    public void testCreateFromInvalidJSON() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("garbage.txt")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
    }
    
    @Test (expected = SpotifyWebAPIException.class)
    public void testCreateFromErrorJSON() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("error.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
    }

    /**
     * Test of calcNameMatchScore method, of class Device.
     */
    @Test
    public void testCalcNameMatchScore() {
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
        String spokenName = "Samsung Galaxy-S7 Phone";
        Device d = new Device();
        d.setName("Galaxy+S7");
        d.setType("Phone");
        int expResult = 4;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

    @Test
    public void testCalcNameMatchScoreNamePartial() {
        String spokenName = "Lounge Big Speaker";
        Device d = new Device();
        d.setName("ABCbigDEF");
        d.setType("SpeaKer");
        int expResult = 3;
        int result = d.calcNameMatchScore(spokenName);
        assertEquals(expResult, result);
    }

    @Test
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

    @Test (expected = UnknownDeviceException.class)
    public void testDeviceIndexZero() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        result.getDeviceAtIndex(0);
    }

    @Test (expected = UnknownDeviceException.class)
    public void testDeviceIndexPlus1() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        result.getDeviceAtIndex(result.getNumberOfDevices()+1);
    }

    @Test (expected = MissingElementException.class)
    public void testNoDevices() throws Exception {
        AvailableDevices ad = new AvailableDevices();
        ad.getDeviceAtIndex(1);
    }

    @Test
    public void testDeviceListVerbose() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        String list = result.getDeviceList(true);
        String expected = "Device 1 is Echo Dot Speaker. " +
                          "Device 2 is Lounge AVR. " +
                          "Device 3 is Home PC Computer. " +
                          "Device 4 is MacBook Computer. ";
        assertEquals(expected, list);
    }

    @Test
    public void testDeviceListNonVerbose() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        String list = result.getDeviceList(false);
        String expected = "Device list fetched.";
        assertEquals(expected, list);
    }

    @Test (expected = MissingElementException.class)
    public void testDeviceListEmpty() throws Exception {
        AvailableDevices ad = new AvailableDevices();
        ad.getDeviceList(true);
    }

    @Test
    public void testActiveDevice() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        Device d = result.getActiveDevice();
        assertTrue(d.isActive());
    }

    @Test
    public void testChangeActiveDeviceId() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        Device d = result.getActiveDevice();
        int activeIndex = d.getIndex();
        int newIndex = 1;
        // Pick another index
        if (activeIndex == 1) {
            newIndex = result.getNumberOfDevices();
        }

        String id = result.getDeviceAtIndex(newIndex).getId();
        result.setActiveDevice(id);

        Device original = result.getDeviceAtIndex(activeIndex);
        Device changed = result.getDeviceAtIndex(newIndex);
        assertTrue(changed.isActive());
        assertFalse(original.isActive());
    }

    @Test
    public void testChangeActiveDevice() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        Device original = result.getActiveDevice();
        Device changed = new Device();

        // Find another device that isn't active
        for (int i=1; i<result.getNumberOfDevices(); i++) {
            changed = result.getDeviceAtIndex(i);
            if (!changed.isActive()) {
                result.setActiveDevice(changed);
                break;
            }
        }

        assertTrue(changed.isActive());
        assertFalse(original.isActive());
    }

    @Test
    public void testDeviceNameMatch() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        String spokenName = "Mac Computer";
        String id = result.findClosestNameMatchId(spokenName);
        assertEquals("device4", id);
    }

    @Test (expected = MissingElementException.class)
    public void testDeviceNameMatchNoDevices() throws Exception {
        AvailableDevices ad = new AvailableDevices();
        ad.findClosestNameMatchId("blah");
    }

    @Test (expected = UnknownDeviceException.class)
    public void testDeviceNameNoMatch() throws Exception {
        stubFor(get(urlPathMatching("/devices"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("devices.json")));

        AvailableDevices result = (AvailableDevices)service.fetchItem("http://localhost:8089/devices", auth);
        result.findClosestNameMatchId("blah bluh");
    }
}
