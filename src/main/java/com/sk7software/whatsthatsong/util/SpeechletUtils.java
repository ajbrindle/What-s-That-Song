package com.sk7software.whatsthatsong.util;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.sk7software.whatsthatsong.exception.UsageLimitException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class SpeechletUtils {
    public static final String REPROMPT_TEXT = "Next Action?";

    public static SpeechletResponse buildStandardAskResponse(final String response, final boolean doCard) {
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(response);

        // Create reprompt
        Reprompt reprompt = getStandardReprompt();

        if (doCard) {
            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("What's That Song");
            card.setContent(response);
            return SpeechletResponse.newAskResponse(speech, reprompt, card);
        } else {
            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
    }

    private static Reprompt getStandardReprompt() {
        Reprompt reprompt = new Reprompt();
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(REPROMPT_TEXT);
        reprompt.setOutputSpeech(repromptSpeech);
        return reprompt;
    }
}
