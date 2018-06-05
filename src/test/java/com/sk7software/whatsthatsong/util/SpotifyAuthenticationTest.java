package com.sk7software.whatsthatsong.util;

import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class SpotifyAuthenticationTest {

    private SpotifyAuthentication authentication;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        authentication = new SpotifyAuthentication();
    }

    @After
    public void tearDown() {
    }

    @Test (expected = SpotifyAuthenticationException.class)
    public void testNoInitialisation() throws Exception{
        authentication.getAccessToken();
    }

    @Test
    public void testSetCorrectly() throws Exception {
        authentication.setAccessToken("aCCESStOKEN");
        assertEquals("aCCESStOKEN", authentication.getAccessToken());
    }

    @Test
    public void testChangeAccessToken() throws Exception {
        authentication.setAccessToken("A");
        assertEquals("A", authentication.getAccessToken());
        authentication.setAccessToken("B");
        assertEquals("B", authentication.getAccessToken());
    }
}