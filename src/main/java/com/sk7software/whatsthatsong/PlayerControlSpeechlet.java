package com.sk7software.whatsthatsong;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONObject;
import com.sk7software.whatsthatsong.exception.SpotifyAuthenticationException;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import com.sk7software.whatsthatsong.model.Track;
import com.sk7software.whatsthatsong.util.PlayerAction;
import com.sk7software.whatsthatsong.util.SpeechletUtils;
import com.sk7software.whatsthatsong.util.SpotifyAuthentication;
import com.sun.prism.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.sk7software.whatsthatsong.util.SpeechletUtils.*;

public class PlayerControlSpeechlet {
    private static final Logger log = LoggerFactory.getLogger(PlayerControlSpeechlet.class);

    private SpotifyAuthentication authentication;

    private PlayerControlSpeechlet() {}

    private PlayerControlSpeechlet(SpotifyAuthentication authentication) {
        this.authentication = authentication;
    }


}
