package com.sk7software.whatsthatsong.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class WhatsThatSongIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(intentName("WhatsThatSongIntent")) ||
                handlerInput.matches(intentName("TrackExplicitIntent")) ||
                handlerInput.matches(intentName("AlbumNameIntent")) ||
                handlerInput.matches(intentName("AlbumLibraryQueryIntent")) ||
                handlerInput.matches(intentName("AlbumLibraryAddIntent")) ||
                handlerInput.matches(intentName("OriginalAlbumNameIntent")) ||
                handlerInput.matches(intentName("TrackTimeIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {

        if (handlerInput.matches(intentName("WhatsThatSongIntent"))) {
            return new TrackHandler().getNowPlayingResponse(handlerInput);
        } else if (handlerInput.matches(intentName("TrackExplicitIntent"))) {
            return new TrackHandler().getTrackExplicitResponse(handlerInput);
        } else if (handlerInput.matches(intentName("AlbumNameIntent"))) {
            return new TrackHandler().getAlbumNameResponse(handlerInput);
        } else if (handlerInput.matches(intentName("AlbumLibraryQueryIntent"))) {
            return new TrackHandler().getAlbumInLibraryResponse(handlerInput);
        } else if (handlerInput.matches(intentName("AlbumLibraryAddIntent"))) {
            return new TrackHandler().getAlbumAddToLibraryResponse(handlerInput);
        } else if (handlerInput.matches(intentName("OriginalAlbumNameIntent"))) {
            return new TrackHandler().getTrackOriginalAlbumResponse(handlerInput);
        } else if (handlerInput.matches(intentName("TrackTimeIntent"))) {
            return new TrackHandler().getTrackTimeResponse(handlerInput);
        } else {
            return null;
        }
    }
}
