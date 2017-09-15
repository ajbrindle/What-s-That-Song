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
    private static final Logger log = LoggerFactory.getLogger(SpeechletUtils.class);
    public static final String REPROMPT_TEXT = "Next Action?";
    public static final int RESPONSE_DONE = 204;
    public static final int RESPONSE_RETRY = 202;
    public static final int RESPONSE_LIMIT = 429;
    public static final int RESPONSE_ERROR = 0;

    public static String getJsonResponse(String requestURL, String accessToken) throws UsageLimitException {
        InputStreamReader inputStream = null;
        BufferedReader bufferedReader = null;
        String text;
        try {
            String line;
            URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // set up url connection to get retrieve information back
            con.setRequestMethod("GET");

            // stuff the Authorization request header
            con.setRequestProperty("Authorization",
                    "Bearer " + accessToken);
            con.connect();

            if (con.getResponseCode() == RESPONSE_LIMIT) {
                throw new UsageLimitException(con);
            } else {
                inputStream = new InputStreamReader(con.getInputStream(), Charset.forName("US-ASCII"));
                bufferedReader = new BufferedReader(inputStream);
                StringBuilder builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                text = builder.toString();
            }
        } catch (IOException e) {
            // reset text variable to a blank string
            log.error(e.getMessage());
            text = "";
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(bufferedReader);
        }

        return text;
    }

    public static SpeechletResponse buildStandardAskResponse(String response, boolean doCard) {
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
