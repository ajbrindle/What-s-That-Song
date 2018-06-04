package com.sk7software.whatsthatsong.util;

import com.amazon.ask.model.interfaces.display.*;
import com.amazon.ask.response.ResponseBuilder;
import com.sk7software.whatsthatsong.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpeechletUtils {
    private static final Logger log = LoggerFactory.getLogger(SpeechletUtils.class);
    public static final String REPROMPT_TEXT = "Next Action?";

    public static ResponseBuilder buildStandardAskResponse(final String response, final boolean doCard) {
        // Create the plain text output.
        log.debug("Building response");
        if (doCard) {
            // Create the Simple card content.
            return new ResponseBuilder().withSpeech(response)
                    .withReprompt(REPROMPT_TEXT)
                    .withSimpleCard("What's That Song", response);
        } else {
            return new ResponseBuilder().withSpeech(response)
                    .withReprompt(REPROMPT_TEXT);
        }
    }

    public static Template createDisplayTemplate(Track track) {
        List<ImageInstance> artwork = new ArrayList<>();
        log.debug("Artwork URL: " + track.getArtworkUrl());
        artwork.add(ImageInstance.builder().withUrl(track.getArtworkUrl()).build());
        return BodyTemplate2.builder()
                .withTextContent(TextContent.builder()
                        .withPrimaryText(PlainText.builder().withText(track.getName()).build())
                        .withSecondaryText(PlainText.builder().withText(track.getArtistName()).build())
                        .build())
                .withBackgroundImage(Image.builder()
                        .withSources(artwork)
                        .build())
                .withImage(Image.builder()
                        .withSources(artwork)
                        .build())
                .build();
    }
}
