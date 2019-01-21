package com.sk7software.whatsthatsong.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.interfaces.display.*;
import com.amazon.ask.response.ResponseBuilder;
import com.amazonaws.util.StringUtils;
import com.sk7software.whatsthatsong.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

public class SpeechletUtils {
    private static final Logger log = LoggerFactory.getLogger(SpeechletUtils.class);
    public static final String REPROMPT_TEXT = "Next Action?";

    public static ResponseBuilder buildStandardAskResponse(final String response, final boolean doCard) {
        // Create the plain text output.
        log.debug("Building response");
        if (doCard) {
            // Create the Simple card content.
            return new ResponseBuilder().withSpeech(StringEscapeUtils.escapeHtml4(response))
                    .withReprompt(REPROMPT_TEXT)
                    .withSimpleCard("What's That Song", response);
        } else {
            return new ResponseBuilder().withSpeech(StringEscapeUtils.escapeHtml4(response))
                    .withReprompt(REPROMPT_TEXT);
        }
    }

    public static void addTrackDisplay(HandlerInput handlerInput, Track track, ResponseBuilder responseBuilder) {
        if (track != null) {
            if (new DeviceCapability(handlerInput).hasDisplay()) {
                log.debug("Device has display");
                String artworkUrl = track.getArtworkUrl();
                if (!StringUtils.isNullOrEmpty(artworkUrl)) {
                    responseBuilder
                            .addRenderTemplateDirective(
                                    createDisplayTemplate(
                                            track.getAlbum().getCovers(),
                                            "<b>" +
                                                    StringEscapeUtils.escapeHtml4(track.getName()) + "</b>",
                                            "<font size=\"2\">" +
                                                    StringEscapeUtils.escapeHtml4(track.getArtistName()) + "</font>"));
                }
            }
        }
    }

    public static void addAlbumDisplay(HandlerInput handlerInput, Track track, ResponseBuilder responseBuilder) {
        if (track != null) {
            if (new DeviceCapability(handlerInput).hasDisplay()) {
                log.debug("Device has display");
                String artworkUrl = track.getArtworkUrl();
                if (!StringUtils.isNullOrEmpty(artworkUrl)) {
                    try {
                        responseBuilder
                                .addRenderTemplateDirective(
                                        createDisplayTemplate(
                                                track.getAlbum().getCovers(),
                                                "<b>" +
                                                        StringEscapeUtils.escapeHtml4(track.getName()) + "</b>",
                                                "<font size=\"2\">" +
                                                        StringEscapeUtils.escapeHtml4(track.getArtistName()) +
                                                        "<br /><br /><i>" +
                                                        StringEscapeUtils.escapeHtml4(track.getAlbumName()) +
                                                        "</i></font>"));
                    } catch (Throwable e) {
                        log.error("Error rendering display: " + e.getMessage());
                    }
                }
            }
        }
    }

    public static void addLyricsDisplay(HandlerInput handlerInput, Track track, Lyrics lyrics, ResponseBuilder responseBuilder) {
        if (track != null) {
            if (new DeviceCapability(handlerInput).hasDisplay()) {
                log.debug("Device has display");
                String artworkUrl = track.getArtworkUrl();
                log.debug(lyrics.getFormattedLyrics());
                if (!StringUtils.isNullOrEmpty(artworkUrl) && lyrics != null) {
                    responseBuilder
                            .addRenderTemplateDirective(
                                    createDisplayTemplate(
                                            track.getAlbum().getCovers(),
                                            "<font size=\"2\">" +
                                                    lyrics.getFormattedLyrics() + "</font>",
                                            "<font size=\"1\"><i>" +
                                                    lyrics.getCopyright() + "</i></font>"));
                }
            }
        }
    }

    public static void addDeviceListDisplay(HandlerInput handlerInput, AvailableDevices devices, ResponseBuilder responseBuilder) {
        if (devices != null) {
            if (new DeviceCapability(handlerInput).hasDisplay()) {
                log.debug("Device has display");
                if (!devices.isEmpty()) {
                    responseBuilder.addRenderTemplateDirective(createListDisplayTemplate(devices));
                }
            }
        }
    }

    private static Template createDisplayTemplate(AlbumArt[] covers, String primaryText, String secondaryText) {
        List<ImageInstance> artwork = new ArrayList<>();
        for (AlbumArt aa : covers) {
            artwork.add(ImageInstance.builder()
                    .withUrl(aa.getUrl())
                    .withHeightPixels(aa.getHeight())
                    .withWidthPixels(aa.getWidth())
                    .build());
        }
        return BodyTemplate2.builder()
                .withTextContent(TextContent.builder()
                        .withPrimaryText(RichText.builder()
                                .withText(primaryText)
                                .build())
                        .withSecondaryText(RichText.builder()
                                .withText(secondaryText)
                                .build())
                        .build())
                .withImage(Image.builder()
                        .withSources(artwork)
                        .build())
                .build();
    }

    private static Template createListDisplayTemplate(AvailableDevices devices) {
        List<ListItem> list = new ArrayList<>();

        for (Device d : devices.getDevices()) {
            ListItem i = ListItem.builder()
                    .withToken(String.valueOf(d.getId()))
                    .withTextContent(TextContent.builder()
                            .withPrimaryText(RichText.builder()
                                    .withText("<font size=\"2\">" +
                                            StringEscapeUtils.escapeHtml4(d.getName()) + "</font>")
                                    .build())
                            .withSecondaryText(RichText.builder()
                                    .withText("<font size=\"1\">" +
                                            StringEscapeUtils.escapeHtml4(d.getType()) + "</font>")
                                    .build())
                            .build())
                    .build();
            list.add(i);
        }

        return ListTemplate1.builder()
                .withListItems(list)
                .withTitle("Available Devices")
                .build();
    }
}
