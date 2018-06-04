package com.sk7software.whatsthatsong;

import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerControlSpeechlet {
    private static final Logger log = LoggerFactory.getLogger(PlayerControlSpeechlet.class);

    private SpotifyAuthentication authentication;

    private PlayerControlSpeechlet() {}

    private PlayerControlSpeechlet(SpotifyAuthentication authentication) {
        this.authentication = authentication;
    }


}
